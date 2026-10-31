package randolh.veterinarysystem.alerta;

import randolh.veterinarysystem.core.config.ConexionBD;
import randolh.veterinarysystem.core.exception.BaseDatosException;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Acceso a datos para persistencia, consulta y actualizacion de alertas del sistema
public class AlertaDAO {

    private static final String MODULO = "AlertaDAO";

    private static final String SELECT_BASE =
            "SELECT a.id, a.tipo_alerta, a.nivel_severidad, a.mensaje AS titulo, a.mensaje, " +
            "a.referencia_id AS producto_id, a.referencia_id, a.fecha_generacion, a.leida, " +
            "i.nombre_producto " +
            "FROM alertas_sistema a " +
            "LEFT JOIN inventario i ON a.referencia_id = i.id ";

    public AlertaDAO() {
        asegurarTabla();
    }

    // Asegura que la tabla de alertas exista en la base de datos
    public void asegurarTabla() {
        String sql = "CREATE TABLE IF NOT EXISTS alertas_sistema (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  tipo_alerta ENUM('STOCK_MINIMO', 'PRODUCTO_VENCIDO', 'PRODUCTO_POR_VENCER', 'CITA_PENDIENTE', 'GENERAL') NOT NULL," +
                "  nivel_severidad ENUM('Informativa', 'Advertencia', 'Critica') NOT NULL," +
                "  referencia_tabla VARCHAR(50) NULL," +
                "  referencia_id INT NULL," +
                "  mensaje TEXT NOT NULL," +
                "  fecha_generacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  leida BOOLEAN NOT NULL DEFAULT FALSE," +
                "  fecha_atencion DATETIME NULL," +
                "  INDEX idx_alertas_tipo (tipo_alerta)," +
                "  INDEX idx_alertas_severidad (nivel_severidad)," +
                "  INDEX idx_alertas_leida (leida)," +
                "  INDEX idx_alertas_fecha (fecha_generacion)" +
                ")";

        try {
            Connection conn = ConexionBD.getInstancia().getConexion();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        } catch (Exception e) {
            LoggerManager.warning(MODULO, "No se pudo verificar/crear tabla alertas_sistema (posible modo offline): " + e.getMessage());
        }
    }

    // Inserta una alerta y captura su ID autoincremental
    public Alerta insertar(Alerta a) {
        String sql = "INSERT INTO alertas_sistema (tipo_alerta, nivel_severidad, referencia_tabla, " +
                "referencia_id, mensaje, fecha_generacion, leida) VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String tipoDb = switch (a.getTipoAlerta()) {
                case STOCK_MINIMO -> "STOCK_MINIMO";
                case VENCIDO -> "PRODUCTO_VENCIDO";
                case POR_VENCER -> "PRODUCTO_POR_VENCER";
                case CITA_PROXIMA -> "CITA_PENDIENTE";
            };
            stmt.setString(1, tipoDb);

            String sevDb = switch (a.getNivelSeveridad()) {
                case INFO -> "Informativa";
                case ADVERTENCIA -> "Advertencia";
                case CRITICO -> "Critica";
            };
            stmt.setString(2, sevDb);

            String refTabla = a.getProductoId() != null ? "inventario" : "sistema";
            stmt.setString(3, refTabla);

            Integer refId = a.getProductoId() != null ? a.getProductoId() : a.getReferenciaId();
            if (refId != null) {
                stmt.setInt(4, refId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            String msgCompleto = (a.getTitulo() != null && !a.getTitulo().isBlank()) ?
                    a.getTitulo() + ": " + a.getMensaje() : a.getMensaje();
            stmt.setString(5, msgCompleto != null ? msgCompleto : "Alerta de sistema");
            stmt.setTimestamp(6, Timestamp.valueOf(a.getFechaGeneracion()));
            stmt.setBoolean(7, a.isLeida());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    a.setId(rs.getInt(1));
                }
            }
            LoggerManager.info(MODULO, "Alerta registrada con ID: " + a.getId() + " [" + a.getNivelSeveridad() + "]");
            return a;

        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al insertar alerta: " + e.getMessage(), e);
            throw new BaseDatosException("Error al registrar alerta: " + e.getMessage(), e);
        }
    }

    // Obtiene alertas pendientes (no leidas) priorizadas por severidad
    public List<Alerta> obtenerAlertasPendientes() {
        List<Alerta> lista = new ArrayList<>();
        String sql = SELECT_BASE +
                "WHERE a.leida = FALSE " +
                "ORDER BY FIELD(a.nivel_severidad, 'Critica', 'Advertencia', 'Informativa'), a.fecha_generacion DESC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearAlerta(rs));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar alertas pendientes", e);
            throw new BaseDatosException("Error al consultar alertas pendientes: " + e.getMessage(), e);
        }
        return lista;
    }

    // Obtiene todas las alertas (historico)
    public List<Alerta> obtenerTodas() {
        List<Alerta> lista = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY a.fecha_generacion DESC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearAlerta(rs));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar historico de alertas", e);
            throw new BaseDatosException("Error al consultar historico de alertas: " + e.getMessage(), e);
        }
        return lista;
    }

    // Busca alerta por ID
    public Optional<Alerta> buscarPorId(int id) {
        String sql = SELECT_BASE + "WHERE a.id = ?";
        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearAlerta(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar alerta por ID: " + id, e);
            throw new BaseDatosException("Error al consultar alerta: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Marca una alerta como leida/atendida
    public boolean marcarComoAtendida(int id) {
        String sql = "UPDATE alertas_sistema SET leida = TRUE WHERE id = ?";
        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al marcar alerta como atendida ID: " + id, e);
            throw new BaseDatosException("Error al actualizar alerta: " + e.getMessage(), e);
        }
    }

    // Marca todas las alertas pendientes como leidas
    public int marcarTodasComoAtendidas() {
        String sql = "UPDATE alertas_sistema SET leida = TRUE WHERE leida = FALSE";
        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al marcar todas las alertas como atendidas", e);
            throw new BaseDatosException("Error al actualizar alertas: " + e.getMessage(), e);
        }
    }

    // Cuenta el total de alertas pendientes
    public int contarAlertasPendientes() {
        String sql = "SELECT COUNT(*) FROM alertas_sistema WHERE leida = FALSE";
        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al contar alertas pendientes", e);
        }
        return 0;
    }

    // Ejecuta el procedimiento almacenado en MySQL para generar alertas de inventario
    public boolean ejecutarGeneracionAlertasDB() {
        String sql = "{CALL sp_generar_alertas_inventario()}";
        Connection conn = ConexionBD.getInstancia().getConexion();
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.execute();
            LoggerManager.info(MODULO, "Procedimiento sp_generar_alertas_inventario ejecutado exitosamente.");
            return true;
        } catch (SQLException e) {
            LoggerManager.warning(MODULO, "Procedimiento sp_generar_alertas_inventario no disponible o fallo: " + e.getMessage());
            return false;
        }
    }

    private Alerta mapearAlerta(ResultSet rs) throws SQLException {
        Timestamp fGen = rs.getTimestamp("fecha_generacion");
        int prodId = rs.getInt("producto_id");
        Integer prodIdObj = rs.wasNull() ? null : prodId;
        int refId = rs.getInt("referencia_id");
        Integer refIdObj = rs.wasNull() ? null : refId;

        Alerta a = new Alerta(
                rs.getInt("id"),
                TipoAlertaEnum.desdeString(rs.getString("tipo_alerta")),
                NivelSeveridadEnum.desdeString(rs.getString("nivel_severidad")),
                rs.getString("titulo"),
                rs.getString("mensaje"),
                prodIdObj,
                refIdObj,
                fGen != null ? fGen.toLocalDateTime() : null,
                rs.getBoolean("leida")
        );
        a.setNombreProducto(rs.getString("nombre_producto"));
        return a;
    }
}
