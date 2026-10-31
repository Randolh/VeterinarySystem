package randolh.veterinarysystem.consulta;

import randolh.veterinarysystem.core.config.ConexionBD;
import randolh.veterinarysystem.core.config.TransaccionManager;
import randolh.veterinarysystem.core.exception.BaseDatosException;
import randolh.veterinarysystem.core.exception.InventarioException;
import randolh.veterinarysystem.core.patterns.observer.GestorEventos;
import randolh.veterinarysystem.core.utils.LoggerManager;
import randolh.veterinarysystem.inventario.Producto;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Acceso a datos para consultas clinicas, cirugias, recetas y deduccion atomica de inventario
public class ConsultaDAO {

    private static final String MODULO = "ConsultaDAO";

    private static final String SELECT_CONSULTA_BASE =
            "SELECT cm.id, cm.mascota_id, cm.veterinario_id, cm.cita_id, cm.fecha_hora, cm.motivo_visita AS motivo, " +
            "cm.sintomas, cm.diagnostico, cm.recomendaciones, cm.tratamiento_indicaciones AS observaciones, cm.peso_registrado, " +
            "cm.temperatura, m.nombre AS mascota_nombre, d.nombre_completo AS dueno_nombre, " +
            "v.nombre_completo AS veterinario_nombre " +
            "FROM consultas_medicas cm " +
            "INNER JOIN mascotas m ON cm.mascota_id = m.id " +
            "INNER JOIN duenos d ON m.dueno_id = d.id " +
            "INNER JOIN veterinarios v ON cm.veterinario_id = v.id ";

    private static final String SELECT_PROCEDIMIENTO_BASE =
            "SELECT pe.id, pe.mascota_id, pe.veterinario_id, pe.tipo_procedimiento, pe.nombre_procedimiento, " +
            "pe.fecha_programada AS fecha_hora, pe.duracion_estimada_minutos, pe.notas_preoperatorias AS informacion_preoperatoria, " +
            "pe.notas_procedimiento AS detalle_procedimiento, pe.notas_postoperatorias AS complicaciones, pe.seguimiento_postoperatorio, " +
            "pe.proximo_control, pe.estado, pe.costo_base AS costo_procedimiento, " +
            "m.nombre AS mascota_nombre, d.nombre_completo AS dueno_nombre, " +
            "v.nombre_completo AS veterinario_nombre " +
            "FROM procedimientos_especiales pe " +
            "INNER JOIN mascotas m ON pe.mascota_id = m.id " +
            "INNER JOIN duenos d ON m.dueno_id = d.id " +
            "INNER JOIN veterinarios v ON pe.veterinario_id = v.id ";

    // ==========================================
    // CONSULTAS MEDICAS Y DEDUCCION ATOMICA
    // ==========================================

    // Registra consulta medica y deduce medicamentos de inventario de forma atomica ACID
    public ConsultaMedica registrarConsultaConDeduccion(ConsultaMedica c, List<Prescripcion> prescripciones) {
        return TransaccionManager.ejecutarEnTransaccion(conn -> {
            // 1. Insertar consulta medica
            String sqlConsulta = "INSERT INTO consultas_medicas (mascota_id, veterinario_id, cita_id, fecha_hora, " +
                    "motivo_visita, sintomas, diagnostico, recomendaciones, tratamiento_indicaciones, peso_registrado, temperatura) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sqlConsulta, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, c.getMascotaId());
                stmt.setInt(2, c.getVeterinarioId());
                if (c.getCitaId() != null) {
                    stmt.setInt(3, c.getCitaId());
                } else {
                    stmt.setNull(3, Types.INTEGER);
                }
                stmt.setTimestamp(4, Timestamp.valueOf(c.getFechaHora()));
                stmt.setString(5, c.getMotivo());
                stmt.setString(6, c.getSintomas());
                stmt.setString(7, c.getDiagnostico());
                stmt.setString(8, c.getRecomendaciones());
                stmt.setString(9, c.getObservaciones());

                if (c.getPesoRegistrado() != null) {
                    stmt.setBigDecimal(10, c.getPesoRegistrado());
                } else {
                    stmt.setNull(10, Types.DECIMAL);
                }

                if (c.getTemperatura() != null) {
                    stmt.setBigDecimal(11, c.getTemperatura());
                } else {
                    stmt.setNull(11, Types.DECIMAL);
                }

                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        c.setId(rs.getInt(1));
                    }
                }
            }

            // 2. Actualizar peso actual de la mascota en su ficha
            if (c.getPesoRegistrado() != null) {
                String sqlPeso = "UPDATE mascotas SET peso_actual = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlPeso)) {
                    stmt.setBigDecimal(1, c.getPesoRegistrado());
                    stmt.setInt(2, c.getMascotaId());
                    stmt.executeUpdate();
                }
            }

            // 3. Procesar prescripciones y deducir inventario
            if (prescripciones != null && !prescripciones.isEmpty()) {
                for (Prescripcion p : prescripciones) {
                    deducirMedicamentoPrescrito(conn, c.getId(), p);
                }
            }

            // 4. Registrar en historial medico (Evento tipo 1: Consulta General)
            registrarEnHistorialMedico(conn, c.getMascotaId(), c.getFechaHora().toLocalDate(), 1,
                    c.getMotivo(), c.getDiagnostico(), c.getRecomendaciones(),
                    c.getVeterinarioId(), c.getId(), null);

            // 5. Si provenia de una cita agendada, pasar cita a Finalizada
            if (c.getCitaId() != null) {
                String sqlCita = "UPDATE citas SET estado = 'Finalizada' WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlCita)) {
                    stmt.setInt(1, c.getCitaId());
                    stmt.executeUpdate();
                }
            }

            LoggerManager.info(MODULO, "Consulta medica ID " + c.getId() + " registrada con exito y stock deducido.");
            return c;
        });
    }

    // Deduce un medicamento prescrito validando stock y fecha de caducidad
    private void deducirMedicamentoPrescrito(Connection conn, int consultaId, Prescripcion p) throws SQLException {
        // Bloqueo y lectura del producto
        String sqlCheck = "SELECT nombre_producto, cantidad_stock, stock_minimo, fecha_vencimiento, activo FROM inventario WHERE id = ? FOR UPDATE";
        String nombreProd;
        int stockActual;
        int stockMinimo;
        Date fechaVenc;
        boolean activo;

        try (PreparedStatement stmt = conn.prepareStatement(sqlCheck)) {
            stmt.setInt(1, p.getProductoId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new InventarioException("El medicamento solicitado con ID " + p.getProductoId() + " no existe.");
                }
                nombreProd = rs.getString("nombre_producto");
                stockActual = rs.getInt("cantidad_stock");
                stockMinimo = rs.getInt("stock_minimo");
                fechaVenc = rs.getDate("fecha_vencimiento");
                activo = rs.getBoolean("activo");
            }
        }

        if (!activo) {
            throw new InventarioException("El producto '" + nombreProd + "' se encuentra inactivo en catalogo.");
        }

        // Validacion de fecha de vencimiento
        if (fechaVenc != null && fechaVenc.toLocalDate().isBefore(LocalDate.now())) {
            throw new InventarioException(String.format(
                    "Operacion cancelada: El medicamento '%s' se encuentra VENCIDO desde el %s y no puede dispensarse.",
                    nombreProd, fechaVenc.toLocalDate()));
        }

        // Validacion estricta de stock disponible
        if (stockActual < p.getCantidad()) {
            throw new InventarioException(String.format(
                    "Stock insuficiente para '%s'. Solicitado en receta: %d, Disponible en farmacia: %d",
                    nombreProd, p.getCantidad(), stockActual));
        }

        int stockNuevo = stockActual - p.getCantidad();

        // Actualizar stock en inventario
        String sqlUpdateStock = "UPDATE inventario SET cantidad_stock = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateStock)) {
            stmt.setInt(1, stockNuevo);
            stmt.setInt(2, p.getProductoId());
            stmt.executeUpdate();
        }

        // Insertar prescripcion
        String sqlPrescripcion = "INSERT INTO prescripciones (consulta_id, producto_id, cantidad, dosis, " +
                "frecuencia, duracion_dias, instrucciones, fecha_prescripcion) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlPrescripcion, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, consultaId);
            stmt.setInt(2, p.getProductoId());
            stmt.setInt(3, p.getCantidad());
            stmt.setString(4, p.getDosis());
            stmt.setString(5, p.getFrecuencia());
            if (p.getDuracionDias() != null) {
                stmt.setInt(6, p.getDuracionDias());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            stmt.setString(7, p.getInstrucciones());
            stmt.setTimestamp(8, Timestamp.valueOf(p.getFechaPrescripcion() != null ? p.getFechaPrescripcion() : LocalDateTime.now()));

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }
        }

        // Registrar movimiento de kardex
        String sqlKardex = "INSERT INTO inventario_movimientos (producto_id, tipo_movimiento, cantidad, " +
                "stock_anterior, stock_nuevo, motivo, referencia_consulta_id, usuario, fecha_hora) " +
                "VALUES (?, 'Salida', ?, ?, ?, ?, ?, 'Sistema', NOW())";
        try (PreparedStatement stmt = conn.prepareStatement(sqlKardex)) {
            stmt.setInt(1, p.getProductoId());
            stmt.setInt(2, p.getCantidad());
            stmt.setInt(3, stockActual);
            stmt.setInt(4, stockNuevo);
            stmt.setString(5, "Deduccion automatica por prescripcion medica en consulta #" + consultaId);
            stmt.setInt(6, consultaId);
            stmt.executeUpdate();
        }

        // Notificar al patron Observer si cayo bajo el stock minimo
        if (stockNuevo <= stockMinimo) {
            Producto prodAlerta = new Producto();
            prodAlerta.setId(p.getProductoId());
            prodAlerta.setNombreProducto(nombreProd);
            prodAlerta.setCantidadStock(stockNuevo);
            prodAlerta.setStockMinimo(stockMinimo);
            GestorEventos.getInstancia().notificar("INVENTARIO_STOCK_MINIMO", prodAlerta);
        }
    }

    // ==========================================
    // PROCEDIMIENTOS QUIRURGICOS E INSUMOS
    // ==========================================

    // Registra procedimiento quirurgico y deduce insumos utilizados de forma atomica
    public ProcedimientoEspecial registrarProcedimientoConInsumos(ProcedimientoEspecial proc, List<InsumoProcedimiento> insumos) {
        return TransaccionManager.ejecutarEnTransaccion(conn -> {
            // 1. Insertar procedimiento especial
            String sqlProc = "INSERT INTO procedimientos_especiales (mascota_id, veterinario_id, tipo_procedimiento, " +
                    "nombre_procedimiento, fecha_programada, duracion_estimada_minutos, notas_preoperatorias, " +
                    "notas_procedimiento, notas_postoperatorias, seguimiento_postoperatorio, proximo_control, " +
                    "estado, costo_base) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sqlProc, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, proc.getMascotaId());
                stmt.setInt(2, proc.getVeterinarioId());
                stmt.setString(3, proc.getTipoProcedimiento());
                stmt.setString(4, proc.getNombreProcedimiento());
                stmt.setTimestamp(5, Timestamp.valueOf(proc.getFechaHora()));

                if (proc.getDuracionEstimadaMinutos() != null) {
                    stmt.setInt(6, proc.getDuracionEstimadaMinutos());
                } else {
                    stmt.setNull(6, Types.INTEGER);
                }

                stmt.setString(7, proc.getInformacionPreoperatoria());
                stmt.setString(8, proc.getDetalleProcedimiento());
                stmt.setString(9, proc.getComplicaciones());
                stmt.setString(10, proc.getSeguimientoPostoperatorio());

                if (proc.getProximoControl() != null) {
                    stmt.setDate(11, Date.valueOf(proc.getProximoControl()));
                } else {
                    stmt.setNull(11, Types.DATE);
                }

                stmt.setString(12, proc.getEstado().getValorDb());
                stmt.setBigDecimal(13, proc.getCostoProcedimiento());

                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        proc.setId(rs.getInt(1));
                    }
                }
            }

            // 2. Procesar insumos quirurgicos y deducir stock
            if (insumos != null && !insumos.isEmpty()) {
                for (InsumoProcedimiento ins : insumos) {
                    deducirInsumoQuirurgico(conn, proc.getId(), ins);
                }
            }

            // 3. Registrar en historial medico (Evento tipo 3: Cirugia)
            registrarEnHistorialMedico(conn, proc.getMascotaId(), proc.getFechaHora().toLocalDate(), 3,
                    proc.getNombreProcedimiento() + " (" + proc.getTipoProcedimiento() + ")",
                    proc.getDetalleProcedimiento(), proc.getSeguimientoPostoperatorio(),
                    proc.getVeterinarioId(), null, proc.getId());

            LoggerManager.info(MODULO, "Procedimiento especial ID " + proc.getId() + " registrado con deduccion de insumos.");
            return proc;
        });
    }

    // Deduce insumo consumido en quirofano
    private void deducirInsumoQuirurgico(Connection conn, int procedimientoId, InsumoProcedimiento ins) throws SQLException {
        String sqlCheck = "SELECT nombre_producto, cantidad_stock, stock_minimo, fecha_vencimiento, activo FROM inventario WHERE id = ? FOR UPDATE";
        String nombreProd;
        int stockActual;
        int stockMinimo;
        Date fechaVenc;
        boolean activo;

        try (PreparedStatement stmt = conn.prepareStatement(sqlCheck)) {
            stmt.setInt(1, ins.getProductoId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new InventarioException("El insumo con ID " + ins.getProductoId() + " no existe.");
                }
                nombreProd = rs.getString("nombre_producto");
                stockActual = rs.getInt("cantidad_stock");
                stockMinimo = rs.getInt("stock_minimo");
                fechaVenc = rs.getDate("fecha_vencimiento");
                activo = rs.getBoolean("activo");
            }
        }

        if (!activo) {
            throw new InventarioException("El insumo '" + nombreProd + "' se encuentra inactivo.");
        }

        if (fechaVenc != null && fechaVenc.toLocalDate().isBefore(LocalDate.now())) {
            throw new InventarioException(String.format(
                    "Operacion cancelada: El insumo quirurgico '%s' se encuentra VENCIDO desde el %s.",
                    nombreProd, fechaVenc.toLocalDate()));
        }

        if (stockActual < ins.getCantidadUsada()) {
            throw new InventarioException(String.format(
                    "Stock insuficiente para insumo quirurgico '%s'. Requerido: %d, Disponible: %d",
                    nombreProd, ins.getCantidadUsada(), stockActual));
        }

        int stockNuevo = stockActual - ins.getCantidadUsada();

        // Actualizar stock
        String sqlUpdateStock = "UPDATE inventario SET cantidad_stock = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateStock)) {
            stmt.setInt(1, stockNuevo);
            stmt.setInt(2, ins.getProductoId());
            stmt.executeUpdate();
        }

        // Insertar registro de insumo
        String sqlInsumo = "INSERT INTO procedimiento_insumos (procedimiento_id, producto_id, cantidad_utilizada, observaciones) " +
                "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlInsumo, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, procedimientoId);
            stmt.setInt(2, ins.getProductoId());
            stmt.setInt(3, ins.getCantidadUsada());
            stmt.setString(4, ins.getObservaciones());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    ins.setId(rs.getInt(1));
                }
            }
        }

        // Registrar en kardex
        String sqlKardex = "INSERT INTO inventario_movimientos (producto_id, tipo_movimiento, cantidad, " +
                "stock_anterior, stock_nuevo, motivo, referencia_procedimiento_id, usuario, fecha_hora) " +
                "VALUES (?, 'Salida', ?, ?, ?, ?, ?, 'Sistema', NOW())";
        try (PreparedStatement stmt = conn.prepareStatement(sqlKardex)) {
            stmt.setInt(1, ins.getProductoId());
            stmt.setInt(2, ins.getCantidadUsada());
            stmt.setInt(3, stockActual);
            stmt.setInt(4, stockNuevo);
            stmt.setString(5, "Deduccion automatica por insumo quirurgico en procedimiento #" + procedimientoId);
            stmt.setInt(6, procedimientoId);
            stmt.executeUpdate();
        }

        // Notificar al patron Observer si cayo bajo el stock minimo
        if (stockNuevo <= stockMinimo) {
            Producto prodAlerta = new Producto();
            prodAlerta.setId(ins.getProductoId());
            prodAlerta.setNombreProducto(nombreProd);
            prodAlerta.setCantidadStock(stockNuevo);
            prodAlerta.setStockMinimo(stockMinimo);
            GestorEventos.getInstancia().notificar("INVENTARIO_STOCK_MINIMO", prodAlerta);
        }
    }

    // Registra entrada de auditoria en historial medico
    private void registrarEnHistorialMedico(Connection conn, int mascotaId, LocalDate fecha,
                                           int eventoTipoId, String descripcion, String diagnostico,
                                           String tratamiento, Integer vetId, Integer consultaId,
                                           Integer procedimientoId) throws SQLException {
        String sql = "INSERT INTO historial_medico (mascota_id, fecha_evento, evento_tipo_id, descripcion, " +
                "diagnostico, tratamiento_recomendado, veterinario_id, consulta_id, procedimiento_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mascotaId);
            stmt.setDate(2, Date.valueOf(fecha));
            stmt.setInt(3, eventoTipoId);
            stmt.setString(4, descripcion != null ? descripcion : "Evento clinico");
            stmt.setString(5, diagnostico);
            stmt.setString(6, tratamiento);

            if (vetId != null) {
                stmt.setInt(7, vetId);
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            if (consultaId != null) {
                stmt.setInt(8, consultaId);
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            if (procedimientoId != null) {
                stmt.setInt(9, procedimientoId);
            } else {
                stmt.setNull(9, Types.INTEGER);
            }

            stmt.executeUpdate();
        }
    }

    // ==========================================
    // METODOS DE CONSULTA Y BUSQUEDA
    // ==========================================

    // Busca consulta por ID
    public Optional<ConsultaMedica> buscarConsultaPorId(int id) {
        String sql = SELECT_CONSULTA_BASE + "WHERE cm.id = ?";
        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ConsultaMedica c = mapearConsulta(rs);
                    c.setPrescripciones(listarPrescripcionesPorConsulta(c.getId()));
                    return Optional.of(c);
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar consulta ID: " + id, e);
            throw new BaseDatosException("Error al consultar atencion medica: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Lista consultas de una mascota
    public List<ConsultaMedica> listarConsultasPorMascota(int mascotaId) {
        List<ConsultaMedica> lista = new ArrayList<>();
        String sql = SELECT_CONSULTA_BASE + "WHERE cm.mascota_id = ? ORDER BY cm.fecha_hora DESC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mascotaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearConsulta(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar consultas de mascota ID: " + mascotaId, e);
            throw new BaseDatosException("Error al consultar atenciones: " + e.getMessage(), e);
        }
        return lista;
    }

    // Busca procedimiento por ID
    public Optional<ProcedimientoEspecial> buscarProcedimientoPorId(int id) {
        String sql = SELECT_PROCEDIMIENTO_BASE + "WHERE pe.id = ?";
        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ProcedimientoEspecial p = mapearProcedimiento(rs);
                    p.setInsumos(listarInsumosPorProcedimiento(p.getId()));
                    return Optional.of(p);
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar procedimiento ID: " + id, e);
            throw new BaseDatosException("Error al consultar procedimiento: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Lista procedimientos de una mascota
    public List<ProcedimientoEspecial> listarProcedimientosPorMascota(int mascotaId) {
        List<ProcedimientoEspecial> lista = new ArrayList<>();
        String sql = SELECT_PROCEDIMIENTO_BASE + "WHERE pe.mascota_id = ? ORDER BY pe.fecha_programada DESC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mascotaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProcedimiento(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar procedimientos de mascota ID: " + mascotaId, e);
            throw new BaseDatosException("Error al consultar cirugias: " + e.getMessage(), e);
        }
        return lista;
    }

    // Actualiza estado de un procedimiento
    public boolean actualizarEstadoProcedimiento(int procId, EstadoProcedimientoEnum nuevoEstado) {
        String sql = "UPDATE procedimientos_especiales SET estado = ? WHERE id = ?";
        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nuevoEstado.getValorDb());
            stmt.setInt(2, procId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al actualizar estado de procedimiento ID: " + procId, e);
            throw new BaseDatosException("Error al actualizar estado del procedimiento: " + e.getMessage(), e);
        }
    }

    // Lista prescripciones asociadas a una consulta
    public List<Prescripcion> listarPrescripcionesPorConsulta(int consultaId) {
        List<Prescripcion> lista = new ArrayList<>();
        String sql = "SELECT p.id, p.consulta_id, p.procedimiento_id, p.producto_id, p.cantidad, " +
                "p.dosis, p.frecuencia, p.duracion_dias, p.instrucciones, p.fecha_prescripcion, " +
                "i.nombre_producto, i.unidad_medida " +
                "FROM prescripciones p " +
                "INNER JOIN inventario i ON p.producto_id = i.id " +
                "WHERE p.consulta_id = ? ORDER BY p.id ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, consultaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Prescripcion pres = new Prescripcion(
                            rs.getInt("id"),
                            rs.getInt("consulta_id"),
                            null,
                            rs.getInt("producto_id"),
                            rs.getInt("cantidad"),
                            rs.getString("dosis"),
                            rs.getString("frecuencia"),
                            rs.getInt("duracion_dias"),
                            rs.getString("instrucciones"),
                            rs.getTimestamp("fecha_prescripcion").toLocalDateTime()
                    );
                    pres.setNombreProducto(rs.getString("nombre_producto"));
                    pres.setUnidadMedida(rs.getString("unidad_medida"));
                    lista.add(pres);
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar prescripciones de consulta ID: " + consultaId, e);
            throw new BaseDatosException("Error al consultar prescripciones: " + e.getMessage(), e);
        }
        return lista;
    }

    // Lista insumos consumidos en un procedimiento
    public List<InsumoProcedimiento> listarInsumosPorProcedimiento(int procId) {
        List<InsumoProcedimiento> lista = new ArrayList<>();
        String sql = "SELECT ip.id, ip.procedimiento_id, ip.producto_id, ip.cantidad_utilizada AS cantidad_usada, ip.observaciones, " +
                "i.nombre_producto, i.unidad_medida " +
                "FROM procedimiento_insumos ip " +
                "INNER JOIN inventario i ON ip.producto_id = i.id " +
                "WHERE ip.procedimiento_id = ? ORDER BY ip.id ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, procId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    InsumoProcedimiento ins = new InsumoProcedimiento(
                            rs.getInt("id"),
                            rs.getInt("procedimiento_id"),
                            rs.getInt("producto_id"),
                            rs.getInt("cantidad_usada"),
                            rs.getString("observaciones")
                    );
                    ins.setNombreProducto(rs.getString("nombre_producto"));
                    ins.setUnidadMedida(rs.getString("unidad_medida"));
                    lista.add(ins);
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar insumos de procedimiento ID: " + procId, e);
            throw new BaseDatosException("Error al consultar insumos de cirugia: " + e.getMessage(), e);
        }
        return lista;
    }

    // Lista historial consolidado del expediente clinico de una mascota
    public List<HistorialMedico> listarHistorialCompletoMascota(int mascotaId) {
        List<HistorialMedico> lista = new ArrayList<>();
        String sql = "SELECT hm.id, hm.mascota_id, hm.fecha_evento, hm.evento_tipo_id, hm.descripcion, " +
                "hm.diagnostico, hm.tratamiento_recomendado, hm.veterinario_id, hm.consulta_id, " +
                "hm.procedimiento_id, et.nombre AS evento_nombre, v.nombre_completo AS veterinario_nombre " +
                "FROM historial_medico hm " +
                "INNER JOIN evento_tipos et ON hm.evento_tipo_id = et.id " +
                "LEFT JOIN veterinarios v ON hm.veterinario_id = v.id " +
                "WHERE hm.mascota_id = ? ORDER BY hm.fecha_evento DESC, hm.id DESC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mascotaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date fEv = rs.getDate("fecha_evento");
                    int vId = rs.getInt("veterinario_id");
                    Integer vIdObj = rs.wasNull() ? null : vId;
                    int cId = rs.getInt("consulta_id");
                    Integer cIdObj = rs.wasNull() ? null : cId;
                    int pId = rs.getInt("procedimiento_id");
                    Integer pIdObj = rs.wasNull() ? null : pId;

                    HistorialMedico h = new HistorialMedico(
                            rs.getInt("id"),
                            rs.getInt("mascota_id"),
                            fEv != null ? fEv.toLocalDate() : null,
                            rs.getInt("evento_tipo_id"),
                            rs.getString("descripcion"),
                            rs.getString("diagnostico"),
                            rs.getString("tratamiento_recomendado"),
                            vIdObj,
                            cIdObj,
                            pIdObj
                    );
                    h.setNombreEventoTipo(rs.getString("evento_nombre"));
                    h.setNombreVeterinario(rs.getString("veterinario_nombre"));
                    lista.add(h);
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar historial clinico de mascota ID: " + mascotaId, e);
            throw new BaseDatosException("Error al consultar historial clinico: " + e.getMessage(), e);
        }
        return lista;
    }

    // ==========================================
    // MAPEO DE RESULTSETS
    // ==========================================

    private ConsultaMedica mapearConsulta(ResultSet rs) throws SQLException {
        Timestamp fHora = rs.getTimestamp("fecha_hora");
        int citaId = rs.getInt("cita_id");
        Integer citaIdObj = rs.wasNull() ? null : citaId;

        ConsultaMedica c = new ConsultaMedica(
                rs.getInt("id"),
                rs.getInt("mascota_id"),
                rs.getInt("veterinario_id"),
                citaIdObj,
                fHora != null ? fHora.toLocalDateTime() : null,
                rs.getString("motivo"),
                rs.getString("sintomas"),
                rs.getString("diagnostico"),
                rs.getString("recomendaciones"),
                rs.getString("observaciones"),
                rs.getBigDecimal("peso_registrado"),
                rs.getBigDecimal("temperatura")
        );
        c.setNombreMascota(rs.getString("mascota_nombre"));
        c.setNombreDueno(rs.getString("dueno_nombre"));
        c.setNombreVeterinario(rs.getString("veterinario_nombre"));
        return c;
    }

    private ProcedimientoEspecial mapearProcedimiento(ResultSet rs) throws SQLException {
        Timestamp fHora = rs.getTimestamp("fecha_hora");
        Date fControl = rs.getDate("proximo_control");
        int dur = rs.getInt("duracion_estimada_minutos");
        Integer durObj = rs.wasNull() ? null : dur;

        ProcedimientoEspecial p = new ProcedimientoEspecial(
                rs.getInt("id"),
                rs.getInt("mascota_id"),
                rs.getInt("veterinario_id"),
                rs.getString("tipo_procedimiento"),
                rs.getString("nombre_procedimiento"),
                fHora != null ? fHora.toLocalDateTime() : null,
                durObj,
                rs.getString("informacion_preoperatoria"),
                rs.getString("detalle_procedimiento"),
                rs.getString("complicaciones"),
                rs.getString("seguimiento_postoperatorio"),
                fControl != null ? fControl.toLocalDate() : null,
                EstadoProcedimientoEnum.desdeString(rs.getString("estado")),
                rs.getBigDecimal("costo_procedimiento")
        );
        p.setNombreMascota(rs.getString("mascota_nombre"));
        p.setNombreDueno(rs.getString("dueno_nombre"));
        p.setNombreVeterinario(rs.getString("veterinario_nombre"));
        return p;
    }
}
