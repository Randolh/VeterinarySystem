package randolh.veterinarysystem.dueno;

import randolh.veterinarysystem.core.config.ConexionBD;
import randolh.veterinarysystem.core.exception.BaseDatosException;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Acceso a datos de duenos y clientes
public class DuenoDAO {

    private static final String MODULO = "DuenoDAO";

    // Inserta un dueno y asigna el id generado
    public Dueno insertar(Dueno dueno) {
        String sql = "INSERT INTO duenos (tipo_documento, numero_documento, nombre_completo, direccion, " +
                "telefono, email, contacto_emergencia, fecha_registro, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, dueno.getTipoDocumento());
            stmt.setString(2, dueno.getNumeroDocumento());
            stmt.setString(3, dueno.getNombreCompleto());
            stmt.setString(4, dueno.getDireccion());
            stmt.setString(5, dueno.getTelefono());
            stmt.setString(6, dueno.getEmail());
            stmt.setString(7, dueno.getContactoEmergencia());
            stmt.setTimestamp(8, Timestamp.valueOf(dueno.getFechaRegistro()));
            stmt.setBoolean(9, dueno.isActivo());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new BaseDatosException("No se pudo insertar el registro del dueno.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    dueno.setId(rs.getInt(1));
                }
            }

            LoggerManager.info(MODULO, "Dueno registrado con ID: " + dueno.getId());
            return dueno;

        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al insertar dueno: " + e.getMessage(), e);
            throw new BaseDatosException("Error al registrar dueno en base de datos: " + e.getMessage(), e);
        }
    }

    // Actualiza los datos de un dueno
    public boolean actualizar(Dueno dueno) {
        String sql = "UPDATE duenos SET tipo_documento = ?, numero_documento = ?, nombre_completo = ?, " +
                "direccion = ?, telefono = ?, email = ?, contacto_emergencia = ?, activo = ? WHERE id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dueno.getTipoDocumento());
            stmt.setString(2, dueno.getNumeroDocumento());
            stmt.setString(3, dueno.getNombreCompleto());
            stmt.setString(4, dueno.getDireccion());
            stmt.setString(5, dueno.getTelefono());
            stmt.setString(6, dueno.getEmail());
            stmt.setString(7, dueno.getContactoEmergencia());
            stmt.setBoolean(8, dueno.isActivo());
            stmt.setInt(9, dueno.getId());

            int filas = stmt.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al actualizar dueno ID " + dueno.getId(), e);
            throw new BaseDatosException("Error al actualizar dueno: " + e.getMessage(), e);
        }
    }

    // Desactiva un dueno de forma logica
    public boolean eliminarLogico(int id) {
        String sql = "UPDATE duenos SET activo = FALSE WHERE id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al desactivar dueno ID " + id, e);
            throw new BaseDatosException("Error al desactivar dueno: " + e.getMessage(), e);
        }
    }

    // Busca un dueno por su ID
    public Optional<Dueno> buscarPorId(int id) {
        String sql = "SELECT id, tipo_documento, numero_documento, nombre_completo, direccion, telefono, " +
                "email, contacto_emergencia, fecha_registro, activo FROM duenos WHERE id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar dueno por ID: " + id, e);
            throw new BaseDatosException("Error al buscar dueno por ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Busca un dueno por numero de documento
    public Optional<Dueno> buscarPorDocumento(String numeroDocumento) {
        String sql = "SELECT id, tipo_documento, numero_documento, nombre_completo, direccion, telefono, " +
                "email, contacto_emergencia, fecha_registro, activo FROM duenos WHERE numero_documento = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, numeroDocumento.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar dueno por documento: " + numeroDocumento, e);
            throw new BaseDatosException("Error al buscar dueno por documento: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Busca un dueno por correo electronico
    public Optional<Dueno> buscarPorEmail(String email) {
        String sql = "SELECT id, tipo_documento, numero_documento, nombre_completo, direccion, telefono, " +
                "email, contacto_emergencia, fecha_registro, activo FROM duenos WHERE email = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar dueno por email: " + email, e);
            throw new BaseDatosException("Error al buscar dueno por email: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Lista todos los duenos registrados
    public List<Dueno> listarTodos() {
        List<Dueno> lista = new ArrayList<>();
        String sql = "SELECT id, tipo_documento, numero_documento, nombre_completo, direccion, telefono, " +
                "email, contacto_emergencia, fecha_registro, activo FROM duenos ORDER BY id ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar todos los duenos", e);
            throw new BaseDatosException("Error al obtener lista de duenos: " + e.getMessage(), e);
        }
        return lista;
    }

    // Busca duenos por coincidencia en nombre o apellido
    public List<Dueno> buscarPorNombre(String termino) {
        List<Dueno> lista = new ArrayList<>();
        String sql = "SELECT id, tipo_documento, numero_documento, nombre_completo, direccion, telefono, " +
                "email, contacto_emergencia, fecha_registro, activo FROM duenos " +
                "WHERE nombre_completo LIKE ? ORDER BY nombre_completo ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + termino.trim() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar duenos por nombre: " + termino, e);
            throw new BaseDatosException("Error al buscar duenos por nombre: " + e.getMessage(), e);
        }
        return lista;
    }

    // Mapea la fila actual del ResultSet a una entidad Dueno
    private Dueno mapearResultSet(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("fecha_registro");
        return new Dueno(
                rs.getInt("id"),
                rs.getString("tipo_documento"),
                rs.getString("numero_documento"),
                rs.getString("nombre_completo"),
                rs.getString("direccion"),
                rs.getString("telefono"),
                rs.getString("email"),
                rs.getString("contacto_emergencia"),
                ts != null ? ts.toLocalDateTime() : null,
                rs.getBoolean("activo")
        );
    }
}
