package randolh.veterinarysystem.cita;

import randolh.veterinarysystem.core.config.ConexionBD;
import randolh.veterinarysystem.core.exception.BaseDatosException;
import randolh.veterinarysystem.core.utils.LoggerManager;

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

// Acceso a datos de citas clinicas
public class CitaDAO {

    private static final String MODULO = "CitaDAO";

    private static final String SELECT_BASE =
            "SELECT c.id, c.mascota_id, c.veterinario_id, c.fecha_hora, c.motivo, c.estado, " +
            "c.notas, c.fecha_cancelacion, c.motivo_cancelacion, c.fecha_creacion, " +
            "m.nombre AS mascota_nombre, d.nombre_completo AS dueno_nombre, " +
            "d.telefono AS dueno_telefono, v.nombre_completo AS veterinario_nombre " +
            "FROM citas c " +
            "INNER JOIN mascotas m ON c.mascota_id = m.id " +
            "INNER JOIN duenos d ON m.dueno_id = d.id " +
            "LEFT JOIN veterinarios v ON c.veterinario_id = v.id ";

    // Inserta una cita y recupera el ID autoincremental
    public Cita insertar(Cita c) {
        String sql = "INSERT INTO citas (mascota_id, veterinario_id, fecha_hora, motivo, estado, " +
                "notas, fecha_cancelacion, motivo_cancelacion, fecha_creacion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, c.getMascotaId());
            if (c.getVeterinarioId() != null) {
                stmt.setInt(2, c.getVeterinarioId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setTimestamp(3, Timestamp.valueOf(c.getFechaHora()));
            stmt.setString(4, c.getMotivo());
            stmt.setString(5, c.getEstado().getValorDb());
            stmt.setString(6, c.getNotas());
            stmt.setNull(7, Types.TIMESTAMP);
            stmt.setNull(8, Types.VARCHAR);
            stmt.setTimestamp(9, Timestamp.valueOf(c.getFechaCreacion()));

            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new BaseDatosException("No se pudo agendar la cita medica.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }

            LoggerManager.info(MODULO, "Cita agendada con ID: " + c.getId());
            return c;

        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al insertar cita: " + e.getMessage(), e);
            throw new BaseDatosException("Error al registrar cita: " + e.getMessage(), e);
        }
    }

    // Actualiza los datos de una cita
    public boolean actualizar(Cita c) {
        String sql = "UPDATE citas SET mascota_id = ?, veterinario_id = ?, fecha_hora = ?, " +
                "motivo = ?, estado = ?, notas = ?, fecha_cancelacion = ?, motivo_cancelacion = ? " +
                "WHERE id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, c.getMascotaId());
            if (c.getVeterinarioId() != null) {
                stmt.setInt(2, c.getVeterinarioId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setTimestamp(3, Timestamp.valueOf(c.getFechaHora()));
            stmt.setString(4, c.getMotivo());
            stmt.setString(5, c.getEstado().getValorDb());
            stmt.setString(6, c.getNotas());

            if (c.getFechaCancelacion() != null) {
                stmt.setTimestamp(7, Timestamp.valueOf(c.getFechaCancelacion()));
            } else {
                stmt.setNull(7, Types.TIMESTAMP);
            }

            stmt.setString(8, c.getMotivoCancelacion());
            stmt.setInt(9, c.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al actualizar cita ID " + c.getId(), e);
            throw new BaseDatosException("Error al actualizar cita: " + e.getMessage(), e);
        }
    }

    // Busca una cita por ID
    public Optional<Cita> buscarPorId(int id) {
        String sql = SELECT_BASE + "WHERE c.id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar cita por ID: " + id, e);
            throw new BaseDatosException("Error al consultar cita: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Detecta si existe solapamiento en el rango de 30 minutos para un veterinario
    public Optional<Cita> detectarSolapamiento(int veterinarioId, LocalDateTime fechaHora, Integer citaIdExcluir) {
        String sql = SELECT_BASE +
                "WHERE c.veterinario_id = ? " +
                "  AND c.estado IN ('Programada', 'Confirmada') " +
                "  AND ABS(TIMESTAMPDIFF(MINUTE, c.fecha_hora, ?)) < 30 " +
                "  AND (? IS NULL OR c.id <> ?) " +
                "LIMIT 1";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, veterinarioId);
            stmt.setTimestamp(2, Timestamp.valueOf(fechaHora));
            if (citaIdExcluir != null) {
                stmt.setInt(3, citaIdExcluir);
                stmt.setInt(4, citaIdExcluir);
            } else {
                stmt.setNull(3, Types.INTEGER);
                stmt.setNull(4, Types.INTEGER);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al verificar solapamiento de agenda", e);
            throw new BaseDatosException("Error al verificar agenda de citas: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Lista todas las citas ordenadas cronologicamente
    public List<Cita> listarTodas() {
        List<Cita> lista = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY c.fecha_hora DESC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar todas las citas", e);
            throw new BaseDatosException("Error al consultar citas: " + e.getMessage(), e);
        }
        return lista;
    }

    // Lista citas para una fecha dada (agenda del dia)
    public List<Cita> listarPorFecha(LocalDate fecha) {
        List<Cita> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE DATE(c.fecha_hora) = ? ORDER BY c.fecha_hora ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(fecha));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al consultar citas por fecha: " + fecha, e);
            throw new BaseDatosException("Error al consultar agenda: " + e.getMessage(), e);
        }
        return lista;
    }

    // Lista citas asignadas a un veterinario en una fecha especifica o general
    public List<Cita> listarPorVeterinario(int vetId, LocalDate fecha) {
        List<Cita> lista = new ArrayList<>();
        String sql = SELECT_BASE +
                "WHERE c.veterinario_id = ? " +
                "  AND (? IS NULL OR DATE(c.fecha_hora) = ?) " +
                "ORDER BY c.fecha_hora ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vetId);
            if (fecha != null) {
                stmt.setDate(2, Date.valueOf(fecha));
                stmt.setDate(3, Date.valueOf(fecha));
            } else {
                stmt.setNull(2, Types.DATE);
                stmt.setNull(3, Types.DATE);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar citas por veterinario", e);
            throw new BaseDatosException("Error al consultar citas del veterinario: " + e.getMessage(), e);
        }
        return lista;
    }

    // Lista historial de citas de una mascota
    public List<Cita> listarPorMascota(int mascotaId) {
        List<Cita> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE c.mascota_id = ? ORDER BY c.fecha_hora DESC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mascotaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar citas de mascota ID: " + mascotaId, e);
            throw new BaseDatosException("Error al consultar citas de la mascota: " + e.getMessage(), e);
        }
        return lista;
    }

    // Mapea la fila del ResultSet a la entidad Cita
    private Cita mapearResultSet(ResultSet rs) throws SQLException {
        Timestamp fHora = rs.getTimestamp("fecha_hora");
        Timestamp fCanc = rs.getTimestamp("fecha_cancelacion");
        Timestamp fCrea = rs.getTimestamp("fecha_creacion");

        int vetId = rs.getInt("veterinario_id");
        Integer vetIdObj = rs.wasNull() ? null : vetId;

        Cita c = new Cita(
                rs.getInt("id"),
                rs.getInt("mascota_id"),
                vetIdObj,
                fHora != null ? fHora.toLocalDateTime() : null,
                rs.getString("motivo"),
                EstadoCitaEnum.desdeString(rs.getString("estado")),
                rs.getString("notas"),
                fCanc != null ? fCanc.toLocalDateTime() : null,
                rs.getString("motivo_cancelacion"),
                fCrea != null ? fCrea.toLocalDateTime() : null
        );

        c.setNombreMascota(rs.getString("mascota_nombre"));
        c.setNombreDueno(rs.getString("dueno_nombre"));
        c.setTelefonoDueno(rs.getString("dueno_telefono"));
        c.setNombreVeterinario(rs.getString("veterinario_nombre"));

        return c;
    }
}
