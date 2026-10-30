package randolh.veterinarysystem.mascota;

import randolh.veterinarysystem.core.config.ConexionBD;
import randolh.veterinarysystem.core.exception.BaseDatosException;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Acceso a datos de mascotas y pacientes
public class MascotaDAO {

    private static final String MODULO = "MascotaDAO";

    // DTO simple para catalogo de especies y razas
    public static class CatalogoItem {
        private final int id;
        private final String nombre;

        public CatalogoItem(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }

        @Override
        public String toString() {
            return String.format("[%d] %s", id, nombre);
        }
    }

    private static final String SELECT_BASE =
            "SELECT m.id, m.dueno_id, m.nombre, m.especie_id, m.raza_id, m.fecha_nacimiento, " +
            "m.sexo, m.peso_actual, m.microchip, m.tatuaje, m.foto_url, m.alergias, " +
            "m.condiciones_preexistentes, m.fecha_registro, m.activo, " +
            "e.nombre AS especie_nombre, r.nombre AS raza_nombre, d.nombre_completo AS dueno_nombre " +
            "FROM mascotas m " +
            "INNER JOIN especies e ON m.especie_id = e.id " +
            "INNER JOIN razas r ON m.raza_id = r.id " +
            "INNER JOIN duenos d ON m.dueno_id = d.id ";

    // Inserta una nueva mascota en la base de datos
    public Mascota insertar(Mascota m) {
        String sql = "INSERT INTO mascotas (dueno_id, nombre, especie_id, raza_id, fecha_nacimiento, " +
                "sexo, peso_actual, microchip, tatuaje, foto_url, alergias, condiciones_preexistentes, " +
                "fecha_registro, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, m.getDuenoId());
            stmt.setString(2, m.getNombre());
            stmt.setInt(3, m.getEspecieId());
            stmt.setInt(4, m.getRazaId());
            stmt.setDate(5, Date.valueOf(m.getFechaNacimiento()));
            stmt.setString(6, m.getSexo().getValorDb());
            stmt.setBigDecimal(7, java.math.BigDecimal.valueOf(m.getPesoActual()));
            stmt.setString(8, m.getMicrochip());
            stmt.setString(9, m.getTatuaje());
            stmt.setString(10, m.getFotoUrl());
            stmt.setString(11, m.getAlergias());
            stmt.setString(12, m.getCondicionesPreexistentes());
            stmt.setTimestamp(13, Timestamp.valueOf(m.getFechaRegistro()));
            stmt.setBoolean(14, m.isActivo());

            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new BaseDatosException("No se pudo registrar la mascota.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    m.setId(rs.getInt(1));
                }
            }

            LoggerManager.info(MODULO, "Mascota registrada con ID: " + m.getId());
            return m;

        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al insertar mascota: " + e.getMessage(), e);
            throw new BaseDatosException("Error al registrar paciente en base de datos: " + e.getMessage(), e);
        }
    }

    // Actualiza los datos de la mascota
    public boolean actualizar(Mascota m) {
        String sql = "UPDATE mascotas SET dueno_id = ?, nombre = ?, especie_id = ?, raza_id = ?, " +
                "fecha_nacimiento = ?, sexo = ?, peso_actual = ?, microchip = ?, tatuaje = ?, " +
                "foto_url = ?, alergias = ?, condiciones_preexistentes = ?, activo = ? WHERE id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, m.getDuenoId());
            stmt.setString(2, m.getNombre());
            stmt.setInt(3, m.getEspecieId());
            stmt.setInt(4, m.getRazaId());
            stmt.setDate(5, Date.valueOf(m.getFechaNacimiento()));
            stmt.setString(6, m.getSexo().getValorDb());
            stmt.setBigDecimal(7, java.math.BigDecimal.valueOf(m.getPesoActual()));
            stmt.setString(8, m.getMicrochip());
            stmt.setString(9, m.getTatuaje());
            stmt.setString(10, m.getFotoUrl());
            stmt.setString(11, m.getAlergias());
            stmt.setString(12, m.getCondicionesPreexistentes());
            stmt.setBoolean(13, m.isActivo());
            stmt.setInt(14, m.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al actualizar mascota ID " + m.getId(), e);
            throw new BaseDatosException("Error al actualizar paciente: " + e.getMessage(), e);
        }
    }

    // Desactivacion logica
    public boolean eliminarLogico(int id) {
        String sql = "UPDATE mascotas SET activo = FALSE WHERE id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al desactivar mascota ID " + id, e);
            throw new BaseDatosException("Error al desactivar paciente: " + e.getMessage(), e);
        }
    }

    // Busca mascota por ID con nombres de especie, raza y dueno
    public Optional<Mascota> buscarPorId(int id) {
        String sql = SELECT_BASE + "WHERE m.id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar mascota por ID: " + id, e);
            throw new BaseDatosException("Error al buscar paciente por ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Busca mascota por codigo de microchip
    public Optional<Mascota> buscarPorMicrochip(String microchip) {
        String sql = SELECT_BASE + "WHERE m.microchip = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, microchip.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar mascota por microchip: " + microchip, e);
            throw new BaseDatosException("Error al buscar por microchip: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Lista todas las mascotas
    public List<Mascota> listarTodas() {
        List<Mascota> lista = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY m.id ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar todas las mascotas", e);
            throw new BaseDatosException("Error al obtener lista de pacientes: " + e.getMessage(), e);
        }
        return lista;
    }

    // Lista mascotas pertenecientes a un dueno especifico
    public List<Mascota> listarPorDueno(int duenoId) {
        List<Mascota> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE m.dueno_id = ? ORDER BY m.nombre ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, duenoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar mascotas del dueno ID: " + duenoId, e);
            throw new BaseDatosException("Error al consultar mascotas del cliente: " + e.getMessage(), e);
        }
        return lista;
    }

    // Busca mascotas por coincidencia de nombre
    public List<Mascota> buscarPorNombre(String termino) {
        List<Mascota> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE m.nombre LIKE ? ORDER BY m.nombre ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + termino.trim() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar mascotas por nombre: " + termino, e);
            throw new BaseDatosException("Error al buscar pacientes por nombre: " + e.getMessage(), e);
        }
        return lista;
    }

    // Transfiere la propiedad de una mascota invocando el SP de base de datos
    public void transferirPropiedad(int mascotaId, int nuevoDuenoId, String motivo) {
        String sql = "{CALL sp_transferir_propiedad_mascota(?, ?, ?)}";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, mascotaId);
            cstmt.setInt(2, nuevoDuenoId);
            cstmt.setString(3, motivo != null ? motivo.trim() : "Transferencia de propiedad");

            cstmt.execute();
            LoggerManager.info(MODULO, String.format("Transferencia exitosa: Mascota %d -> Nuevo Dueno %d", mascotaId, nuevoDuenoId));

        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Fallo al transferir mascota: " + e.getMessage(), e);
            throw new BaseDatosException(e.getMessage(), e);
        }
    }

    // Catalogo de especies activas
    public List<CatalogoItem> listarEspecies() {
        List<CatalogoItem> lista = new ArrayList<>();
        String sql = "SELECT id, nombre FROM especies ORDER BY id ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new CatalogoItem(rs.getInt("id"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al consultar especies", e);
            throw new BaseDatosException("Error al consultar catalogo de especies: " + e.getMessage(), e);
        }
        return lista;
    }

    // Catalogo de razas por especie
    public List<CatalogoItem> listarRazasPorEspecie(int especieId) {
        List<CatalogoItem> lista = new ArrayList<>();
        String sql = "SELECT id, nombre FROM razas WHERE especie_id = ? ORDER BY nombre ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, especieId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new CatalogoItem(rs.getInt("id"), rs.getString("nombre")));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al consultar razas para especie " + especieId, e);
            throw new BaseDatosException("Error al consultar catalogo de razas: " + e.getMessage(), e);
        }
        return lista;
    }

    // Mapea la fila actual del ResultSet a Mascota usando Builder
    private Mascota mapearResultSet(ResultSet rs) throws SQLException {
        Date fNac = rs.getDate("fecha_nacimiento");
        Timestamp fReg = rs.getTimestamp("fecha_registro");

        return Mascota.builder()
                .conId(rs.getInt("id"))
                .conDueno(rs.getInt("dueno_id"))
                .conNombre(rs.getString("nombre"))
                .conEspecie(rs.getInt("especie_id"))
                .conRaza(rs.getInt("raza_id"))
                .conFechaNacimiento(fNac != null ? fNac.toLocalDate() : null)
                .conSexo(SexoEnum.desdeString(rs.getString("sexo")))
                .conPeso(rs.getBigDecimal("peso_actual").doubleValue())
                .conMicrochip(rs.getString("microchip"))
                .conTatuaje(rs.getString("tatuaje"))
                .conFotoUrl(rs.getString("foto_url"))
                .conAlergias(rs.getString("alergias"))
                .conCondiciones(rs.getString("condiciones_preexistentes"))
                .conFechaRegistro(fReg != null ? fReg.toLocalDateTime() : null)
                .conActivo(rs.getBoolean("activo"))
                .conNombreEspecie(rs.getString("especie_nombre"))
                .conNombreRaza(rs.getString("raza_nombre"))
                .conNombreDueno(rs.getString("dueno_nombre"))
                .build();
    }
}
