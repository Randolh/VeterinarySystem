package randolh.veterinarysystem.cita;

import randolh.veterinarysystem.core.exception.ValidacionException;
import randolh.veterinarysystem.core.utils.LoggerManager;
import randolh.veterinarysystem.mascota.Mascota;
import randolh.veterinarysystem.mascota.MascotaDAO;
import randolh.veterinarysystem.veterinario.Veterinario;
import randolh.veterinarysystem.veterinario.VeterinarioDAO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Controlador de logica de negocio para agendamiento y ciclo de vida de citas
public class CitaController {

    private static final String MODULO = "CitaController";

    private final CitaDAO citaDAO;
    private final MascotaDAO mascotaDAO;
    private final VeterinarioDAO veterinarioDAO;

    public CitaController() {
        this.citaDAO = new CitaDAO();
        this.mascotaDAO = new MascotaDAO();
        this.veterinarioDAO = new VeterinarioDAO();
    }

    public CitaController(CitaDAO citaDAO, MascotaDAO mascotaDAO, VeterinarioDAO veterinarioDAO) {
        this.citaDAO = citaDAO;
        this.mascotaDAO = mascotaDAO;
        this.veterinarioDAO = veterinarioDAO;
    }

    // Agenda una nueva cita medica validando paciente, veterinario, horario y solapamiento
    public Cita agendarCita(Cita c) {
        validarCitaNueva(c);

        // Validacion de paciente activo
        Optional<Mascota> mascotaOpt = mascotaDAO.buscarPorId(c.getMascotaId());
        if (mascotaOpt.isEmpty()) {
            throw new ValidacionException("La mascota especificada (ID: " + c.getMascotaId() + ") no existe en el sistema.");
        }
        if (!mascotaOpt.get().isActivo()) {
            throw new ValidacionException("No se puede agendar cita para una mascota inactiva.");
        }

        // Validacion de veterinario activo y solapamiento de agenda (+/- 30 min)
        if (c.getVeterinarioId() != null) {
            Optional<Veterinario> vetOpt = veterinarioDAO.buscarPorId(c.getVeterinarioId());
            if (vetOpt.isEmpty()) {
                throw new ValidacionException("El veterinario asignado (ID: " + c.getVeterinarioId() + ") no existe.");
            }
            if (!vetOpt.get().isActivo()) {
                throw new ValidacionException("El veterinario asignado se encuentra inactivo.");
            }

            // Deteccion de conflicto de horario
            Optional<Cita> solapada = citaDAO.detectarSolapamiento(c.getVeterinarioId(), c.getFechaHora(), null);
            if (solapada.isPresent()) {
                Cita s = solapada.get();
                throw new ValidacionException(String.format(
                        "Conflicto de horario: El veterinario ya tiene una cita (%s) programada para '%s' a las %s.",
                        s.getEstado().getDescripcion(), s.getNombreMascota(), s.getFechaHora().toString().replace('T', ' ')
                ));
            }
        }

        LoggerManager.info(MODULO, "Agendando cita para mascota ID: " + c.getMascotaId() + " el " + c.getFechaHora());
        return citaDAO.insertar(c);
    }

    // Confirma una cita programada validando transicion State y agenda
    public boolean confirmarCita(int citaId) {
        Cita c = obtenerCitaOError(citaId);

        // Si tiene veterinario asignado, revalidar solapamiento antes de confirmar
        if (c.getVeterinarioId() != null) {
            Optional<Cita> solapada = citaDAO.detectarSolapamiento(c.getVeterinarioId(), c.getFechaHora(), c.getId());
            if (solapada.isPresent()) {
                throw new ValidacionException("No se puede confirmar: Existe un conflicto de agenda con la cita ID " + solapada.get().getId());
            }
        }

        c.confirmar();
        LoggerManager.info(MODULO, "Cita ID " + citaId + " confirmada.");
        return citaDAO.actualizar(c);
    }

    // Inicia la atencion medica de una cita en curso
    public boolean iniciarAtencion(int citaId) {
        Cita c = obtenerCitaOError(citaId);
        c.iniciarAtencion();
        LoggerManager.info(MODULO, "Cita ID " + citaId + " iniciada (En Curso).");
        return citaDAO.actualizar(c);
    }

    // Finaliza la atencion medica de una cita
    public boolean finalizarCita(int citaId) {
        Cita c = obtenerCitaOError(citaId);
        c.finalizar();
        LoggerManager.info(MODULO, "Cita ID " + citaId + " finalizada con exito.");
        return citaDAO.actualizar(c);
    }

    // Cancela una cita requiriendo motivo explicito
    public boolean cancelarCita(int citaId, String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new ValidacionException("Debe proporcionar un motivo explicito para cancelar la cita.");
        }
        Cita c = obtenerCitaOError(citaId);
        c.cancelar(motivo.trim());
        LoggerManager.info(MODULO, "Cita ID " + citaId + " cancelada. Motivo: " + motivo.trim());
        return citaDAO.actualizar(c);
    }

    // Marca una cita como No Asistio
    public boolean marcarNoAsistio(int citaId) {
        Cita c = obtenerCitaOError(citaId);
        c.marcarNoAsistio();
        LoggerManager.info(MODULO, "Cita ID " + citaId + " marcada como No Asistio.");
        return citaDAO.actualizar(c);
    }

    // Reprograma fecha, hora o veterinario de una cita
    public boolean reprogramarCita(int citaId, LocalDateTime nuevaFechaHora, Integer nuevoVeterinarioId) {
        Cita c = obtenerCitaOError(citaId);

        if (c.getEstado() != EstadoCitaEnum.PROGRAMADA && c.getEstado() != EstadoCitaEnum.CONFIRMADA) {
            throw new ValidacionException("Solo se pueden reprogramar citas en estado 'Programada' o 'Confirmada'.");
        }

        if (nuevaFechaHora == null) {
            throw new ValidacionException("La nueva fecha y hora son obligatorias.");
        }
        if (nuevaFechaHora.isBefore(LocalDateTime.now())) {
            throw new ValidacionException("La nueva fecha y hora no pueden ser en el pasado.");
        }

        Integer vetIdFinal = nuevoVeterinarioId != null ? nuevoVeterinarioId : c.getVeterinarioId();
        if (vetIdFinal != null) {
            Optional<Veterinario> vetOpt = veterinarioDAO.buscarPorId(vetIdFinal);
            if (vetOpt.isEmpty() || !vetOpt.get().isActivo()) {
                throw new ValidacionException("El veterinario asignado es invalido o se encuentra inactivo.");
            }

            Optional<Cita> solapada = citaDAO.detectarSolapamiento(vetIdFinal, nuevaFechaHora, c.getId());
            if (solapada.isPresent()) {
                throw new ValidacionException("Conflicto de horario: El veterinario ya tiene una cita a esa hora.");
            }
        }

        c.setFechaHora(nuevaFechaHora);
        c.setVeterinarioId(vetIdFinal);
        LoggerManager.info(MODULO, "Reprogramando cita ID " + citaId + " a " + nuevaFechaHora);
        return citaDAO.actualizar(c);
    }

    // Busca cita por ID
    public Optional<Cita> obtenerPorId(int id) {
        if (id <= 0) {
            throw new ValidacionException("El ID de la cita debe ser mayor a 0.");
        }
        return citaDAO.buscarPorId(id);
    }

    // Lista todas las citas
    public List<Cita> listarTodas() {
        return citaDAO.listarTodas();
    }

    // Lista agenda de citas por fecha
    public List<Cita> listarPorFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new ValidacionException("La fecha para consultar la agenda es obligatoria.");
        }
        return citaDAO.listarPorFecha(fecha);
    }

    // Lista citas asignadas a un veterinario en una fecha
    public List<Cita> listarPorVeterinario(int vetId, LocalDate fecha) {
        if (vetId <= 0) {
            throw new ValidacionException("El ID del veterinario debe ser mayor a 0.");
        }
        return citaDAO.listarPorVeterinario(vetId, fecha);
    }

    // Lista citas pertenecientes a una mascota
    public List<Cita> listarPorMascota(int mascotaId) {
        if (mascotaId <= 0) {
            throw new ValidacionException("El ID de la mascota debe ser mayor a 0.");
        }
        return citaDAO.listarPorMascota(mascotaId);
    }

    // Obtiene una cita existente o lanza excepcion de validacion
    private Cita obtenerCitaOError(int citaId) {
        if (citaId <= 0) {
            throw new ValidacionException("El ID de la cita debe ser mayor a 0.");
        }
        return citaDAO.buscarPorId(citaId)
                .orElseThrow(() -> new ValidacionException("No existe ninguna cita registrada con ID: " + citaId));
    }

    // Valida atributos basicos de una cita antes de insercion
    private void validarCitaNueva(Cita c) {
        if (c == null) {
            throw new ValidacionException("Los datos de la cita son obligatorios.");
        }
        if (c.getMascotaId() <= 0) {
            throw new ValidacionException("El ID del paciente (mascota) es obligatorio.");
        }
        if (c.getFechaHora() == null) {
            throw new ValidacionException("La fecha y hora de la cita son obligatorias.");
        }
        if (c.getFechaHora().isBefore(LocalDateTime.now())) {
            throw new ValidacionException("No es posible agendar una cita en una fecha u hora pasada.");
        }
        if (c.getMotivo() == null || c.getMotivo().trim().isEmpty()) {
            throw new ValidacionException("El motivo de la cita medica es obligatorio.");
        }
        if (c.getMotivo().trim().length() > 500) {
            throw new ValidacionException("El motivo no puede exceder los 500 caracteres.");
        }
    }
}
