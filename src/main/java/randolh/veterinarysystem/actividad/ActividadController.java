package randolh.veterinarysystem.actividad;

import randolh.veterinarysystem.core.config.ConexionBD;
import randolh.veterinarysystem.core.exception.ValidacionException;
import randolh.veterinarysystem.core.exportador.ExportadorFactory;
import randolh.veterinarysystem.core.exportador.FormatoExportacion;
import randolh.veterinarysystem.core.exportador.contrato.ContratoAdopcionExportData;
import randolh.veterinarysystem.core.patterns.observer.GestorEventos;
import randolh.veterinarysystem.core.utils.LoggerManager;
import randolh.veterinarysystem.dueno.Dueno;
import randolh.veterinarysystem.dueno.DuenoDAO;
import randolh.veterinarysystem.inventario.InventarioDAO;
import randolh.veterinarysystem.inventario.Producto;
import randolh.veterinarysystem.mascota.Mascota;
import randolh.veterinarysystem.mascota.MascotaDAO;
import randolh.veterinarysystem.veterinario.Veterinario;
import randolh.veterinarysystem.veterinario.VeterinarioDAO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

// Controlador de logica de negocio para adopciones, jornadas de vacunacion masiva y Club de Mascotas
public class ActividadController {

    private static final String MODULO = "ActividadController";

    private final ActividadDAO actividadDAO;
    private final MascotaDAO mascotaDAO;
    private final DuenoDAO duenoDAO;
    private final InventarioDAO inventarioDAO;
    private final VeterinarioDAO veterinarioDAO;

    public ActividadController() {
        this.actividadDAO = new ActividadDAO();
        this.mascotaDAO = new MascotaDAO();
        this.duenoDAO = new DuenoDAO();
        this.inventarioDAO = new InventarioDAO();
        this.veterinarioDAO = new VeterinarioDAO();
    }

    public ActividadController(ActividadDAO actividadDAO, MascotaDAO mascotaDAO, DuenoDAO duenoDAO,
                               InventarioDAO inventarioDAO, VeterinarioDAO veterinarioDAO) {
        this.actividadDAO = actividadDAO != null ? actividadDAO : new ActividadDAO();
        this.mascotaDAO = mascotaDAO != null ? mascotaDAO : new MascotaDAO();
        this.duenoDAO = duenoDAO != null ? duenoDAO : new DuenoDAO();
        this.inventarioDAO = inventarioDAO != null ? inventarioDAO : new InventarioDAO();
        this.veterinarioDAO = veterinarioDAO != null ? veterinarioDAO : new VeterinarioDAO();
    }

    // ==========================================
    // 1. MODULO DE ADOPCIONES RESPONSABLES
    // ==========================================

    // Lista las mascotas que se encuentran disponibles para adopcion
    public List<MascotaAdopcion> listarMascotasDisponibles() {
        return actividadDAO.listarMascotasAdopcionDisponibles();
    }

    // Lista todo el historico de mascotas registradas en adopcion
    public List<MascotaAdopcion> listarTodasMascotasAdopcion() {
        return actividadDAO.listarTodasMascotasAdopcion();
    }

    // Consulta una mascota en adopcion por su ID
    public Optional<MascotaAdopcion> buscarMascotaAdopcion(int id) {
        return actividadDAO.buscarMascotaAdopcionPorId(id);
    }

    // Da de alta una mascota en el programa de adopciones
    public MascotaAdopcion registrarMascotaParaAdopcion(int mascotaId, String historia, String temperamento,
                                                         String necesidades, String fotoUrl) {
        Optional<Mascota> mascOpt = mascotaDAO.buscarPorId(mascotaId);
        if (mascOpt.isEmpty()) {
            throw new ValidacionException("La mascota con ID " + mascotaId + " no existe.");
        }

        MascotaAdopcion ma = new MascotaAdopcion(
                null, mascotaId, historia, temperamento, necesidades,
                LocalDate.now(), EstadoAdopcionEnum.DISPONIBLE, null, null, fotoUrl
        );
        return actividadDAO.registrarMascotaParaAdopcion(ma);
    }

    // Formaliza un contrato legal de adopcion responsable y genera los documentos en texto y OpenPDF
    public Adopcion formalizarAdopcion(int mascotaAdopcionId, int adoptanteId, String condicionesEspeciales, boolean seguimientoRequerido) {
        Optional<MascotaAdopcion> maOpt = actividadDAO.buscarMascotaAdopcionPorId(mascotaAdopcionId);
        if (maOpt.isEmpty()) {
            throw new ValidacionException("No existe registro de adopcion para el ID " + mascotaAdopcionId);
        }
        MascotaAdopcion ma = maOpt.get();
        if (ma.getEstado() != EstadoAdopcionEnum.DISPONIBLE) {
            throw new ValidacionException("La mascota seleccionada no se encuentra disponible (Estado actual: " + ma.getEstado() + ").");
        }

        Optional<Dueno> duenoOpt = duenoDAO.buscarPorId(adoptanteId);
        if (duenoOpt.isEmpty()) {
            throw new ValidacionException("El adoptante con ID " + adoptanteId + " no se encuentra registrado.");
        }
        Dueno adoptante = duenoOpt.get();
        if (!adoptante.isActivo()) {
            throw new ValidacionException("El adoptante " + adoptante.getNombreCompleto() + " se encuentra inactivo.");
        }

        Optional<Mascota> mascOpt = mascotaDAO.buscarPorId(ma.getMascotaId());
        Integer duenoAnteriorId = mascOpt.map(Mascota::getDuenoId).orElse(null);

        // Generar texto legal del contrato
        String fechaHoy = LocalDate.now().toString();
        String fechaSeguimiento = LocalDate.now().plusDays(30).toString();
        String contratoTexto = String.format("CONTRATO DE ADOPCION RESPONSABLE - TITULAR: %s (CC: %s) - BENEFICIARIO: %s - FECHA: %s",
                adoptante.getNombreCompleto(), adoptante.getNumeroDocumento(), ma.getNombreMascota(), fechaHoy);

        Adopcion adopcion = new Adopcion(
                null, mascotaAdopcionId, adoptanteId, LocalDate.now(),
                contratoTexto, condicionesEspeciales, seguimientoRequerido,
                LocalDate.now().plusDays(30)
        );

        adopcion.setNombreMascota(ma.getNombreMascota());
        adopcion.setEspecieMascota(ma.getEspecieMascota());
        adopcion.setRazaMascota(ma.getRazaMascota());
        adopcion.setNombreAdoptante(adoptante.getNombreCompleto());
        adopcion.setDocumentoAdoptante(adoptante.getNumeroDocumento());
        adopcion.setDireccionAdoptante(adoptante.getDireccion());
        adopcion.setTelefonoAdoptante(adoptante.getTelefono());
        adopcion.setEmailAdoptante(adoptante.getEmail());

        // Ejecutar transaccion ACID
        Adopcion adopcionGuardada = actividadDAO.formalizarAdopcionTransaccional(adopcion, ma, duenoAnteriorId);

        // Exportar a archivos duales (Texto plano y OpenPDF formal)
        exportarContratos(adopcionGuardada, ma, adoptante);

        LoggerManager.info(MODULO, String.format("Adopcion formalizada con exito: Contrato ADOP-%05d | Mascota: %s | Adoptante: %s",
                adopcionGuardada.getId(), ma.getNombreMascota(), adoptante.getNombreCompleto()));

        return adopcionGuardada;
    }

    // Exporta contrato en formato de texto plano y en PDF estandar formal
    public void exportarContratos(Adopcion adopcion, MascotaAdopcion ma, Dueno adoptante) {
        ConexionBD config = ConexionBD.getInstancia();
        String fechaHoy = adopcion.getFechaAdopcion() != null ? adopcion.getFechaAdopcion().toString() : LocalDate.now().toString();
        String fechaSeg = adopcion.getFechaPrimerSeguimiento() != null ? adopcion.getFechaPrimerSeguimiento().toString() : LocalDate.now().plusDays(30).toString();
        String edadStr = (ma.getEdadMascota() != null ? ma.getEdadMascota() + " anos aprox." : "Desconocida");

        ContratoAdopcionExportData data = ContratoAdopcionExportData.builder()
                .conClinica(config.getNombreClinica(), config.getNitClinica(), config.getTelefonoClinica(), config.getDireccionClinica())
                .conAdopcion(adopcion.getId(), fechaHoy, fechaSeg)
                .conAdoptante(adoptante.getNombreCompleto(), adoptante.getNumeroDocumento(), adoptante.getTelefono(), adoptante.getDireccion(), adoptante.getEmail())
                .conMascota(ma.getNombreMascota(), ma.getEspecieMascota(), ma.getRazaMascota(), ma.getSexoMascota(), edadStr)
                .conDetalles(ma.getHistoria(), ma.getTemperamento(), ma.getNecesidadesEspeciales(), adopcion.getCondicionesEspeciales())
                .build();

        ExportadorFactory.getExportadorContrato(FormatoExportacion.TEXTO).exportar(data);
        ExportadorFactory.getExportadorContrato(FormatoExportacion.PDF).exportar(data);
    }

    // Busca un contrato por su ID
    public Optional<Adopcion> buscarAdopcion(int id) {
        return actividadDAO.buscarAdopcionPorId(id);
    }

    // ==========================================
    // 2. MODULO JORNADAS DE VACUNACION FAST-TRACK
    // ==========================================

    // Planifica una nueva jornada
    public JornadaVacunacion crearJornada(String nombre, LocalDate fecha, LocalTime horaInicio,
                                          LocalTime horaFin, String ubicacion, String descripcion,
                                          Integer capacidad) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidacionException("El nombre de la jornada es obligatorio.");
        }
        if (fecha == null) {
            fecha = LocalDate.now();
        }

        JornadaVacunacion j = new JornadaVacunacion(
                null, nombre.trim(), fecha, horaInicio, horaFin, ubicacion, descripcion,
                capacidad != null && capacidad > 0 ? capacidad : 100, EstadoJornadaEnum.PLANIFICADA
        );
        return actividadDAO.crearJornada(j);
    }

    // Cambia el estado de una jornada
    public boolean actualizarEstadoJornada(int jornadaId, EstadoJornadaEnum nuevoEstado) {
        Optional<JornadaVacunacion> jOpt = actividadDAO.buscarJornadaPorId(jornadaId);
        if (jOpt.isEmpty()) {
            throw new ValidacionException("La jornada ID " + jornadaId + " no existe.");
        }
        return actividadDAO.actualizarEstadoJornada(jornadaId, nuevoEstado);
    }

    // Lista todas las jornadas registradas
    public List<JornadaVacunacion> listarJornadas() {
        return actividadDAO.listarJornadas();
    }

    // Busca una jornada por ID
    public Optional<JornadaVacunacion> buscarJornada(int id) {
        return actividadDAO.buscarJornadaPorId(id);
    }

    // Fast-Track Perfil 1: Paciente regular de la clinica
    public RegistroJornadaVacunacion registrarPacienteClinica(int jornadaId, int mascotaId, int vacunaId,
                                                              Integer veterinarioId, String loteVacuna, String observaciones) {
        Optional<Mascota> mascOpt = mascotaDAO.buscarPorId(mascotaId);
        if (mascOpt.isEmpty()) {
            throw new ValidacionException("La mascota con ID " + mascotaId + " no se encuentra en el sistema.");
        }
        Mascota m = mascOpt.get();

        validarVacunaDisponible(vacunaId);

        RegistroJornadaVacunacion reg = RegistroJornadaVacunacion.paraPacienteClinica(
                jornadaId, m.getId(), m.getDuenoId(), m.getEspecieId(), vacunaId, veterinarioId, loteVacuna, observaciones
        );
        reg.setNombreMascota(m.getNombre());

        RegistroJornadaVacunacion guardado = actividadDAO.registrarPacienteJornadaTransaccional(reg);
        notificarAlertaInventarioSiAplica(vacunaId);
        return guardado;
    }

    // Fast-Track Perfil 2: Registro rapido comunitario (con vecino/responsable)
    public RegistroJornadaVacunacion registrarPacienteComunitario(int jornadaId, String nombreAnimal, int especieId,
                                                                  String nombreVecino, String telefonoVecino,
                                                                  int vacunaId, Integer veterinarioId,
                                                                  String loteVacuna, String observaciones) {
        if (nombreAnimal == null || nombreAnimal.trim().isEmpty()) {
            throw new ValidacionException("Debe ingresar el nombre del animal.");
        }
        validarVacunaDisponible(vacunaId);

        RegistroJornadaVacunacion reg = RegistroJornadaVacunacion.paraRegistroComunitario(
                jornadaId, nombreAnimal.trim(), especieId, nombreVecino, telefonoVecino,
                vacunaId, veterinarioId, loteVacuna, observaciones
        );

        RegistroJornadaVacunacion guardado = actividadDAO.registrarPacienteJornadaTransaccional(reg);
        notificarAlertaInventarioSiAplica(vacunaId);
        return guardado;
    }

    // Fast-Track Perfil 3: Animal callejero / comunitario sin titular
    public RegistroJornadaVacunacion registrarAnimalCallejero(int jornadaId, String nombreTemporal, int especieId,
                                                              String nombrePadrino, String telefonoPadrino,
                                                              int vacunaId, Integer veterinarioId,
                                                              String loteVacuna, String observaciones) {
        if (nombreTemporal == null || nombreTemporal.trim().isEmpty()) {
            nombreTemporal = "Animal Comunitario #" + System.currentTimeMillis() % 1000;
        }
        validarVacunaDisponible(vacunaId);

        RegistroJornadaVacunacion reg = RegistroJornadaVacunacion.paraAnimalCallejero(
                jornadaId, nombreTemporal.trim(), especieId, nombrePadrino, telefonoPadrino,
                vacunaId, veterinarioId, loteVacuna, observaciones
        );

        RegistroJornadaVacunacion guardado = actividadDAO.registrarPacienteJornadaTransaccional(reg);
        notificarAlertaInventarioSiAplica(vacunaId);
        return guardado;
    }

    // Lista las atenciones de una jornada
    public List<RegistroJornadaVacunacion> listarRegistrosDeJornada(int jornadaId) {
        return actividadDAO.listarRegistrosPorJornada(jornadaId);
    }

    // Valida que el biologico exista y tenga stock disponible
    private void validarVacunaDisponible(int vacunaId) {
        Optional<Producto> prodOpt = inventarioDAO.buscarProductoPorId(vacunaId);
        if (prodOpt.isEmpty()) {
            throw new ValidacionException("El producto biologico / vacuna con ID " + vacunaId + " no existe.");
        }
        Producto p = prodOpt.get();
        if (p.getCantidadStock() <= 0) {
            throw new ValidacionException("Stock agotado para la vacuna '" + p.getNombreProducto() + "'.");
        }
        if (p.getFechaVencimiento() != null && p.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw new ValidacionException("La vacuna '" + p.getNombreProducto() + "' ha caducado (" + p.getFechaVencimiento() + ").");
        }
    }

    // Notifica al gestor de eventos si la deduccion deja el producto por debajo de stock minimo
    private void notificarAlertaInventarioSiAplica(int vacunaId) {
        try {
            Optional<Producto> prodOpt = inventarioDAO.buscarProductoPorId(vacunaId);
            if (prodOpt.isPresent()) {
                Producto p = prodOpt.get();
                if (p.getCantidadStock() <= p.getStockMinimo()) {
                    GestorEventos.getInstancia().notificar("INVENTARIO_STOCK_MINIMO", p);
                }
            }
        } catch (Exception e) {
            LoggerManager.warning(MODULO, "No se pudo notificar evento de stock de inventario: " + e.getMessage());
        }
    }

    // ==========================================
    // 3. SUBMODULO CLUB DE MASCOTAS FRECUENTES
    // ==========================================

    // Retorna la tabla de clasificacion / ranking general
    public List<ClubMascota> obtenerRankingClub() {
        return actividadDAO.obtenerRankingClub();
    }

    // Consulta el estado de membresia de un dueno
    public Optional<ClubMascota> buscarClubPorDueno(int duenoId) {
        return actividadDAO.buscarClubPorDuenoId(duenoId);
    }

    // Inscribe a un dueno en el Club
    public ClubMascota inscribirDuenoEnClub(int duenoId) {
        Optional<Dueno> duenoOpt = duenoDAO.buscarPorId(duenoId);
        if (duenoOpt.isEmpty()) {
            throw new ValidacionException("El cliente con ID " + duenoId + " no existe.");
        }
        Optional<ClubMascota> existente = actividadDAO.buscarClubPorDuenoId(duenoId);
        if (existente.isPresent()) {
            throw new ValidacionException("El cliente ya cuenta con membresia activa en el Club (ID: " + existente.get().getId() + ").");
        }
        return actividadDAO.inscribirDuenoEnClub(duenoId);
    }

    // Lista el catalogo de beneficios canjeables
    public List<BeneficioClub> listarBeneficios() {
        return actividadDAO.listarBeneficiosDisponibles();
    }

    // Canjea puntos por un beneficio
    public CanjeBeneficio canjearBeneficio(int duenoId, int beneficioId) {
        Optional<BeneficioClub> benOpt = actividadDAO.buscarBeneficioPorId(beneficioId);
        if (benOpt.isEmpty()) {
            throw new ValidacionException("El beneficio con ID " + beneficioId + " no existe.");
        }
        BeneficioClub beneficio = benOpt.get();
        if (!beneficio.isActivo()) {
            throw new ValidacionException("El beneficio '" + beneficio.getNombre() + "' no se encuentra activo.");
        }

        return actividadDAO.canjearPuntosTransaccional(duenoId, beneficio);
    }

    public ActividadDAO getActividadDAO() { return actividadDAO; }
    public MascotaDAO getMascotaDAO() { return mascotaDAO; }
    public DuenoDAO getDuenoDAO() { return duenoDAO; }
    public InventarioDAO getInventarioDAO() { return inventarioDAO; }
    public VeterinarioDAO getVeterinarioDAO() { return veterinarioDAO; }
}
