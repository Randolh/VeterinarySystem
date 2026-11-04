package randolh.veterinarysystem.mascota;

import randolh.veterinarysystem.consulta.ConsultaDAO;
import randolh.veterinarysystem.consulta.ProcedimientoEspecial;
import randolh.veterinarysystem.core.config.ConexionBD;
import randolh.veterinarysystem.core.exception.ValidacionException;
import randolh.veterinarysystem.core.exportador.ExportadorFactory;
import randolh.veterinarysystem.core.exportador.FormatoExportacion;
import randolh.veterinarysystem.core.exportador.carnet.CarnetSaludExportData;
import randolh.veterinarysystem.core.utils.LoggerManager;
import randolh.veterinarysystem.dueno.Dueno;
import randolh.veterinarysystem.dueno.DuenoDAO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Controlador de logica de negocio para pacientes mascotas
public class MascotaController {

    private static final String MODULO = "MascotaController";

    private final MascotaDAO mascotaDAO;
    private final DuenoDAO duenoDAO;
    private final ConsultaDAO consultaDAO;

    public MascotaController() {
        this.mascotaDAO = new MascotaDAO();
        this.duenoDAO = new DuenoDAO();
        this.consultaDAO = new ConsultaDAO();
    }

    public MascotaController(MascotaDAO mascotaDAO, DuenoDAO duenoDAO) {
        this.mascotaDAO = mascotaDAO;
        this.duenoDAO = duenoDAO;
        this.consultaDAO = new ConsultaDAO();
    }

    public MascotaController(MascotaDAO mascotaDAO, DuenoDAO duenoDAO, ConsultaDAO consultaDAO) {
        this.mascotaDAO = mascotaDAO;
        this.duenoDAO = duenoDAO;
        this.consultaDAO = consultaDAO;
    }

    // Registra una nueva mascota validando reglas clinicas y dueno activo
    public Mascota registrarMascota(Mascota mascota) {
        validarMascota(mascota);

        // Validacion de unicidad de microchip si fue suministrado
        if (mascota.getMicrochip() != null && !mascota.getMicrochip().trim().isEmpty()) {
            Optional<Mascota> existenteChip = mascotaDAO.buscarPorMicrochip(mascota.getMicrochip().trim());
            if (existenteChip.isPresent()) {
                throw new ValidacionException("Ya existe un paciente registrado con el microchip: " + mascota.getMicrochip());
            }
        }

        LoggerManager.info(MODULO, "Registrando paciente: " + mascota.getNombre() + " para dueno ID: " + mascota.getDuenoId());
        return mascotaDAO.insertar(mascota);
    }

    // Actualiza datos de la mascota
    public boolean actualizarMascota(Mascota mascota) {
        if (mascota.getId() == null || mascota.getId() <= 0) {
            throw new ValidacionException("El ID de la mascota es obligatorio para actualizar.");
        }

        validarMascota(mascota);

        // Validacion de unicidad de microchip contra otros registros
        if (mascota.getMicrochip() != null && !mascota.getMicrochip().trim().isEmpty()) {
            Optional<Mascota> existenteChip = mascotaDAO.buscarPorMicrochip(mascota.getMicrochip().trim());
            if (existenteChip.isPresent() && !existenteChip.get().getId().equals(mascota.getId())) {
                throw new ValidacionException("El microchip '" + mascota.getMicrochip() + "' ya pertenece a otro paciente.");
            }
        }

        LoggerManager.info(MODULO, "Actualizando paciente ID: " + mascota.getId());
        return mascotaDAO.actualizar(mascota);
    }

    // Transfiere la propiedad de una mascota a un nuevo dueno
    public void transferirPropiedad(int mascotaId, int nuevoDuenoId, String motivo) {
        if (mascotaId <= 0) {
            throw new ValidacionException("ID de mascota invalido.");
        }
        if (nuevoDuenoId <= 0) {
            throw new ValidacionException("ID de nuevo dueno invalido.");
        }

        // Validar que la mascota exista
        Optional<Mascota> mascotaOpt = mascotaDAO.buscarPorId(mascotaId);
        if (mascotaOpt.isEmpty()) {
            throw new ValidacionException("No existe ninguna mascota con el ID: " + mascotaId);
        }

        Mascota mascota = mascotaOpt.get();
        if (mascota.getDuenoId() == nuevoDuenoId) {
            throw new ValidacionException("La mascota ya pertenece al cliente especificado.");
        }

        // Validar que el nuevo dueno exista y este activo
        Optional<Dueno> nuevoDuenoOpt = duenoDAO.buscarPorId(nuevoDuenoId);
        if (nuevoDuenoOpt.isEmpty()) {
            throw new ValidacionException("No existe ningun cliente con el ID: " + nuevoDuenoId);
        }
        if (!nuevoDuenoOpt.get().isActivo()) {
            throw new ValidacionException("El cliente destinatario se encuentra inactivo.");
        }

        if (motivo == null || motivo.trim().isEmpty()) {
            motivo = "Traspaso de titularidad";
        }

        LoggerManager.info(MODULO, String.format("Transfiriendo mascota %d ('%s') de dueno %d a nuevo dueno %d",
                mascotaId, mascota.getNombre(), mascota.getDuenoId(), nuevoDuenoId));

        mascotaDAO.transferirPropiedad(mascotaId, nuevoDuenoId, motivo);
    }

    // Desactivacion logica de paciente
    public boolean desactivarMascota(int id) {
        if (id <= 0) {
            throw new ValidacionException("ID invalido para desactivacion.");
        }
        Optional<Mascota> opt = mascotaDAO.buscarPorId(id);
        if (opt.isEmpty()) {
            throw new ValidacionException("No se encontro ningun paciente con el ID: " + id);
        }
        LoggerManager.info(MODULO, "Desactivando paciente ID: " + id);
        return mascotaDAO.eliminarLogico(id);
    }

    // Consultas
    public Optional<Mascota> obtenerPorId(int id) {
        if (id <= 0) throw new ValidacionException("ID de consulta invalido.");
        return mascotaDAO.buscarPorId(id);
    }

    public Optional<Mascota> obtenerPorMicrochip(String microchip) {
        if (microchip == null || microchip.trim().isEmpty()) {
            throw new ValidacionException("Debe ingresar un codigo de microchip.");
        }
        return mascotaDAO.buscarPorMicrochip(microchip.trim());
    }

    public List<Mascota> listarTodas() {
        return mascotaDAO.listarTodas();
    }

    public List<Mascota> listarPorDueno(int duenoId) {
        if (duenoId <= 0) throw new ValidacionException("ID de dueno invalido.");
        return mascotaDAO.listarPorDueno(duenoId);
    }

    public List<Mascota> buscarPorNombre(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            throw new ValidacionException("Debe ingresar un termino de busqueda.");
        }
        return mascotaDAO.buscarPorNombre(termino.trim());
    }

    public List<MascotaDAO.CatalogoItem> listarEspecies() {
        return mascotaDAO.listarEspecies();
    }

    public List<MascotaDAO.CatalogoItem> listarRazasPorEspecie(int especieId) {
        return mascotaDAO.listarRazasPorEspecie(especieId);
    }

    // Historial de vacunas y biologicos aplicados
    public List<MascotaDAO.VacunaHistorialItem> listarVacunasPorMascota(int mascotaId) {
        if (mascotaId <= 0) throw new ValidacionException("ID de mascota invalido.");
        return mascotaDAO.listarVacunasPorMascota(mascotaId);
    }

    // Historial de procedimientos quirurgicos y cirugias
    public List<ProcedimientoEspecial> listarProcedimientosPorMascota(int mascotaId) {
        if (mascotaId <= 0) throw new ValidacionException("ID de mascota invalido.");
        return consultaDAO.listarProcedimientosPorMascota(mascotaId);
    }

    // Exporta el carnet de salud de una mascota en el formato seleccionado (TXT o PDF)
    public String exportarCarnetSalud(int mascotaId, FormatoExportacion formato) {
        Mascota m = obtenerPorId(mascotaId)
                .orElseThrow(() -> new ValidacionException("No se encontro ninguna mascota con el ID: " + mascotaId));

        List<MascotaDAO.VacunaHistorialItem> vacunas = listarVacunasPorMascota(mascotaId);
        List<ProcedimientoEspecial> cirugias = listarProcedimientosPorMascota(mascotaId);

        ConexionBD config = ConexionBD.getInstancia();
        CarnetSaludExportData data = CarnetSaludExportData.builder()
                .conClinicaNombre(config.getNombreClinica())
                .conClinicaNit(config.getNitClinica())
                .conClinicaTel(config.getTelefonoClinica())
                .conClinicaDir(config.getDireccionClinica())
                .conMascotaId(m.getId())
                .conMascotaNombre(m.getNombre())
                .conEspecie(m.getNombreEspecie())
                .conRaza(m.getNombreRaza())
                .conSexo(m.getSexo() != null ? m.getSexo().getValorDb() : "N/D")
                .conFechaNacimiento(m.getFechaNacimiento() != null ? m.getFechaNacimiento().toString() : "N/D")
                .conEdadCalculada(m.getEdadCalculada())
                .conPesoActual(m.getPesoActual())
                .conMicrochip(m.getMicrochip())
                .conTatuaje(m.getTatuaje())
                .conFotoUrl(m.getFotoUrl())
                .conAlergias(m.getAlergias())
                .conCondicionesPreexistentes(m.getCondicionesPreexistentes())
                .conDuenoId(m.getDuenoId())
                .conDuenoNombre(m.getNombreDueno())
                .conVacunas(vacunas)
                .conCirugias(cirugias)
                .conFechaEmision(LocalDate.now())
                .build();

        return ExportadorFactory.getExportadorCarnet(formato).exportar(data);
    }

    // Validacion de reglas de negocio clinicas
    public void validarMascota(Mascota m) {
        if (m == null) {
            throw new ValidacionException("Los datos de la mascota no pueden ser nulos.");
        }

        // Validar que el dueno exista y este activo
        if (m.getDuenoId() <= 0) {
            throw new ValidacionException("La mascota debe estar asociada a un dueno registrado.");
        }
        Optional<Dueno> duenoOpt = duenoDAO.buscarPorId(m.getDuenoId());
        if (duenoOpt.isEmpty()) {
            throw new ValidacionException("No existe ningun cliente registrado con el ID: " + m.getDuenoId());
        }
        if (!duenoOpt.get().isActivo()) {
            throw new ValidacionException("El cliente asociado (ID " + m.getDuenoId() + ") se encuentra inactivo.");
        }

        // Nombre (VARCHAR 100, obligatorio)
        if (m.getNombre() == null || m.getNombre().trim().isEmpty()) {
            throw new ValidacionException("El nombre de la mascota es obligatorio.");
        }
        if (m.getNombre().trim().length() > 100) {
            throw new ValidacionException("El nombre no puede exceder 100 caracteres.");
        }

        // Especie y Raza obligatorias
        if (m.getEspecieId() <= 0) {
            throw new ValidacionException("Debe seleccionar una especie valida.");
        }
        if (m.getRazaId() <= 0) {
            throw new ValidacionException("Debe seleccionar una raza valida.");
        }

        // Fecha de nacimiento obligatoria y no futura
        if (m.getFechaNacimiento() == null) {
            throw new ValidacionException("La fecha de nacimiento es obligatoria.");
        }
        if (m.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new ValidacionException("La fecha de nacimiento no puede ser posterior a la fecha actual.");
        }

        // Sexo obligatorio
        if (m.getSexo() == null) {
            m.setSexo(SexoEnum.MACHO);
        }

        // Peso actual (0.01 a 999.99 kg segun CHECK constraint DDL)
        if (m.getPesoActual() <= 0.01 || m.getPesoActual() > 999.99) {
            throw new ValidacionException("El peso debe ser mayor a 0.01 kg y menor a 1000 kg.");
        }

        // Microchip y Tatuaje (VARCHAR 50)
        if (m.getMicrochip() != null && m.getMicrochip().trim().length() > 50) {
            throw new ValidacionException("El codigo de microchip no puede exceder 50 caracteres.");
        }
        if (m.getTatuaje() != null && m.getTatuaje().trim().length() > 50) {
            throw new ValidacionException("El codigo de tatuaje no puede exceder 50 caracteres.");
        }

        // Foto URL (VARCHAR 255)
        if (m.getFotoUrl() != null && m.getFotoUrl().trim().length() > 255) {
            throw new ValidacionException("La URL de la foto no puede exceder 255 caracteres.");
        }
    }
}
