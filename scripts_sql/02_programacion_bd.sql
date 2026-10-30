-- Programacion y logica reactiva de base de datos
USE happy_feet_veterinaria;

-- 1. TRIGGERS PREVENTIVOS BEFORE INSERT (VALIDACIONES CLINICAS Y STOCK)

-- Bloqueo de insumo vencido en procedimientos
DROP TRIGGER IF EXISTS trg_bloquear_insumo_vencido_procedimiento;
DELIMITER $$
CREATE TRIGGER trg_bloquear_insumo_vencido_procedimiento
BEFORE INSERT ON procedimiento_insumos
FOR EACH ROW
BEGIN
    DECLARE v_vencimiento DATE;
    SELECT fecha_vencimiento INTO v_vencimiento 
    FROM inventario WHERE id = NEW.producto_id;

    IF v_vencimiento IS NOT NULL AND v_vencimiento <= CURDATE() THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Insumo vencido: No se puede utilizar un producto cuya fecha de caducidad ha expirado.';
    END IF;
END$$
DELIMITER ;

-- Validacion preventiva de stock en insumos de procedimientos
DROP TRIGGER IF EXISTS trg_validar_stock_disponible_insumos;
DELIMITER $$
CREATE TRIGGER trg_validar_stock_disponible_insumos
BEFORE INSERT ON procedimiento_insumos
FOR EACH ROW
BEGIN
    DECLARE v_stock INT;
    SELECT cantidad_stock INTO v_stock 
    FROM inventario WHERE id = NEW.producto_id;

    IF v_stock IS NULL OR v_stock < NEW.cantidad_utilizada THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Inventario insuficiente: No hay existencias suficientes para el insumo solicitado.';
    END IF;
END$$
DELIMITER ;

-- Bloqueo de medicamentos vencidos y stock insuficiente en prescripciones
DROP TRIGGER IF EXISTS trg_validar_stock_disponible_prescripcion;
DELIMITER $$
CREATE TRIGGER trg_validar_stock_disponible_prescripcion
BEFORE INSERT ON prescripciones
FOR EACH ROW
BEGIN
    DECLARE v_stock INT;
    DECLARE v_vencimiento DATE;

    SELECT cantidad_stock, fecha_vencimiento INTO v_stock, v_vencimiento
    FROM inventario WHERE id = NEW.producto_id;

    IF v_vencimiento IS NOT NULL AND v_vencimiento <= CURDATE() THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Medicamento vencido: No se puede prescribir un producto vencido.';
    END IF;

    IF v_stock IS NULL OR v_stock < NEW.cantidad THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Inventario insuficiente: No hay existencias para prescribir el producto solicitado.';
    END IF;
END$$
DELIMITER ;

-- Bloqueo de vacuna vencida y stock en jornadas
DROP TRIGGER IF EXISTS trg_bloquear_vacuna_vencida_jornada;
DELIMITER $$
CREATE TRIGGER trg_bloquear_vacuna_vencida_jornada
BEFORE INSERT ON registro_jornada_vacunacion
FOR EACH ROW
BEGIN
    DECLARE v_stock INT;
    DECLARE v_vencimiento DATE;

    SELECT cantidad_stock, fecha_vencimiento INTO v_stock, v_vencimiento
    FROM inventario WHERE id = NEW.vacuna_id;

    IF v_vencimiento IS NOT NULL AND v_vencimiento <= CURDATE() THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Vacuna vencida: No se puede aplicar un biologico cuya fecha de caducidad ha expirado.';
    END IF;

    IF v_stock IS NULL OR v_stock < 1 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Inventario insuficiente: No hay dosis disponibles de la vacuna solicitada.';
    END IF;
END$$
DELIMITER ;

-- 2. TRIGGER ANTISOLAPAMIENTO DE CITAS MEDICAS

-- Antisolapamiento de citas en insercion
DROP TRIGGER IF EXISTS trg_prevenir_solapamiento_citas_insert;
DELIMITER $$
CREATE TRIGGER trg_prevenir_solapamiento_citas_insert
BEFORE INSERT ON citas
FOR EACH ROW
BEGIN
    IF NEW.veterinario_id IS NOT NULL AND NEW.estado IN ('Programada', 'Confirmada') THEN
        IF EXISTS (
            SELECT 1 FROM citas
            WHERE veterinario_id = NEW.veterinario_id
              AND estado IN ('Programada', 'Confirmada')
              AND ABS(TIMESTAMPDIFF(MINUTE, fecha_hora, NEW.fecha_hora)) < 30
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Conflicto de agenda: El veterinario ya cuenta con una cita asignada en ese intervalo de horario.';
        END IF;
    END IF;
END$$
DELIMITER ;

-- Antisolapamiento de citas en actualizacion
DROP TRIGGER IF EXISTS trg_prevenir_solapamiento_citas_update;
DELIMITER $$
CREATE TRIGGER trg_prevenir_solapamiento_citas_update
BEFORE UPDATE ON citas
FOR EACH ROW
BEGIN
    IF NEW.veterinario_id IS NOT NULL AND NEW.estado IN ('Programada', 'Confirmada') THEN
        IF EXISTS (
            SELECT 1 FROM citas
            WHERE id <> NEW.id
              AND veterinario_id = NEW.veterinario_id
              AND estado IN ('Programada', 'Confirmada')
              AND ABS(TIMESTAMPDIFF(MINUTE, fecha_hora, NEW.fecha_hora)) < 30
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Conflicto de agenda: El veterinario ya cuenta con una cita asignada en ese intervalo de horario.';
        END IF;
    END IF;
END$$
DELIMITER ;

-- 3. TRIGGERS DE DEDUCCION AUTOMATICA DE INVENTARIO

-- Deduccion automatica de insumo quirurgico
DROP TRIGGER IF EXISTS trg_deducir_stock_insumo_procedimiento;
DELIMITER $$
CREATE TRIGGER trg_deducir_stock_insumo_procedimiento
AFTER INSERT ON procedimiento_insumos
FOR EACH ROW
BEGIN
    DECLARE v_stock_ant INT;
    DECLARE v_stock_nue INT;

    SELECT cantidad_stock INTO v_stock_ant FROM inventario WHERE id = NEW.producto_id;
    SET v_stock_nue = v_stock_ant - NEW.cantidad_utilizada;

    UPDATE inventario 
    SET cantidad_stock = v_stock_nue 
    WHERE id = NEW.producto_id;

    INSERT INTO inventario_movimientos (
        producto_id,
        tipo_movimiento,
        cantidad,
        stock_anterior,
        stock_nuevo,
        motivo,
        referencia_procedimiento_id,
        usuario,
        fecha_hora
    ) VALUES (
        NEW.producto_id,
        'Salida',
        NEW.cantidad_utilizada,
        v_stock_ant,
        v_stock_nue,
        'Consumo de insumo en procedimiento quirurgico',
        NEW.procedimiento_id,
        'Sistema',
        NOW()
    );
END$$
DELIMITER ;

-- Deduccion automatica por prescripcion medica
DROP TRIGGER IF EXISTS trg_deducir_stock_prescripcion;
DELIMITER $$
CREATE TRIGGER trg_deducir_stock_prescripcion
AFTER INSERT ON prescripciones
FOR EACH ROW
BEGIN
    DECLARE v_stock_ant INT;
    DECLARE v_stock_nue INT;

    SELECT cantidad_stock INTO v_stock_ant FROM inventario WHERE id = NEW.producto_id;
    SET v_stock_nue = v_stock_ant - NEW.cantidad;

    UPDATE inventario 
    SET cantidad_stock = v_stock_nue 
    WHERE id = NEW.producto_id;

    INSERT INTO inventario_movimientos (
        producto_id,
        tipo_movimiento,
        cantidad,
        stock_anterior,
        stock_nuevo,
        motivo,
        referencia_consulta_id,
        referencia_procedimiento_id,
        usuario,
        fecha_hora
    ) VALUES (
        NEW.producto_id,
        'Salida',
        NEW.cantidad,
        v_stock_ant,
        v_stock_nue,
        'Deduccion por prescripcion medica',
        NEW.consulta_id,
        NEW.procedimiento_id,
        'Sistema',
        NOW()
    );
END$$
DELIMITER ;

-- Deduccion automatica por vacuna en jornada
DROP TRIGGER IF EXISTS trg_deducir_stock_vacuna_jornada;
DELIMITER $$
CREATE TRIGGER trg_deducir_stock_vacuna_jornada
AFTER INSERT ON registro_jornada_vacunacion
FOR EACH ROW
BEGIN
    DECLARE v_stock_ant INT;
    DECLARE v_stock_nue INT;

    SELECT cantidad_stock INTO v_stock_ant FROM inventario WHERE id = NEW.vacuna_id;
    SET v_stock_nue = v_stock_ant - 1;

    UPDATE inventario 
    SET cantidad_stock = v_stock_nue 
    WHERE id = NEW.vacuna_id;

    INSERT INTO inventario_movimientos (
        producto_id,
        tipo_movimiento,
        cantidad,
        stock_anterior,
        stock_nuevo,
        motivo,
        referencia_id,
        usuario,
        fecha_hora
    ) VALUES (
        NEW.vacuna_id,
        'Salida',
        1,
        v_stock_ant,
        v_stock_nue,
        CONCAT('Dosis aplicada en jornada ID: ', NEW.jornada_id),
        NEW.id,
        'Sistema',
        NOW()
    );
END$$
DELIMITER ;

-- 4. TRIGGERS DE ALERTAS REACTIVAS Y AUDITORIA

-- Generacion reactiva de alerta de stock minimo con filtro anti-spam
DROP TRIGGER IF EXISTS trg_alerta_stock_minimo_update;
DELIMITER $$
CREATE TRIGGER trg_alerta_stock_minimo_update
AFTER UPDATE ON inventario
FOR EACH ROW
BEGIN
    IF NEW.cantidad_stock <= NEW.stock_minimo THEN
        IF NOT EXISTS (
            SELECT 1 FROM alertas_sistema
            WHERE referencia_tabla = 'inventario'
              AND referencia_id = NEW.id
              AND tipo_alerta = 'STOCK_MINIMO'
              AND leida = FALSE
        ) THEN
            INSERT INTO alertas_sistema (
                tipo_alerta,
                nivel_severidad,
                referencia_tabla,
                referencia_id,
                mensaje,
                fecha_generacion,
                leida
            ) VALUES (
                'STOCK_MINIMO',
                IF(NEW.cantidad_stock = 0, 'Critica', 'Advertencia'),
                'inventario',
                NEW.id,
                CONCAT('Stock bajo para producto: ', NEW.nombre_producto, ' (Stock actual: ', NEW.cantidad_stock, ', Minimo: ', NEW.stock_minimo, ')'),
                NOW(),
                FALSE
            );
        END IF;
    END IF;
END$$
DELIMITER ;

-- Auditoria de cambios de precio de compra y venta
DROP TRIGGER IF EXISTS trg_historico_precios_update;
DELIMITER $$
CREATE TRIGGER trg_historico_precios_update
AFTER UPDATE ON inventario
FOR EACH ROW
BEGIN
    IF OLD.precio_compra <> NEW.precio_compra THEN
        INSERT INTO historico_precios_inventario (
            producto_id,
            precio_anterior,
            precio_nuevo,
            tipo_precio,
            fecha_modificacion,
            motivo
        ) VALUES (
            NEW.id,
            OLD.precio_compra,
            NEW.precio_compra,
            'Compra',
            NOW(),
            'Actualizacion de precio de compra'
        );
    END IF;

    IF OLD.precio_venta <> NEW.precio_venta THEN
        INSERT INTO historico_precios_inventario (
            producto_id,
            precio_anterior,
            precio_nuevo,
            tipo_precio,
            fecha_modificacion,
            motivo
        ) VALUES (
            NEW.id,
            OLD.precio_venta,
            NEW.precio_venta,
            'Venta',
            NOW(),
            'Actualizacion de precio de venta'
        );
    END IF;
END$$
DELIMITER ;

-- 5. PROCEDIMIENTOS ALMACENADOS TRANSACCIONALES

-- Transferencia formal de propiedad de mascota
DROP PROCEDURE IF EXISTS sp_transferir_propiedad_mascota;
DELIMITER $$
CREATE PROCEDURE sp_transferir_propiedad_mascota(
    IN in_mascota_id INT,
    IN in_nuevo_dueno_id INT,
    IN in_motivo TEXT
)
BEGIN
    DECLARE v_dueno_anterior_id INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    SELECT dueno_id INTO v_dueno_anterior_id 
    FROM mascotas WHERE id = in_mascota_id;

    IF v_dueno_anterior_id IS NULL THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: La mascota especificada no existe.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM duenos WHERE id = in_nuevo_dueno_id) THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El nuevo dueno especificado no existe.';
    END IF;

    IF v_dueno_anterior_id = in_nuevo_dueno_id THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Aviso: La mascota ya pertenece al dueno indicado.';
    END IF;

    START TRANSACTION;
        UPDATE mascotas 
        SET dueno_id = in_nuevo_dueno_id 
        WHERE id = in_mascota_id;

        INSERT INTO historial_transferencias_mascota (
            mascota_id,
            dueno_anterior_id,
            dueno_nuevo_id,
            fecha_transferencia,
            motivo
        ) VALUES (
            in_mascota_id,
            v_dueno_anterior_id,
            in_nuevo_dueno_id,
            NOW(),
            in_motivo
        );
    COMMIT;
END$$
DELIMITER ;

-- Formalizacion de proceso de adopcion
DROP PROCEDURE IF EXISTS sp_formalizar_adopcion;
DELIMITER $$
CREATE PROCEDURE sp_formalizar_adopcion(
    IN in_adopcion_id INT,
    IN in_dueno_id INT
)
BEGIN
    DECLARE v_mascota_id INT;
    DECLARE v_estado VARCHAR(50);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    SELECT mascota_id, estado INTO v_mascota_id, v_estado 
    FROM mascotas_adopcion WHERE id = in_adopcion_id;

    IF v_mascota_id IS NULL THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: Registro de adopcion no encontrado.';
    END IF;

    IF v_estado = 'Adoptada' THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: La mascota ya ha sido adoptada previamente.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM duenos WHERE id = in_dueno_id) THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El adoptante no existe en el registro de duenos.';
    END IF;

    START TRANSACTION;
        UPDATE mascotas 
        SET dueno_id = in_dueno_id 
        WHERE id = v_mascota_id;

        UPDATE mascotas_adopcion 
        SET estado = 'Adoptada',
            fecha_adopcion = CURDATE(),
            dueno_adoptante_id = in_dueno_id
        WHERE id = in_adopcion_id;

        INSERT INTO adopciones (
            mascota_adopcion_id,
            dueno_id,
            fecha_adopcion,
            contrato_texto,
            seguimiento_requerido,
            fecha_primer_seguimiento
        ) VALUES (
            in_adopcion_id,
            in_dueno_id,
            CURDATE(),
            CONCAT('Contrato formalizado de adopcion para mascota ID: ', v_mascota_id),
            TRUE,
            DATE_ADD(CURDATE(), INTERVAL 30 DAY)
        );
    COMMIT;
END$$
DELIMITER ;

-- 6. FUNCIONES ALMACENADAS

-- Calculo de edad formateada de la mascota
DROP FUNCTION IF EXISTS fn_calcular_edad_mascota;
DELIMITER $$
CREATE FUNCTION fn_calcular_edad_mascota(in_fecha_nacimiento DATE)
RETURNS VARCHAR(50)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_anios INT;
    DECLARE v_meses INT;
    DECLARE v_resultado VARCHAR(50);

    IF in_fecha_nacimiento IS NULL THEN
        RETURN 'Desconocida';
    END IF;

    SET v_anios = TIMESTAMPDIFF(YEAR, in_fecha_nacimiento, CURDATE());
    SET v_meses = TIMESTAMPDIFF(MONTH, in_fecha_nacimiento, CURDATE()) % 12;

    IF v_anios > 0 THEN
        IF v_meses > 0 THEN
            SET v_resultado = CONCAT(v_anios, ' anio(s), ', v_meses, ' mes(es)');
        ELSE
            SET v_resultado = CONCAT(v_anios, ' anio(s)');
        END IF;
    ELSE
        SET v_resultado = CONCAT(v_meses, ' mes(es)');
    END IF;

    RETURN v_resultado;
END$$
DELIMITER ;

-- 7. VISTAS GERENCIALES (INTELIGENCIA DE NEGOCIO)

-- Servicios y productos mas solicitados
CREATE OR REPLACE VIEW v_servicios_mas_solicitados AS
SELECT 
    fd.descripcion AS concepto,
    fd.tipo_item,
    COUNT(fd.id) AS total_solicitudes,
    SUM(fd.cantidad) AS cantidad_total,
    SUM(fd.subtotal) AS ingresos_totales
FROM factura_detalles fd
JOIN facturas f ON fd.factura_id = f.id
WHERE f.estado <> 'Anulada'
GROUP BY fd.descripcion, fd.tipo_item
ORDER BY total_solicitudes DESC;

-- Desempeno del equipo veterinario
CREATE OR REPLACE VIEW v_desempeno_veterinarios AS
SELECT 
    v.id AS veterinario_id,
    v.nombre_completo,
    v.licencia_profesional,
    v.especialidad,
    COUNT(DISTINCT c.id) AS total_consultas,
    COUNT(DISTINCT p.id) AS total_procedimientos,
    (COUNT(DISTINCT c.id) + COUNT(DISTINCT p.id)) AS total_atenciones,
    COALESCE(SUM(c.costo_consulta), 0.00) + COALESCE(SUM(p.costo_base), 0.00) AS ingresos_generados
FROM veterinarios v
LEFT JOIN consultas_medicas c ON v.id = c.veterinario_id
LEFT JOIN procedimientos_especiales p ON v.id = p.veterinario_id AND p.estado = 'Finalizado'
WHERE v.activo = TRUE
GROUP BY v.id, v.nombre_completo, v.licencia_profesional, v.especialidad
ORDER BY total_atenciones DESC;

-- Alertas criticas de inventario y vencimientos proximos
CREATE OR REPLACE VIEW v_alertas_inventario_criticas AS
SELECT 
    i.id AS producto_id,
    i.codigo_barras,
    i.nombre_producto,
    pt.nombre AS categoria,
    i.lote,
    i.cantidad_stock,
    i.stock_minimo,
    i.fecha_vencimiento,
    DATEDIFF(i.fecha_vencimiento, CURDATE()) AS dias_para_vencer,
    CASE 
        WHEN i.fecha_vencimiento <= CURDATE() THEN 'VENCIDO'
        WHEN i.cantidad_stock = 0 THEN 'SIN_STOCK'
        WHEN i.cantidad_stock <= i.stock_minimo THEN 'STOCK_MINIMO'
        WHEN DATEDIFF(i.fecha_vencimiento, CURDATE()) <= 30 THEN 'POR_VENCER'
        ELSE 'NORMAL'
    END AS estado_alerta
FROM inventario i
JOIN producto_tipos pt ON i.tipo_id = pt.id
WHERE i.activo = TRUE 
  AND (i.cantidad_stock <= i.stock_minimo OR DATEDIFF(i.fecha_vencimiento, CURDATE()) <= 30)
ORDER BY i.cantidad_stock ASC, dias_para_vencer ASC;

-- Resumen de facturacion mensual
CREATE OR REPLACE VIEW v_resumen_facturacion_mensual AS
SELECT 
    YEAR(fecha_emision) AS anio,
    MONTH(fecha_emision) AS mes,
    COUNT(id) AS facturas_emitidas,
    SUM(IF(estado = 'Pagada', 1, 0)) AS facturas_pagadas,
    SUM(IF(estado = 'Pendiente', 1, 0)) AS facturas_pendientes,
    SUM(IF(estado = 'Anulada', 1, 0)) AS facturas_anuladas,
    SUM(IF(estado <> 'Anulada', subtotal, 0.00)) AS subtotal_neto,
    SUM(IF(estado <> 'Anulada', descuento, 0.00)) AS total_descuentos,
    SUM(IF(estado <> 'Anulada', impuesto, 0.00)) AS total_impuestos,
    SUM(IF(estado <> 'Anulada', total, 0.00)) AS total_facturado
FROM facturas
GROUP BY YEAR(fecha_emision), MONTH(fecha_emision)
ORDER BY anio DESC, mes DESC;

-- 8. EVENTO PROGRAMADO DE AUDITORIA DE VENCIMIENTOS

DROP EVENT IF EXISTS evt_auditoria_vencimientos_diaria;
DELIMITER $$
CREATE EVENT evt_auditoria_vencimientos_diaria
ON SCHEDULE EVERY 1 DAY
STARTS (TIMESTAMP(CURDATE()) + INTERVAL 1 DAY)
DO
BEGIN
    -- Alertas para productos vencidos
    INSERT INTO alertas_sistema (
        tipo_alerta,
        nivel_severidad,
        referencia_tabla,
        referencia_id,
        mensaje,
        fecha_generacion,
        leida
    )
    SELECT 
        'PRODUCTO_VENCIDO',
        'Critica',
        'inventario',
        i.id,
        CONCAT('Producto vencido: ', i.nombre_producto, ' (Lote: ', i.lote, ', Caducidad: ', i.fecha_vencimiento, ')'),
        NOW(),
        FALSE
    FROM inventario i
    WHERE i.activo = TRUE 
      AND i.fecha_vencimiento <= CURDATE()
      AND NOT EXISTS (
          SELECT 1 FROM alertas_sistema 
          WHERE referencia_tabla = 'inventario' 
            AND referencia_id = i.id 
            AND tipo_alerta = 'PRODUCTO_VENCIDO' 
            AND leida = FALSE
      );

    -- Alertas para productos proximos a vencer (<= 30 dias)
    INSERT INTO alertas_sistema (
        tipo_alerta,
        nivel_severidad,
        referencia_tabla,
        referencia_id,
        mensaje,
        fecha_generacion,
        leida
    )
    SELECT 
        'PRODUCTO_POR_VENCER',
        'Advertencia',
        'inventario',
        i.id,
        CONCAT('Producto proximo a vencer: ', i.nombre_producto, ' (Lote: ', i.lote, ', Caducidad: ', i.fecha_vencimiento, ')'),
        NOW(),
        FALSE
    FROM inventario i
    WHERE i.activo = TRUE 
      AND i.fecha_vencimiento > CURDATE()
      AND i.fecha_vencimiento <= DATE_ADD(CURDATE(), INTERVAL 30 DAY)
      AND NOT EXISTS (
          SELECT 1 FROM alertas_sistema 
          WHERE referencia_tabla = 'inventario' 
            AND referencia_id = i.id 
            AND tipo_alerta = 'PRODUCTO_POR_VENCER' 
            AND leida = FALSE
      );
END$$
DELIMITER ;
