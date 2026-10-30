package randolh.veterinarysystem.veterinario;

import randolh.veterinarysystem.core.config.ConexionBD;
import randolh.veterinarysystem.core.exception.BaseDatosException;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Acceso a datos de medicos veterinarios
public class VeterinarioDAO {

    private static final String MODULO = "VeterinarioDAO";

    // Inserta un nuevo veterinario y recupera el ID autoincremental
    public Veterinario insertar(Veterinario v) {
        String sql = "INSERT INTO veterinarios (tipo_documento, numero_documento, nombre_completo, " +
                "licencia_profesional, especialidad, telefono, email, fecha_contratacion, activo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, v.getTipoDocumento());
            stmt.setString(2, v.getNumeroDocumento());
            stmt.setString(3, v.getNombreCompleto());
            stmt.setString(4, v.getLicenciaProfesional());
            stmt.setString(5, v.getEspecialidad());
            stmt.setString(6, v.getTelefono());
            stmt.setString(7, v.getEmail());
            stmt.setDate(8, v.getFechaContratacion() != null ? Date.valueOf(v.getFechaContratacion()) : null);
            stmt.setBoolean(9, v.isActivo());

            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new BaseDatosException("No se pudo registrar el medico veterinario.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    v.setId(rs.getInt(1));
                }
            }

            LoggerManager.info(MODULO, "Veterinario registrado con ID: " + v.getId());
            return v;

        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al insertar veterinario: " + e.getMessage(), e);
            throw new BaseDatosException("Error al registrar medico veterinario: " + e.getMessage(), e);
        }
    }

    // Actualiza datos de un veterinario
    public boolean actualizar(Veterinario v) {
        String sql = "UPDATE veterinarios SET tipo_documento = ?, numero_documento = ?, nombre_completo = ?, " +
                "licencia_profesional = ?, especialidad = ?, telefono = ?, email = ?, " +
                "fecha_contratacion = ?, activo = ? WHERE id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, v.getTipoDocumento());
            stmt.setString(2, v.getNumeroDocumento());
            stmt.setString(3, v.getNombreCompleto());
            stmt.setString(4, v.getLicenciaProfesional());
            stmt.setString(5, v.getEspecialidad());
            stmt.setString(6, v.getTelefono());
            stmt.setString(7, v.getEmail());
            stmt.setDate(8, v.getFechaContratacion() != null ? Date.valueOf(v.getFechaContratacion()) : null);
            stmt.setBoolean(9, v.isActivo());
            stmt.setInt(10, v.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al actualizar veterinario ID " + v.getId(), e);
            throw new BaseDatosException("Error al actualizar medico veterinario: " + e.getMessage(), e);
        }
    }

    // Desactivacion logica
    public boolean eliminarLogico(int id) {
        String sql = "UPDATE veterinarios SET activo = FALSE WHERE id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al desactivar veterinario ID " + id, e);
            throw new BaseDatosException("Error al desactivar medico: " + e.getMessage(), e);
        }
    }

    // Busca veterinario por ID
    public Optional<Veterinario> buscarPorId(int id) {
        String sql = "SELECT id, tipo_documento, numero_documento, nombre_completo, licencia_profesional, " +
                "especialidad, telefono, email, fecha_contratacion, activo FROM veterinarios WHERE id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar veterinario por ID: " + id, e);
            throw new BaseDatosException("Error al consultar veterinario: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Busca por numero de licencia profesional medica
    public Optional<Veterinario> buscarPorLicencia(String licencia) {
        String sql = "SELECT id, tipo_documento, numero_documento, nombre_completo, licencia_profesional, " +
                "especialidad, telefono, email, fecha_contratacion, activo FROM veterinarios " +
                "WHERE licencia_profesional = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, licencia.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar por licencia: " + licencia, e);
            throw new BaseDatosException("Error al consultar por licencia: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Busca por documento de identidad
    public Optional<Veterinario> buscarPorDocumento(String documento) {
        String sql = "SELECT id, tipo_documento, numero_documento, nombre_completo, licencia_profesional, " +
                "especialidad, telefono, email, fecha_contratacion, activo FROM veterinarios " +
                "WHERE numero_documento = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, documento.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar veterinario por documento: " + documento, e);
            throw new BaseDatosException("Error al consultar por documento: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Busca por correo electronico
    public Optional<Veterinario> buscarPorEmail(String email) {
        String sql = "SELECT id, tipo_documento, numero_documento, nombre_completo, licencia_profesional, " +
                "especialidad, telefono, email, fecha_contratacion, activo FROM veterinarios " +
                "WHERE email = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar veterinario por email: " + email, e);
            throw new BaseDatosException("Error al consultar por correo: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Lista todos los veterinarios ordenados por nombre
    public List<Veterinario> listarTodos() {
        List<Veterinario> lista = new ArrayList<>();
        String sql = "SELECT id, tipo_documento, numero_documento, nombre_completo, licencia_profesional, " +
                "especialidad, telefono, email, fecha_contratacion, activo FROM veterinarios " +
                "ORDER BY nombre_completo ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar veterinarios", e);
            throw new BaseDatosException("Error al obtener equipo medico: " + e.getMessage(), e);
        }
        return lista;
    }

    // Filtra veterinarios por especialidad
    public List<Veterinario> listarPorEspecialidad(String especialidad) {
        List<Veterinario> lista = new ArrayList<>();
        String sql = "SELECT id, tipo_documento, numero_documento, nombre_completo, licencia_profesional, " +
                "especialidad, telefono, email, fecha_contratacion, activo FROM veterinarios " +
                "WHERE especialidad LIKE ? ORDER BY nombre_completo ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + especialidad.trim() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al filtrar veterinarios por especialidad: " + especialidad, e);
            throw new BaseDatosException("Error al filtrar por especialidad: " + e.getMessage(), e);
        }
        return lista;
    }

    // Mapea ResultSet a entidad Veterinario
    private Veterinario mapearResultSet(ResultSet rs) throws SQLException {
        Date fCont = rs.getDate("fecha_contratacion");
        return new Veterinario(
                rs.getInt("id"),
                rs.getString("tipo_documento"),
                rs.getString("numero_documento"),
                rs.getString("nombre_completo"),
                rs.getString("licencia_profesional"),
                rs.getString("especialidad"),
                rs.getString("telefono"),
                rs.getString("email"),
                fCont != null ? fCont.toLocalDate() : null,
                rs.getBoolean("activo")
        );
    }
}
