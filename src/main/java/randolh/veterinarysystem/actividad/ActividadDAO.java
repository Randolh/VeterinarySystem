package randolh.veterinarysystem.actividad;

import randolh.veterinarysystem.core.config.ConexionBD;
import randolh.veterinarysystem.core.config.TransaccionManager;
import randolh.veterinarysystem.core.exception.BaseDatosException;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Acceso a datos para jornadas de vacunacion, dias de adopcion y programa Club de Mascotas
public class ActividadDAO {

    private static final String MODULO = "ActividadDAO";

    // ==========================================
    // 1. SUBMODULO DE ADOPCIONES RESPONSABLES
    // ==========================================

    // Lista las mascotas albergadas que estan disponibles para ser adoptadas
    public List<MascotaAdopcion> listarMascotasAdopcionDisponibles() {
        List<MascotaAdopcion> lista = new ArrayList<>();
        String sql = "SELECT ma.id, ma.mascota_id, ma.historia, ma.temperamento, ma.necesidades_especiales, " +
                "ma.fecha_ingreso, ma.estado, ma.fecha_adopcion, ma.dueno_adoptante_id, ma.foto_adicional_url, " +
                "m.nombre AS mascota_nombre, e.nombre AS especie_nombre, r.nombre AS raza_nombre, " +
                "m.sexo, m.fecha_nacimiento " +
                "FROM mascotas_adopcion ma " +
                "INNER JOIN mascotas m ON ma.mascota_id = m.id " +
                "INNER JOIN especies e ON m.especie_id = e.id " +
                "LEFT JOIN razas r ON m.raza_id = r.id " +
                "WHERE ma.estado = 'Disponible' " +
                "ORDER BY ma.fecha_ingreso ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearMascotaAdopcion(rs));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar mascotas disponibles para adopcion", e);
            throw new BaseDatosException("Error al listar mascotas de adopcion: " + e.getMessage(), e);
        }
        return lista;
    }

    // Lista el historico completo de mascotas en el programa de adopcion
    public List<MascotaAdopcion> listarTodasMascotasAdopcion() {
        List<MascotaAdopcion> lista = new ArrayList<>();
        String sql = "SELECT ma.id, ma.mascota_id, ma.historia, ma.temperamento, ma.necesidades_especiales, " +
                "ma.fecha_ingreso, ma.estado, ma.fecha_adopcion, ma.dueno_adoptante_id, ma.foto_adicional_url, " +
                "m.nombre AS mascota_nombre, e.nombre AS especie_nombre, r.nombre AS raza_nombre, " +
                "m.sexo, m.fecha_nacimiento, d.nombre_completo AS adoptante_nombre, " +
                "d.numero_documento AS adoptante_doc, d.telefono AS adoptante_tel " +
                "FROM mascotas_adopcion ma " +
                "INNER JOIN mascotas m ON ma.mascota_id = m.id " +
                "INNER JOIN especies e ON m.especie_id = e.id " +
                "LEFT JOIN razas r ON m.raza_id = r.id " +
                "LEFT JOIN duenos d ON ma.dueno_adoptante_id = d.id " +
                "ORDER BY ma.id DESC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                MascotaAdopcion ma = mapearMascotaAdopcion(rs);
                ma.setNombreAdoptante(rs.getString("adoptante_nombre"));
                ma.setDocumentoAdoptante(rs.getString("adoptante_doc"));
                ma.setTelefonoAdoptante(rs.getString("adoptante_tel"));
                lista.add(ma);
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar todas las mascotas de adopcion", e);
            throw new BaseDatosException("Error al listar historial de adopciones: " + e.getMessage(), e);
        }
        return lista;
    }

    // Busca un registro de mascota en adopcion por su ID primario
    public Optional<MascotaAdopcion> buscarMascotaAdopcionPorId(int id) {
        String sql = "SELECT ma.id, ma.mascota_id, ma.historia, ma.temperamento, ma.necesidades_especiales, " +
                "ma.fecha_ingreso, ma.estado, ma.fecha_adopcion, ma.dueno_adoptante_id, ma.foto_adicional_url, " +
                "m.nombre AS mascota_nombre, e.nombre AS especie_nombre, r.nombre AS raza_nombre, " +
                "m.sexo, m.fecha_nacimiento, d.nombre_completo AS adoptante_nombre, " +
                "d.numero_documento AS adoptante_doc, d.telefono AS adoptante_tel " +
                "FROM mascotas_adopcion ma " +
                "INNER JOIN mascotas m ON ma.mascota_id = m.id " +
                "INNER JOIN especies e ON m.especie_id = e.id " +
                "LEFT JOIN razas r ON m.raza_id = r.id " +
                "LEFT JOIN duenos d ON ma.dueno_adoptante_id = d.id " +
                "WHERE ma.id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    MascotaAdopcion ma = mapearMascotaAdopcion(rs);
                    ma.setNombreAdoptante(rs.getString("adoptante_nombre"));
                    ma.setDocumentoAdoptante(rs.getString("adoptante_doc"));
                    ma.setTelefonoAdoptante(rs.getString("adoptante_tel"));
                    return Optional.of(ma);
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al consultar mascota en adopcion ID " + id, e);
            throw new BaseDatosException("Error al consultar mascota de adopcion: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Registra un animal rescatado en el programa de adopcion
    public MascotaAdopcion registrarMascotaParaAdopcion(MascotaAdopcion ma) {
        String sql = "INSERT INTO mascotas_adopcion (mascota_id, historia, temperamento, necesidades_especiales, " +
                "fecha_ingreso, estado, foto_adicional_url) VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ma.getMascotaId());
            stmt.setString(2, ma.getHistoria());
            stmt.setString(3, ma.getTemperamento());
            stmt.setString(4, ma.getNecesidadesEspeciales());
            stmt.setDate(5, Date.valueOf(ma.getFechaIngreso()));
            stmt.setString(6, ma.getEstado().getValor());
            stmt.setString(7, ma.getFotoAdicionalUrl());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    ma.setId(rs.getInt(1));
                }
            }
            LoggerManager.info(MODULO, "Mascota registrada en adopcion con ID: " + ma.getId());
            return ma;
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al registrar mascota para adopcion", e);
            throw new BaseDatosException("Error al registrar mascota en adopcion: " + e.getMessage(), e);
        }
    }

    // Formaliza la adopcion de manera transaccional: contrato, actualizacion de tutela e historial
    public Adopcion formalizarAdopcionTransaccional(Adopcion adopcion, MascotaAdopcion ma, Integer duenoAnteriorId) {
        return TransaccionManager.ejecutarEnTransaccion(conn -> {
            // 1. Insertar el contrato en la tabla adopciones
            String sqlAdopcion = "INSERT INTO adopciones (mascota_adopcion_id, dueno_id, fecha_adopcion, " +
                    "contrato_texto, condiciones_especiales, seguimiento_requerido, fecha_primer_seguimiento) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmtAdop = conn.prepareStatement(sqlAdopcion, Statement.RETURN_GENERATED_KEYS)) {
                stmtAdop.setInt(1, adopcion.getMascotaAdopcionId());
                stmtAdop.setInt(2, adopcion.getDuenoId());
                stmtAdop.setDate(3, Date.valueOf(adopcion.getFechaAdopcion()));
                stmtAdop.setString(4, adopcion.getContratoTexto());
                stmtAdop.setString(5, adopcion.getCondicionesEspeciales());
                stmtAdop.setBoolean(6, adopcion.isSeguimientoRequerido());
                if (adopcion.getFechaPrimerSeguimiento() != null) {
                    stmtAdop.setDate(7, Date.valueOf(adopcion.getFechaPrimerSeguimiento()));
                } else {
                    stmtAdop.setNull(7, Types.DATE);
                }

                stmtAdop.executeUpdate();
                try (ResultSet rs = stmtAdop.getGeneratedKeys()) {
                    if (rs.next()) {
                        adopcion.setId(rs.getInt(1));
                    }
                }
            }

            // 2. Actualizar estado en mascotas_adopcion a Adoptada
            String sqlUpdMascotaAdop = "UPDATE mascotas_adopcion SET estado = 'Adoptada', " +
                    "fecha_adopcion = ?, dueno_adoptante_id = ? WHERE id = ?";
            try (PreparedStatement stmtUpd = conn.prepareStatement(sqlUpdMascotaAdop)) {
                stmtUpd.setDate(1, Date.valueOf(adopcion.getFechaAdopcion()));
                stmtUpd.setInt(2, adopcion.getDuenoId());
                stmtUpd.setInt(3, adopcion.getMascotaAdopcionId());
                stmtUpd.executeUpdate();
            }

            // 3. Actualizar titularidad en tabla mascotas
            String sqlUpdMascota = "UPDATE mascotas SET dueno_id = ? WHERE id = ?";
            try (PreparedStatement stmtMasc = conn.prepareStatement(sqlUpdMascota)) {
                stmtMasc.setInt(1, adopcion.getDuenoId());
                stmtMasc.setInt(2, ma.getMascotaId());
                stmtMasc.executeUpdate();
            }

            // 4. Registrar en historial de transferencias si existe titular previo
            String sqlHist = "INSERT INTO historial_transferencias_mascota (mascota_id, dueno_anterior_id, " +
                    "dueno_nuevo_id, fecha_transferencia, motivo) VALUES (?, ?, ?, NOW(), ?)";
            try (PreparedStatement stmtHist = conn.prepareStatement(sqlHist)) {
                stmtHist.setInt(1, ma.getMascotaId());
                if (duenoAnteriorId != null && duenoAnteriorId > 0) {
                    stmtHist.setInt(2, duenoAnteriorId);
                } else {
                    stmtHist.setNull(2, Types.INTEGER);
                }
                stmtHist.setInt(3, adopcion.getDuenoId());
                stmtHist.setString(4, "Adopcion formal responsable y asignacion de nuevo tutor legal");
                stmtHist.executeUpdate();
            }

            LoggerManager.info(MODULO, "Adopcion formalizada con exito con contrato ID: " + adopcion.getId());
            return adopcion;
        });
    }

    // Busca un contrato de adopcion formalizado por su ID
    public Optional<Adopcion> buscarAdopcionPorId(int id) {
        String sql = "SELECT a.id, a.mascota_adopcion_id, a.dueno_id, a.fecha_adopcion, a.contrato_texto, " +
                "a.condiciones_especiales, a.seguimiento_requerido, a.fecha_primer_seguimiento, " +
                "m.nombre AS mascota_nombre, e.nombre AS especie_nombre, r.nombre AS raza_nombre, " +
                "d.nombre_completo AS adoptante_nombre, d.numero_documento AS adoptante_doc, " +
                "d.direccion AS adoptante_dir, d.telefono AS adoptante_tel, d.email AS adoptante_email " +
                "FROM adopciones a " +
                "INNER JOIN mascotas_adopcion ma ON a.mascota_adopcion_id = ma.id " +
                "INNER JOIN mascotas m ON ma.mascota_id = m.id " +
                "INNER JOIN especies e ON m.especie_id = e.id " +
                "LEFT JOIN razas r ON m.raza_id = r.id " +
                "INNER JOIN duenos d ON a.dueno_id = d.id " +
                "WHERE a.id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Adopcion a = new Adopcion(
                            rs.getInt("id"),
                            rs.getInt("mascota_adopcion_id"),
                            rs.getInt("dueno_id"),
                            rs.getDate("fecha_adopcion").toLocalDate(),
                            rs.getString("contrato_texto"),
                            rs.getString("condiciones_especiales"),
                            rs.getBoolean("seguimiento_requerido"),
                            rs.getDate("fecha_primer_seguimiento") != null ? rs.getDate("fecha_primer_seguimiento").toLocalDate() : null
                    );
                    a.setNombreMascota(rs.getString("mascota_nombre"));
                    a.setEspecieMascota(rs.getString("especie_nombre"));
                    a.setRazaMascota(rs.getString("raza_nombre"));
                    a.setNombreAdoptante(rs.getString("adoptante_nombre"));
                    a.setDocumentoAdoptante(rs.getString("adoptante_doc"));
                    a.setDireccionAdoptante(rs.getString("adoptante_dir"));
                    a.setTelefonoAdoptante(rs.getString("adoptante_tel"));
                    a.setEmailAdoptante(rs.getString("adoptante_email"));
                    return Optional.of(a);
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar adopcion ID " + id, e);
        }
        return Optional.empty();
    }

    // ==========================================
    // 2. SUBMODULO DE JORNADAS DE VACUNACION
    // ==========================================

    // Planifica o registra una nueva jornada comunitaria
    public JornadaVacunacion crearJornada(JornadaVacunacion j) {
        String sql = "INSERT INTO jornadas_vacunacion (nombre, fecha, hora_inicio, hora_fin, ubicacion, " +
                "descripcion, capacidad_maxima, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, j.getNombre());
            stmt.setDate(2, Date.valueOf(j.getFecha()));
            stmt.setTime(3, j.getHoraInicio() != null ? Time.valueOf(j.getHoraInicio()) : null);
            stmt.setTime(4, j.getHoraFin() != null ? Time.valueOf(j.getHoraFin()) : null);
            stmt.setString(5, j.getUbicacion());
            stmt.setString(6, j.getDescripcion());
            stmt.setObject(7, j.getCapacidadMaxima(), Types.INTEGER);
            stmt.setString(8, j.getEstado().getValor());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    j.setId(rs.getInt(1));
                }
            }
            LoggerManager.info(MODULO, "Jornada de vacunacion creada con ID: " + j.getId());
            return j;
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al crear jornada de vacunacion", e);
            throw new BaseDatosException("Error al registrar jornada: " + e.getMessage(), e);
        }
    }

    // Actualiza el estado de una jornada (Planificada -> En Curso -> Finalizada / Cancelada)
    public boolean actualizarEstadoJornada(int jornadaId, EstadoJornadaEnum nuevoEstado) {
        String sql = "UPDATE jornadas_vacunacion SET estado = ? WHERE id = ?";
        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nuevoEstado.getValor());
            stmt.setInt(2, jornadaId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al actualizar estado de jornada ID " + jornadaId, e);
            throw new BaseDatosException("Error al cambiar estado de jornada: " + e.getMessage(), e);
        }
    }

    // Consulta jornada por ID calculando el conteo de pacientes registrados
    public Optional<JornadaVacunacion> buscarJornadaPorId(int id) {
        String sql = "SELECT j.id, j.nombre, j.fecha, j.hora_inicio, j.hora_fin, j.ubicacion, " +
                "j.descripcion, j.capacidad_maxima, j.estado, " +
                "(SELECT COUNT(*) FROM registro_jornada_vacunacion r WHERE r.jornada_id = j.id) AS total_reg " +
                "FROM jornadas_vacunacion j WHERE j.id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearJornada(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al consultar jornada ID " + id, e);
        }
        return Optional.empty();
    }

    // Lista todas las jornadas registradas ordenadas cronologicamente
    public List<JornadaVacunacion> listarJornadas() {
        List<JornadaVacunacion> lista = new ArrayList<>();
        String sql = "SELECT j.id, j.nombre, j.fecha, j.hora_inicio, j.hora_fin, j.ubicacion, " +
                "j.descripcion, j.capacidad_maxima, j.estado, " +
                "(SELECT COUNT(*) FROM registro_jornada_vacunacion r WHERE r.jornada_id = j.id) AS total_reg " +
                "FROM jornadas_vacunacion j ORDER BY j.fecha DESC, j.id DESC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearJornada(rs));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar jornadas de vacunacion", e);
            throw new BaseDatosException("Error al listar jornadas: " + e.getMessage(), e);
        }
        return lista;
    }

    // Registra la aplicacion masiva de una vacuna (Fast-Track en cola continua)
    public RegistroJornadaVacunacion registrarPacienteJornadaTransaccional(RegistroJornadaVacunacion reg) {
        return TransaccionManager.ejecutarEnTransaccion(conn -> {
            String sql = "INSERT INTO registro_jornada_vacunacion (jornada_id, mascota_id, dueno_id, " +
                    "nombre_mascota_temporal, especie_id, es_callejero_comunitario, nombre_responsable_temporal, " +
                    "telefono_responsable_temporal, vacuna_id, veterinario_id, fecha_hora, lote_vacuna, " +
                    "proxima_dosis, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, reg.getJornadaId());
                if (reg.getMascotaId() != null && reg.getMascotaId() > 0) {
                    stmt.setInt(2, reg.getMascotaId());
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }

                if (reg.getDuenoId() != null && reg.getDuenoId() > 0) {
                    stmt.setInt(3, reg.getDuenoId());
                } else {
                    stmt.setNull(3, Types.INTEGER);
                }

                if (reg.getNombreMascotaTemporal() != null && !reg.getNombreMascotaTemporal().isEmpty()) {
                    stmt.setString(4, reg.getNombreMascotaTemporal());
                } else {
                    stmt.setNull(4, Types.VARCHAR);
                }

                stmt.setInt(5, reg.getEspecieId());
                stmt.setBoolean(6, reg.isEsCallejeroComunitario());

                if (reg.getNombreResponsableTemporal() != null && !reg.getNombreResponsableTemporal().isEmpty()) {
                    stmt.setString(7, reg.getNombreResponsableTemporal());
                } else {
                    stmt.setNull(7, Types.VARCHAR);
                }

                if (reg.getTelefonoResponsableTemporal() != null && !reg.getTelefonoResponsableTemporal().isEmpty()) {
                    stmt.setString(8, reg.getTelefonoResponsableTemporal());
                } else {
                    stmt.setNull(8, Types.VARCHAR);
                }

                stmt.setInt(9, reg.getVacunaId());

                if (reg.getVeterinarioId() != null && reg.getVeterinarioId() > 0) {
                    stmt.setInt(10, reg.getVeterinarioId());
                } else {
                    stmt.setNull(10, Types.INTEGER);
                }

                stmt.setTimestamp(11, Timestamp.valueOf(reg.getFechaHora()));
                stmt.setString(12, reg.getLoteVacuna());

                if (reg.getProximaDosis() != null) {
                    stmt.setDate(13, Date.valueOf(reg.getProximaDosis()));
                } else {
                    stmt.setNull(13, Types.DATE);
                }

                stmt.setString(14, reg.getObservaciones());

                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        reg.setId(rs.getInt(1));
                    }
                }
            }

            LoggerManager.info(MODULO, "Registro masivo en jornada ID " + reg.getJornadaId() + " completado. Registro ID: " + reg.getId());
            return reg;
        });
    }

    // Lista las atenciones de una jornada especifica
    public List<RegistroJornadaVacunacion> listarRegistrosPorJornada(int jornadaId) {
        List<RegistroJornadaVacunacion> lista = new ArrayList<>();
        String sql = "SELECT r.id, r.jornada_id, r.mascota_id, r.dueno_id, r.nombre_mascota_temporal, " +
                "r.especie_id, r.es_callejero_comunitario, r.nombre_responsable_temporal, " +
                "r.telefono_responsable_temporal, r.vacuna_id, r.veterinario_id, r.fecha_hora, " +
                "r.lote_vacuna, r.proxima_dosis, r.observaciones, " +
                "m.nombre AS mascota_nombre, e.nombre AS especie_nombre, i.nombre_producto AS vacuna_nombre, " +
                "v.nombre_completo AS veterinario_nombre " +
                "FROM registro_jornada_vacunacion r " +
                "LEFT JOIN mascotas m ON r.mascota_id = m.id " +
                "INNER JOIN especies e ON r.especie_id = e.id " +
                "INNER JOIN inventario i ON r.vacuna_id = i.id " +
                "LEFT JOIN veterinarios v ON r.veterinario_id = v.id " +
                "WHERE r.jornada_id = ? " +
                "ORDER BY r.id ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, jornadaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RegistroJornadaVacunacion reg = new RegistroJornadaVacunacion(
                            rs.getInt("id"),
                            rs.getInt("jornada_id"),
                            rs.getObject("mascota_id") != null ? rs.getInt("mascota_id") : null,
                            rs.getObject("dueno_id") != null ? rs.getInt("dueno_id") : null,
                            rs.getString("nombre_mascota_temporal"),
                            rs.getInt("especie_id"),
                            rs.getBoolean("es_callejero_comunitario"),
                            rs.getString("nombre_responsable_temporal"),
                            rs.getString("telefono_responsable_temporal"),
                            rs.getInt("vacuna_id"),
                            rs.getObject("veterinario_id") != null ? rs.getInt("veterinario_id") : null,
                            rs.getTimestamp("fecha_hora").toLocalDateTime(),
                            rs.getString("lote_vacuna"),
                            rs.getDate("proxima_dosis") != null ? rs.getDate("proxima_dosis").toLocalDate() : null,
                            rs.getString("observaciones")
                    );
                    reg.setNombreMascota(rs.getString("mascota_nombre"));
                    reg.setNombreEspecie(rs.getString("especie_nombre"));
                    reg.setNombreVacuna(rs.getString("vacuna_nombre"));
                    reg.setNombreVeterinario(rs.getString("veterinario_nombre"));
                    lista.add(reg);
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar registros de jornada ID " + jornadaId, e);
        }
        return lista;
    }

    // ==========================================
    // 3. SUBMODULO CLUB DE MASCOTAS FRECUENTES
    // ==========================================

    // Consulta la tabla de clasificacion / ranking general de miembros por puntos acumulados
    public List<ClubMascota> obtenerRankingClub() {
        List<ClubMascota> ranking = new ArrayList<>();
        String sql = "SELECT cm.id, cm.dueno_id, cm.puntos_acumulados, cm.puntos_canjeados, " +
                "cm.puntos_disponibles, cm.nivel, cm.fecha_inscripcion, cm.fecha_ultima_actualizacion, cm.activo, " +
                "d.nombre_completo AS dueno_nombre, d.numero_documento AS dueno_doc, " +
                "d.telefono AS dueno_tel, d.email AS dueno_email " +
                "FROM club_mascotas cm " +
                "INNER JOIN duenos d ON cm.dueno_id = d.id " +
                "WHERE cm.activo = TRUE " +
                "ORDER BY cm.puntos_acumulados DESC, cm.puntos_disponibles DESC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ranking.add(mapearClubMascota(rs));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al consultar ranking del club de mascotas", e);
            throw new BaseDatosException("Error al consultar ranking del club: " + e.getMessage(), e);
        }
        return ranking;
    }

    // Consulta la membresia activa de un cliente dueno
    public Optional<ClubMascota> buscarClubPorDuenoId(int duenoId) {
        String sql = "SELECT cm.id, cm.dueno_id, cm.puntos_acumulados, cm.puntos_canjeados, " +
                "cm.puntos_disponibles, cm.nivel, cm.fecha_inscripcion, cm.fecha_ultima_actualizacion, cm.activo, " +
                "d.nombre_completo AS dueno_nombre, d.numero_documento AS dueno_doc, " +
                "d.telefono AS dueno_tel, d.email AS dueno_email " +
                "FROM club_mascotas cm " +
                "INNER JOIN duenos d ON cm.dueno_id = d.id " +
                "WHERE cm.dueno_id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, duenoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearClubMascota(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al consultar club de dueno ID " + duenoId, e);
        }
        return Optional.empty();
    }

    // Inscribe a un dueno en el Club de Mascotas con nivel Bronce inicial
    public ClubMascota inscribirDuenoEnClub(int duenoId) {
        String sql = "INSERT INTO club_mascotas (dueno_id, puntos_acumulados, puntos_canjeados, " +
                "puntos_disponibles, nivel, fecha_inscripcion, activo) " +
                "VALUES (?, 0, 0, 0, 'Bronce', CURDATE(), TRUE)";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, duenoId);
            stmt.executeUpdate();

            ClubMascota cm = new ClubMascota();
            cm.setDuenoId(duenoId);
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    cm.setId(rs.getInt(1));
                }
            }
            LoggerManager.info(MODULO, "Dueno ID " + duenoId + " inscrito en Club de Mascotas con ID: " + cm.getId());
            return cm;
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al inscribir dueno ID " + duenoId + " en el Club", e);
            throw new BaseDatosException("Error al inscribir dueno en club: " + e.getMessage(), e);
        }
    }

    // Lista el catalogo de beneficios vigentes disponibles para redencion
    public List<BeneficioClub> listarBeneficiosDisponibles() {
        List<BeneficioClub> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion, nivel_requerido, puntos_necesarios, tipo_beneficio, " +
                "valor_beneficio, activo FROM beneficios_club WHERE activo = TRUE " +
                "ORDER BY puntos_necesarios ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new BeneficioClub(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        NivelClubEnum.desdeString(rs.getString("nivel_requerido")),
                        rs.getInt("puntos_necesarios"),
                        rs.getString("tipo_beneficio"),
                        rs.getBigDecimal("valor_beneficio"),
                        rs.getBoolean("activo")
                ));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar beneficios del club", e);
        }
        return lista;
    }

    // Busca un beneficio por ID
    public Optional<BeneficioClub> buscarBeneficioPorId(int id) {
        String sql = "SELECT id, nombre, descripcion, nivel_requerido, puntos_necesarios, tipo_beneficio, " +
                "valor_beneficio, activo FROM beneficios_club WHERE id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new BeneficioClub(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("descripcion"),
                            NivelClubEnum.desdeString(rs.getString("nivel_requerido")),
                            rs.getInt("puntos_necesarios"),
                            rs.getString("tipo_beneficio"),
                            rs.getBigDecimal("valor_beneficio"),
                            rs.getBoolean("activo")
                    ));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al consultar beneficio ID " + id, e);
        }
        return Optional.empty();
    }

    // Ejecuta transaccionalmente el canje de puntos por un beneficio
    public CanjeBeneficio canjearPuntosTransaccional(int duenoId, BeneficioClub beneficio) {
        return TransaccionManager.ejecutarEnTransaccion(conn -> {
            // 1. Obtener y bloquear registro del club del dueno
            String sqlClub = "SELECT id, puntos_disponibles, nivel FROM club_mascotas " +
                    "WHERE dueno_id = ? AND activo = TRUE FOR UPDATE";

            int clubId;
            int saldoAnterior;
            NivelClubEnum nivelActual;

            try (PreparedStatement stmtC = conn.prepareStatement(sqlClub)) {
                stmtC.setInt(1, duenoId);
                try (ResultSet rsC = stmtC.executeQuery()) {
                    if (!rsC.next()) {
                        throw new BaseDatosException("El dueno no tiene cuenta activa en el Club de Mascotas.");
                    }
                    clubId = rsC.getInt("id");
                    saldoAnterior = rsC.getInt("puntos_disponibles");
                    nivelActual = NivelClubEnum.desdeString(rsC.getString("nivel"));
                }
            }

            // Validar nivel y saldo suficiente
            if (!nivelActual.esMayorOIgualQue(beneficio.getNivelRequerido())) {
                throw new BaseDatosException(String.format("Nivel insuficiente: Se requiere nivel %s o superior (Nivel actual: %s).",
                        beneficio.getNivelRequerido(), nivelActual));
            }
            if (saldoAnterior < beneficio.getPuntosNecesarios()) {
                throw new BaseDatosException(String.format("Puntos insuficientes: Requiere %d pts y dispone de %d pts.",
                        beneficio.getPuntosNecesarios(), saldoAnterior));
            }

            int saldoNuevo = saldoAnterior - beneficio.getPuntosNecesarios();

            // 2. Actualizar puntos en club_mascotas
            String sqlUpdClub = "UPDATE club_mascotas SET puntos_canjeados = puntos_canjeados + ?, " +
                    "puntos_disponibles = ? WHERE id = ?";
            try (PreparedStatement stmtUpd = conn.prepareStatement(sqlUpdClub)) {
                stmtUpd.setInt(1, beneficio.getPuntosNecesarios());
                stmtUpd.setInt(2, saldoNuevo);
                stmtUpd.setInt(3, clubId);
                stmtUpd.executeUpdate();
            }

            // 3. Insertar registro en canjes_beneficios
            CanjeBeneficio canje = new CanjeBeneficio(
                    null, clubId, beneficio.getId(), LocalDateTime.now(),
                    beneficio.getPuntosNecesarios(), "Aplicado", LocalDate.now().plusMonths(1), null
            );

            String sqlCanje = "INSERT INTO canjes_beneficios (club_mascotas_id, beneficio_id, fecha_canje, " +
                    "puntos_canjeados, estado, fecha_expiracion) VALUES (?, ?, NOW(), ?, 'Aplicado', DATE_ADD(CURDATE(), INTERVAL 30 DAY))";
            try (PreparedStatement stmtCanje = conn.prepareStatement(sqlCanje, Statement.RETURN_GENERATED_KEYS)) {
                stmtCanje.setInt(1, clubId);
                stmtCanje.setInt(2, beneficio.getId());
                stmtCanje.setInt(3, beneficio.getPuntosNecesarios());
                stmtCanje.executeUpdate();
                try (ResultSet rsK = stmtCanje.getGeneratedKeys()) {
                    if (rsK.next()) {
                        canje.setId(rsK.getInt(1));
                    }
                }
            }

            // 4. Insertar transaccion en transacciones_puntos
            String sqlTrans = "INSERT INTO transacciones_puntos (club_mascotas_id, factura_id, puntos, " +
                    "tipo, fecha, descripcion, saldo_anterior, saldo_nuevo) " +
                    "VALUES (?, NULL, ?, 'Canjeados', NOW(), ?, ?, ?)";
            try (PreparedStatement stmtTr = conn.prepareStatement(sqlTrans)) {
                stmtTr.setInt(1, clubId);
                stmtTr.setInt(2, beneficio.getPuntosNecesarios());
                stmtTr.setString(3, "Canje de beneficio: " + beneficio.getNombre());
                stmtTr.setInt(4, saldoAnterior);
                stmtTr.setInt(5, saldoNuevo);
                stmtTr.executeUpdate();
            }

            canje.setNombreBeneficio(beneficio.getNombre());
            LoggerManager.info(MODULO, "Canje de beneficio exitoso ID " + canje.getId() + " (-" + beneficio.getPuntosNecesarios() + " pts)");
            return canje;
        });
    }

    // ==========================================
    // METODOS AUXILIARES DE MAPEO
    // ==========================================

    private MascotaAdopcion mapearMascotaAdopcion(ResultSet rs) throws SQLException {
        MascotaAdopcion ma = new MascotaAdopcion(
                rs.getInt("id"),
                rs.getInt("mascota_id"),
                rs.getString("historia"),
                rs.getString("temperamento"),
                rs.getString("necesidades_especiales"),
                rs.getDate("fecha_ingreso").toLocalDate(),
                EstadoAdopcionEnum.desdeString(rs.getString("estado")),
                rs.getDate("fecha_adopcion") != null ? rs.getDate("fecha_adopcion").toLocalDate() : null,
                rs.getObject("dueno_adoptante_id") != null ? rs.getInt("dueno_adoptante_id") : null,
                rs.getString("foto_adicional_url")
        );
        ma.setNombreMascota(rs.getString("mascota_nombre"));
        ma.setEspecieMascota(rs.getString("especie_nombre"));
        ma.setRazaMascota(rs.getString("raza_nombre"));
        ma.setSexoMascota(rs.getString("sexo"));

        Date fNac = rs.getDate("fecha_nacimiento");
        if (fNac != null) {
            int edad = LocalDate.now().getYear() - fNac.toLocalDate().getYear();
            ma.setEdadMascota(Math.max(0, edad));
        }
        return ma;
    }

    private JornadaVacunacion mapearJornada(ResultSet rs) throws SQLException {
        JornadaVacunacion j = new JornadaVacunacion(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getDate("fecha").toLocalDate(),
                rs.getTime("hora_inicio") != null ? rs.getTime("hora_inicio").toLocalTime() : null,
                rs.getTime("hora_fin") != null ? rs.getTime("hora_fin").toLocalTime() : null,
                rs.getString("ubicacion"),
                rs.getString("descripcion"),
                rs.getObject("capacidad_maxima") != null ? rs.getInt("capacidad_maxima") : null,
                EstadoJornadaEnum.desdeString(rs.getString("estado"))
        );
        j.setTotalRegistrados(rs.getInt("total_reg"));
        return j;
    }

    private ClubMascota mapearClubMascota(ResultSet rs) throws SQLException {
        ClubMascota cm = new ClubMascota(
                rs.getInt("id"),
                rs.getInt("dueno_id"),
                rs.getInt("puntos_acumulados"),
                rs.getInt("puntos_canjeados"),
                rs.getInt("puntos_disponibles"),
                NivelClubEnum.desdeString(rs.getString("nivel")),
                rs.getDate("fecha_inscripcion").toLocalDate(),
                rs.getTimestamp("fecha_ultima_actualizacion").toLocalDateTime(),
                rs.getBoolean("activo")
        );
        cm.setNombreDueno(rs.getString("dueno_nombre"));
        cm.setDocumentoDueno(rs.getString("dueno_doc"));
        cm.setTelefonoDueno(rs.getString("dueno_tel"));
        cm.setEmailDueno(rs.getString("dueno_email"));
        return cm;
    }
}
