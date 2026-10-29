-- Base de datos Happy Feet
DROP DATABASE IF EXISTS happy_feet_veterinaria;
CREATE DATABASE happy_feet_veterinaria CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE happy_feet_veterinaria;

-- Tablas de catalogo
CREATE TABLE especies (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL UNIQUE,
  descripcion TEXT NULL,
  INDEX idx_especies_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE razas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  especie_id INT NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  caracteristicas TEXT NULL,
  FOREIGN KEY (especie_id) REFERENCES especies(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  UNIQUE KEY uk_raza_especie (especie_id, nombre),
  INDEX idx_razas_especie (especie_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE producto_tipos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL UNIQUE,
  descripcion TEXT NULL,
  INDEX idx_prod_tipos_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE evento_tipos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL UNIQUE,
  descripcion TEXT NULL,
  INDEX idx_evento_tipos_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cita_estados (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL UNIQUE,
  descripcion TEXT NULL,
  INDEX idx_cita_estados_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Entidades maestras
CREATE TABLE duenos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  tipo_documento VARCHAR(20) NOT NULL DEFAULT 'CC',
  numero_documento VARCHAR(20) NOT NULL UNIQUE,
  nombre_completo VARCHAR(150) NOT NULL,
  direccion VARCHAR(255) NULL,
  telefono VARCHAR(20) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  contacto_emergencia VARCHAR(100) NULL,
  fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  INDEX idx_duenos_documento (numero_documento),
  INDEX idx_duenos_email (email),
  INDEX idx_duenos_nombre (nombre_completo),
  INDEX idx_duenos_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE veterinarios (
  id INT AUTO_INCREMENT PRIMARY KEY,
  tipo_documento VARCHAR(20) NOT NULL DEFAULT 'CC',
  numero_documento VARCHAR(20) NOT NULL UNIQUE,
  nombre_completo VARCHAR(150) NOT NULL,
  licencia_profesional VARCHAR(50) NOT NULL UNIQUE,
  especialidad VARCHAR(100) NOT NULL,
  telefono VARCHAR(20) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  fecha_contratacion DATE NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  INDEX idx_vet_documento (numero_documento),
  INDEX idx_vet_licencia (licencia_profesional),
  INDEX idx_vet_especialidad (especialidad),
  INDEX idx_vet_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE mascotas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  dueno_id INT NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  especie_id INT NOT NULL,
  raza_id INT NOT NULL,
  fecha_nacimiento DATE NOT NULL,
  sexo ENUM('Macho', 'Hembra') NOT NULL,
  peso_actual DECIMAL(5,2) NOT NULL,
  microchip VARCHAR(50) NULL UNIQUE,
  tatuaje VARCHAR(50) NULL UNIQUE,
  foto_url VARCHAR(255) NULL,
  alergias TEXT NULL,
  condiciones_preexistentes TEXT NULL,
  fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  FOREIGN KEY (dueno_id) REFERENCES duenos(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (especie_id) REFERENCES especies(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (raza_id) REFERENCES razas(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX idx_mascotas_dueno (dueno_id),
  INDEX idx_mascotas_nombre (nombre),
  INDEX idx_mascotas_especie (especie_id),
  INDEX idx_mascotas_raza (raza_id),
  INDEX idx_mascotas_microchip (microchip),
  INDEX idx_mascotas_activo (activo),
  CONSTRAINT chk_mascota_peso CHECK (peso_actual > 0.01 AND peso_actual <= 999.99)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE historial_transferencias_mascota (
  id INT AUTO_INCREMENT PRIMARY KEY,
  mascota_id INT NOT NULL,
  dueno_anterior_id INT NOT NULL,
  dueno_nuevo_id INT NOT NULL,
  fecha_transferencia DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  motivo TEXT NULL,
  FOREIGN KEY (mascota_id) REFERENCES mascotas(id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (dueno_anterior_id) REFERENCES duenos(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (dueno_nuevo_id) REFERENCES duenos(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX idx_transf_mascota (mascota_id),
  INDEX idx_transf_dueno_ant (dueno_anterior_id),
  INDEX idx_transf_dueno_nuevo (dueno_nuevo_id),
  INDEX idx_transf_fecha (fecha_transferencia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Servicios medicos y citas
CREATE TABLE servicios (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(150) NOT NULL,
  descripcion TEXT NULL,
  categoria VARCHAR(100) NULL,
  precio_base DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  duracion_estimada_minutos INT NULL DEFAULT 30,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  INDEX idx_servicios_nombre (nombre),
  INDEX idx_servicios_categoria (categoria),
  INDEX idx_servicios_activo (activo),
  CONSTRAINT chk_servicio_precio CHECK (precio_base >= 0.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE citas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  mascota_id INT NOT NULL,
  veterinario_id INT NULL,
  fecha_hora DATETIME NOT NULL,
  motivo VARCHAR(500) NOT NULL,
  estado ENUM('Programada', 'Confirmada', 'En Curso', 'Finalizada', 'Cancelada', 'No Asistio') NOT NULL DEFAULT 'Programada',
  notas TEXT NULL,
  fecha_cancelacion DATETIME NULL,
  motivo_cancelacion TEXT NULL,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (mascota_id) REFERENCES mascotas(id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (veterinario_id) REFERENCES veterinarios(id) ON DELETE SET NULL ON UPDATE CASCADE,
  INDEX idx_citas_mascota (mascota_id),
  INDEX idx_citas_veterinario (veterinario_id),
  INDEX idx_citas_fecha (fecha_hora),
  INDEX idx_citas_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE consultas_medicas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  cita_id INT NULL,
  mascota_id INT NOT NULL,
  veterinario_id INT NOT NULL,
  fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  motivo_visita VARCHAR(500) NOT NULL,
  sintomas TEXT NULL,
  diagnostico TEXT NULL,
  tratamiento_indicaciones TEXT NULL,
  recomendaciones TEXT NULL,
  peso_registrado DECIMAL(5,2) NULL,
  temperatura DECIMAL(4,2) NULL,
  costo_consulta DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  FOREIGN KEY (cita_id) REFERENCES citas(id) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (mascota_id) REFERENCES mascotas(id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (veterinario_id) REFERENCES veterinarios(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX idx_consultas_cita (cita_id),
  INDEX idx_consultas_mascota (mascota_id),
  INDEX idx_consultas_veterinario (veterinario_id),
  INDEX idx_consultas_fecha (fecha_hora),
  CONSTRAINT chk_consulta_costo CHECK (costo_consulta >= 0.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE procedimientos_especiales (
  id INT AUTO_INCREMENT PRIMARY KEY,
  consulta_id INT NULL,
  mascota_id INT NOT NULL,
  veterinario_id INT NOT NULL,
  tipo_procedimiento VARCHAR(100) NOT NULL,
  nombre_procedimiento VARCHAR(150) NOT NULL,
  fecha_programada DATETIME NOT NULL,
  fecha_ejecucion DATETIME NULL,
  duracion_estimada_minutos INT NULL,
  estado ENUM('Programado', 'En Proceso', 'Finalizado', 'Cancelado') NOT NULL DEFAULT 'Programado',
  notas_preoperatorias TEXT NULL,
  notas_procedimiento TEXT NULL,
  notas_postoperatorias TEXT NULL,
  seguimiento_postoperatorio TEXT NULL,
  proximo_control DATE NULL,
  costo_base DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  FOREIGN KEY (consulta_id) REFERENCES consultas_medicas(id) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (mascota_id) REFERENCES mascotas(id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (veterinario_id) REFERENCES veterinarios(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX idx_proc_consulta (consulta_id),
  INDEX idx_proc_mascota (mascota_id),
  INDEX idx_proc_veterinario (veterinario_id),
  INDEX idx_proc_fecha_prog (fecha_programada),
  INDEX idx_proc_estado (estado),
  CONSTRAINT chk_proc_costo CHECK (costo_base >= 0.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Inventario y farmacia
CREATE TABLE proveedores (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre_empresa VARCHAR(150) NOT NULL,
  ruc_nit VARCHAR(20) NOT NULL UNIQUE,
  contacto_nombre VARCHAR(150) NULL,
  telefono VARCHAR(20) NULL,
  email VARCHAR(100) NULL,
  direccion VARCHAR(255) NULL,
  sitio_web VARCHAR(255) NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_proveedores_ruc (ruc_nit),
  INDEX idx_proveedores_nombre (nombre_empresa),
  INDEX idx_proveedores_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inventario (
  id INT AUTO_INCREMENT PRIMARY KEY,
  codigo_barras VARCHAR(50) NOT NULL UNIQUE,
  nombre_producto VARCHAR(150) NOT NULL,
  tipo_id INT NOT NULL,
  descripcion TEXT NULL,
  fabricante VARCHAR(100) NOT NULL,
  proveedor_id INT NULL,
  cantidad_stock INT NOT NULL DEFAULT 0,
  stock_minimo INT NOT NULL DEFAULT 1,
  unidad_medida VARCHAR(50) NOT NULL DEFAULT 'unidad',
  precio_compra DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  precio_venta DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  lote VARCHAR(50) NOT NULL,
  fecha_vencimiento DATE NOT NULL,
  requiere_receta BOOLEAN NOT NULL DEFAULT FALSE,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (tipo_id) REFERENCES producto_tipos(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (proveedor_id) REFERENCES proveedores(id) ON DELETE SET NULL ON UPDATE CASCADE,
  INDEX idx_inventario_codigo (codigo_barras),
  INDEX idx_inventario_nombre (nombre_producto),
  INDEX idx_inventario_tipo (tipo_id),
  INDEX idx_inventario_proveedor (proveedor_id),
  INDEX idx_inventario_stock (cantidad_stock),
  INDEX idx_inventario_vencimiento (fecha_vencimiento),
  INDEX idx_inventario_activo (activo),
  CONSTRAINT chk_inv_stock_positivo CHECK (cantidad_stock >= 0),
  CONSTRAINT chk_inv_stock_minimo CHECK (stock_minimo >= 0),
  CONSTRAINT chk_inv_precios CHECK (precio_compra >= 0.00 AND precio_venta >= 0.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inventario_movimientos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  producto_id INT NOT NULL,
  tipo_movimiento ENUM('Entrada', 'Salida', 'Ajuste', 'Vencimiento') NOT NULL,
  cantidad INT NOT NULL,
  stock_anterior INT NOT NULL,
  stock_nuevo INT NOT NULL,
  motivo VARCHAR(255) NULL,
  referencia_id INT NULL,
  referencia_consulta_id INT NULL,
  referencia_procedimiento_id INT NULL,
  usuario VARCHAR(100) NOT NULL DEFAULT 'Sistema',
  fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (producto_id) REFERENCES inventario(id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (referencia_consulta_id) REFERENCES consultas_medicas(id) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (referencia_procedimiento_id) REFERENCES procedimientos_especiales(id) ON DELETE SET NULL ON UPDATE CASCADE,
  INDEX idx_mov_producto (producto_id),
  INDEX idx_mov_tipo (tipo_movimiento),
  INDEX idx_mov_fecha (fecha_hora)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE prescripciones (
  id INT AUTO_INCREMENT PRIMARY KEY,
  consulta_id INT NULL,
  procedimiento_id INT NULL,
  producto_id INT NOT NULL,
  cantidad INT NOT NULL,
  dosis VARCHAR(100) NULL,
  frecuencia VARCHAR(100) NULL,
  duracion_dias INT NULL,
  instrucciones TEXT NULL,
  fecha_prescripcion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (consulta_id) REFERENCES consultas_medicas(id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (procedimiento_id) REFERENCES procedimientos_especiales(id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (producto_id) REFERENCES inventario(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX idx_presc_consulta (consulta_id),
  INDEX idx_presc_procedimiento (procedimiento_id),
  INDEX idx_presc_producto (producto_id),
  INDEX idx_presc_fecha (fecha_prescripcion),
  CONSTRAINT chk_presc_cantidad CHECK (cantidad > 0),
  CONSTRAINT chk_presc_referencia CHECK (consulta_id IS NOT NULL OR procedimiento_id IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE procedimiento_insumos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  procedimiento_id INT NOT NULL,
  producto_id INT NOT NULL,
  cantidad_utilizada INT NOT NULL,
  fecha_uso DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  observaciones VARCHAR(255) NULL,
  FOREIGN KEY (procedimiento_id) REFERENCES procedimientos_especiales(id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (producto_id) REFERENCES inventario(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX idx_proc_insumos_proc (procedimiento_id),
  INDEX idx_proc_insumos_prod (producto_id),
  CONSTRAINT chk_insumo_cantidad CHECK (cantidad_utilizada > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE historico_precios_inventario (
  id INT AUTO_INCREMENT PRIMARY KEY,
  producto_id INT NOT NULL,
  precio_anterior DECIMAL(10,2) NOT NULL,
  precio_nuevo DECIMAL(10,2) NOT NULL,
  tipo_precio ENUM('Compra', 'Venta') NOT NULL,
  fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  motivo VARCHAR(255) NULL,
  FOREIGN KEY (producto_id) REFERENCES inventario(id) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_hist_precios_prod (producto_id),
  INDEX idx_hist_precios_fecha (fecha_modificacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE historial_medico (
  id INT AUTO_INCREMENT PRIMARY KEY,
  mascota_id INT NOT NULL,
  fecha_evento DATE NOT NULL,
  evento_tipo_id INT NOT NULL,
  descripcion TEXT NOT NULL,
  diagnostico TEXT NULL,
  tratamiento_recomendado TEXT NULL,
  veterinario_id INT NULL,
  consulta_id INT NULL,
  procedimiento_id INT NULL,
  FOREIGN KEY (mascota_id) REFERENCES mascotas(id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (evento_tipo_id) REFERENCES evento_tipos(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (veterinario_id) REFERENCES veterinarios(id) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (consulta_id) REFERENCES consultas_medicas(id) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (procedimiento_id) REFERENCES procedimientos_especiales(id) ON DELETE SET NULL ON UPDATE CASCADE,
  INDEX idx_hist_med_mascota (mascota_id),
  INDEX idx_hist_med_fecha (fecha_evento),
  INDEX idx_hist_med_tipo (evento_tipo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Facturacion
CREATE TABLE facturas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  numero_factura VARCHAR(50) NOT NULL UNIQUE,
  dueno_id INT NOT NULL,
  fecha_emision DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  descuento DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  impuesto DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  metodo_pago ENUM('Efectivo', 'Tarjeta', 'Transferencia', 'Mixto') NOT NULL DEFAULT 'Efectivo',
  estado ENUM('Pendiente', 'Pagada', 'Anulada') NOT NULL DEFAULT 'Pendiente',
  observaciones TEXT NULL,
  FOREIGN KEY (dueno_id) REFERENCES duenos(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX idx_facturas_numero (numero_factura),
  INDEX idx_facturas_dueno (dueno_id),
  INDEX idx_facturas_fecha (fecha_emision),
  INDEX idx_facturas_estado (estado),
  CONSTRAINT chk_factura_subtotal CHECK (subtotal >= 0.00),
  CONSTRAINT chk_factura_descuento CHECK (descuento >= 0.00),
  CONSTRAINT chk_factura_impuesto CHECK (impuesto >= 0.00),
  CONSTRAINT chk_factura_total CHECK (total >= 0.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE factura_detalles (
  id INT AUTO_INCREMENT PRIMARY KEY,
  factura_id INT NOT NULL,
  tipo_item ENUM('Producto', 'Servicio') NOT NULL,
  producto_id INT NULL,
  servicio_id INT NULL,
  referencia_id INT NULL,
  descripcion VARCHAR(255) NOT NULL,
  cantidad INT NOT NULL DEFAULT 1,
  precio_unitario DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  FOREIGN KEY (factura_id) REFERENCES facturas(id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (producto_id) REFERENCES inventario(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (servicio_id) REFERENCES servicios(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX idx_fac_det_factura (factura_id),
  INDEX idx_fac_det_producto (producto_id),
  INDEX idx_fac_det_servicio (servicio_id),
  CONSTRAINT chk_fac_det_cantidad CHECK (cantidad > 0),
  CONSTRAINT chk_fac_det_precio CHECK (precio_unitario >= 0.00),
  CONSTRAINT chk_fac_det_subtotal CHECK (subtotal >= 0.00),
  CONSTRAINT chk_fac_det_tipo CHECK (
    (tipo_item = 'Producto' AND producto_id IS NOT NULL) OR
    (tipo_item = 'Servicio' AND servicio_id IS NOT NULL)
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Actividades especiales
CREATE TABLE mascotas_adopcion (
  id INT AUTO_INCREMENT PRIMARY KEY,
  mascota_id INT NOT NULL,
  historia TEXT NULL,
  temperamento VARCHAR(255) NULL,
  necesidades_especiales TEXT NULL,
  fecha_ingreso DATE NOT NULL,
  estado ENUM('Disponible', 'En Proceso', 'Adoptada', 'Retirada') NOT NULL DEFAULT 'Disponible',
  fecha_adopcion DATE NULL,
  dueno_adoptante_id INT NULL,
  foto_adicional_url VARCHAR(255) NULL,
  FOREIGN KEY (mascota_id) REFERENCES mascotas(id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (dueno_adoptante_id) REFERENCES duenos(id) ON DELETE SET NULL ON UPDATE CASCADE,
  INDEX idx_adop_mascota (mascota_id),
  INDEX idx_adop_estado (estado),
  INDEX idx_adop_fecha_ingreso (fecha_ingreso),
  INDEX idx_adop_adoptante (dueno_adoptante_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE adopciones (
  id INT AUTO_INCREMENT PRIMARY KEY,
  mascota_adopcion_id INT NOT NULL,
  dueno_id INT NOT NULL,
  fecha_adopcion DATE NOT NULL,
  contrato_texto TEXT NULL,
  condiciones_especiales TEXT NULL,
  seguimiento_requerido BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_primer_seguimiento DATE NULL,
  FOREIGN KEY (mascota_adopcion_id) REFERENCES mascotas_adopcion(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (dueno_id) REFERENCES duenos(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX idx_adopciones_mascota (mascota_adopcion_id),
  INDEX idx_adopciones_dueno (dueno_id),
  INDEX idx_adopciones_fecha (fecha_adopcion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE jornadas_vacunacion (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL,
  fecha DATE NOT NULL,
  hora_inicio TIME NULL,
  hora_fin TIME NULL,
  ubicacion VARCHAR(255) NULL,
  descripcion TEXT NULL,
  capacidad_maxima INT NULL,
  estado ENUM('Planificada', 'En Curso', 'Finalizada', 'Cancelada') NOT NULL DEFAULT 'Planificada',
  INDEX idx_jornadas_fecha (fecha),
  INDEX idx_jornadas_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE registro_jornada_vacunacion (
  id INT AUTO_INCREMENT PRIMARY KEY,
  jornada_id INT NOT NULL,
  mascota_id INT NULL,
  dueno_id INT NULL,
  nombre_mascota_temporal VARCHAR(100) NULL,
  especie_id INT NOT NULL,
  es_callejero_comunitario BOOLEAN NOT NULL DEFAULT FALSE,
  nombre_responsable_temporal VARCHAR(150) NULL,
  telefono_responsable_temporal VARCHAR(50) NULL,
  vacuna_id INT NOT NULL,
  veterinario_id INT NULL,
  fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  lote_vacuna VARCHAR(50) NOT NULL,
  proxima_dosis DATE NULL,
  observaciones TEXT NULL,
  FOREIGN KEY (jornada_id) REFERENCES jornadas_vacunacion(id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (mascota_id) REFERENCES mascotas(id) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (dueno_id) REFERENCES duenos(id) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (especie_id) REFERENCES especies(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (vacuna_id) REFERENCES inventario(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (veterinario_id) REFERENCES veterinarios(id) ON DELETE SET NULL ON UPDATE CASCADE,
  INDEX idx_reg_jornada (jornada_id),
  INDEX idx_reg_mascota (mascota_id),
  INDEX idx_reg_dueno (dueno_id),
  INDEX idx_reg_especie (especie_id),
  INDEX idx_reg_vacuna (vacuna_id),
  INDEX idx_reg_veterinario (veterinario_id),
  INDEX idx_reg_fecha (fecha_hora),
  CONSTRAINT chk_identificacion_mascota CHECK (mascota_id IS NOT NULL OR nombre_mascota_temporal IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE club_mascotas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  dueno_id INT NOT NULL UNIQUE,
  puntos_acumulados INT NOT NULL DEFAULT 0,
  puntos_canjeados INT NOT NULL DEFAULT 0,
  puntos_disponibles INT NOT NULL DEFAULT 0,
  nivel ENUM('Bronce', 'Plata', 'Oro', 'Platino') NOT NULL DEFAULT 'Bronce',
  fecha_inscripcion DATE NOT NULL,
  fecha_ultima_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  FOREIGN KEY (dueno_id) REFERENCES duenos(id) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_club_dueno (dueno_id),
  INDEX idx_club_nivel (nivel),
  INDEX idx_club_puntos (puntos_disponibles),
  INDEX idx_club_activo (activo),
  CONSTRAINT chk_puntos_no_negativos CHECK (
    puntos_acumulados >= 0 AND puntos_canjeados >= 0 AND puntos_disponibles >= 0
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE transacciones_puntos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  club_mascotas_id INT NOT NULL,
  factura_id INT NULL,
  puntos INT NOT NULL,
  tipo ENUM('Ganados', 'Canjeados', 'Expirados', 'Ajuste') NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  descripcion VARCHAR(255) NULL,
  saldo_anterior INT NOT NULL,
  saldo_nuevo INT NOT NULL,
  FOREIGN KEY (club_mascotas_id) REFERENCES club_mascotas(id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (factura_id) REFERENCES facturas(id) ON DELETE SET NULL ON UPDATE CASCADE,
  INDEX idx_trans_club (club_mascotas_id),
  INDEX idx_trans_factura (factura_id),
  INDEX idx_trans_fecha (fecha),
  INDEX idx_trans_tipo (tipo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE beneficios_club (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL,
  descripcion TEXT NULL,
  nivel_requerido ENUM('Bronce', 'Plata', 'Oro', 'Platino') NOT NULL,
  puntos_necesarios INT NOT NULL,
  tipo_beneficio ENUM('Descuento', 'Servicio Gratis', 'Producto Gratis', 'Puntos Extra') NOT NULL,
  valor_beneficio DECIMAL(10,2) NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  INDEX idx_ben_nivel (nivel_requerido),
  INDEX idx_ben_puntos (puntos_necesarios),
  INDEX idx_ben_activo (activo),
  CONSTRAINT chk_ben_puntos CHECK (puntos_necesarios > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE canjes_beneficios (
  id INT AUTO_INCREMENT PRIMARY KEY,
  club_mascotas_id INT NOT NULL,
  beneficio_id INT NOT NULL,
  fecha_canje DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  puntos_canjeados INT NOT NULL,
  estado ENUM('Pendiente', 'Aplicado', 'Expirado') NOT NULL DEFAULT 'Pendiente',
  fecha_expiracion DATE NULL,
  factura_id INT NULL,
  FOREIGN KEY (club_mascotas_id) REFERENCES club_mascotas(id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (beneficio_id) REFERENCES beneficios_club(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (factura_id) REFERENCES facturas(id) ON DELETE SET NULL ON UPDATE CASCADE,
  INDEX idx_canje_club (club_mascotas_id),
  INDEX idx_canje_beneficio (beneficio_id),
  INDEX idx_canje_fecha (fecha_canje),
  INDEX idx_canje_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Alertas del sistema
CREATE TABLE alertas_sistema (
  id INT AUTO_INCREMENT PRIMARY KEY,
  tipo_alerta ENUM('STOCK_MINIMO', 'PRODUCTO_VENCIDO', 'PRODUCTO_POR_VENCER', 'CITA_PENDIENTE', 'GENERAL') NOT NULL,
  nivel_severidad ENUM('Informativa', 'Advertencia', 'Critica') NOT NULL,
  referencia_tabla VARCHAR(50) NULL,
  referencia_id INT NULL,
  mensaje TEXT NOT NULL,
  fecha_generacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  leida BOOLEAN NOT NULL DEFAULT FALSE,
  fecha_atencion DATETIME NULL,
  INDEX idx_alertas_tipo (tipo_alerta),
  INDEX idx_alertas_severidad (nivel_severidad),
  INDEX idx_alertas_leida (leida),
  INDEX idx_alertas_fecha (fecha_generacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
