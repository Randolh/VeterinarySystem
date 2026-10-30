package randolh.veterinarysystem.consulta;

import randolh.veterinarysystem.core.exception.ValidacionException;
import randolh.veterinarysystem.core.utils.LoggerManager;
import randolh.veterinarysystem.inventario.InventarioController;
import randolh.veterinarysystem.mascota.Mascota;
import randolh.veterinarysystem.mascota.MascotaDAO;
import randolh.veterinarysystem.veterinario.Veterinario;
import randolh.veterinarysystem.veterinario.VeterinarioDAO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// Controlador de logica de negocio para atenciones clinicas, cirugias e historial medico
public class ConsultaController {

    private static final String MODULO = "ConsultaController";

    private final ConsultaDAO consultaDAO;
    private final MascotaDAO mascotaDAO;
    private final VeterinarioDAO veterinarioDAO;
    private final InventarioController inventarioController;

    public ConsultaController() {
        this.consultaDAO = new ConsultaDAO();
        this.mascotaDAO = new MascotaDAO();
        this.veterinarioDAO = new VeterinarioDAO();
        this.inventarioController = new InventarioController();
    }

    public ConsultaController(ConsultaDAO consultaDAO, MascotaDAO mascotaDAO,
                              VeterinarioDAO veterinarioDAO, InventarioController inventarioController) {
        this.consultaDAO = consultaDAO;
        this.mascotaDAO = mascotaDAO;
        this.veterinarioDAO = veterinarioDAO;
        this.inventarioController = inventarioController;
    }

    // Registra consulta medica validando paciente, profesional, diagnostico e inventario previo
    public ConsultaMedica registrarConsulta(ConsultaMedica c, List<Prescripcion> prescripciones) {
        validarConsulta(c);

        // Validacion de paciente activo
        Optional<Mascota> mOpt = mascotaDAO.buscarPorId(c.getMascotaId());
        if (mOpt.isEmpty()) {
            throw new ValidacionException("La mascota especificada (ID: " + c.getMascotaId() + ") no existe.");
        }
        if (!mOpt.get().isActivo()) {
            throw new ValidacionException("No es posible registrar atencion clinica para una mascota inactiva.");
        }

        // Validacion de profesional veterinario activo
        Optional<Veterinario> vOpt = veterinarioDAO.buscarPorId(c.getVeterinarioId());
        if (vOpt.isEmpty()) {
            throw new ValidacionException("El medico veterinario asignado (ID: " + c.getVeterinarioId() + ") no existe.");
        }
        if (!vOpt.get().isActivo()) {
            throw new ValidacionException("El medico veterinario asignado se encuentra inactivo.");
        }

        // Pre-validacion de stock y caducidad de medicamentos antes de abrir sesion transaccional
        if (prescripciones != null && !prescripciones.isEmpty()) {
            for (Prescripcion p : prescripciones) {
                if (p.getCantidad() <= 0) {
                    throw new ValidacionException("La cantidad prescrita para cada medicamento debe ser mayor a 0.");
                }
                inventarioController.validarDisponibilidadParaUso(p.getProductoId(), p.getCantidad());
            }
        }

        LoggerManager.info(MODULO, "Procesando consulta clinica para mascota ID: " + c.getMascotaId());
        return consultaDAO.registrarConsultaConDeduccion(c, prescripciones);
    }

    // Registra cirugia o procedimiento especial validando insumos quirurgicos previos
    public ProcedimientoEspecial registrarProcedimiento(ProcedimientoEspecial proc, List<InsumoProcedimiento> insumos) {
        validarProcedimiento(proc);

        Optional<Mascota> mOpt = mascotaDAO.buscarPorId(proc.getMascotaId());
        if (mOpt.isEmpty()) {
            throw new ValidacionException("La mascota especificada (ID: " + proc.getMascotaId() + ") no existe.");
        }
        if (!mOpt.get().isActivo()) {
            throw new ValidacionException("No se pueden programar cirugias para mascotas inactivas.");
        }

        Optional<Veterinario> vOpt = veterinarioDAO.buscarPorId(proc.getVeterinarioId());
        if (vOpt.isEmpty() || !vOpt.get().isActivo()) {
            throw new ValidacionException("El cirujano veterinario asignado no existe o no se encuentra activo.");
        }

        // Pre-validacion de insumos quirurgicos
        if (insumos != null && !insumos.isEmpty()) {
            for (InsumoProcedimiento ins : insumos) {
                if (ins.getCantidadUsada() <= 0) {
                    throw new ValidacionException("La cantidad usada para cada insumo debe ser mayor a 0.");
                }
                inventarioController.validarDisponibilidadParaUso(ins.getProductoId(), ins.getCantidadUsada());
            }
        }

        LoggerManager.info(MODULO, "Procesando acto quirurgico: " + proc.getNombreProcedimiento() + " para mascota ID: " + proc.getMascotaId());
        return consultaDAO.registrarProcedimientoConInsumos(proc, insumos);
    }

    // Actualiza estado de un procedimiento
    public boolean cambiarEstadoProcedimiento(int procId, EstadoProcedimientoEnum nuevoEstado) {
        if (procId <= 0) {
            throw new ValidacionException("El ID del procedimiento debe ser mayor a 0.");
        }
        if (nuevoEstado == null) {
            throw new ValidacionException("El nuevo estado del procedimiento es obligatorio.");
        }

        Optional<ProcedimientoEspecial> opt = consultaDAO.buscarProcedimientoPorId(procId);
        if (opt.isEmpty()) {
            throw new ValidacionException("No existe ningun procedimiento con ID: " + procId);
        }

        LoggerManager.info(MODULO, "Cambiando estado de procedimiento ID " + procId + " a " + nuevoEstado.getDescripcion());
        return consultaDAO.actualizarEstadoProcedimiento(procId, nuevoEstado);
    }

    // Busca consulta por ID
    public Optional<ConsultaMedica> obtenerConsultaPorId(int id) {
        if (id <= 0) {
            throw new ValidacionException("El ID de la consulta debe ser mayor a 0.");
        }
        return consultaDAO.buscarConsultaPorId(id);
    }

    // Lista consultas de una mascota
    public List<ConsultaMedica> listarConsultasPorMascota(int mascotaId) {
        if (mascotaId <= 0) {
            throw new ValidacionException("El ID de la mascota debe ser mayor a 0.");
        }
        return consultaDAO.listarConsultasPorMascota(mascotaId);
    }

    // Busca procedimiento por ID
    public Optional<ProcedimientoEspecial> obtenerProcedimientoPorId(int id) {
        if (id <= 0) {
            throw new ValidacionException("El ID del procedimiento debe ser mayor a 0.");
        }
        return consultaDAO.buscarProcedimientoPorId(id);
    }

    // Lista procedimientos de una mascota
    public List<ProcedimientoEspecial> listarProcedimientosPorMascota(int mascotaId) {
        if (mascotaId <= 0) {
            throw new ValidacionException("El ID de la mascota debe ser mayor a 0.");
        }
        return consultaDAO.listarProcedimientosPorMascota(mascotaId);
    }

    // Lista historial consolidado del expediente clinico
    public List<HistorialMedico> listarHistorialCompletoMascota(int mascotaId) {
        if (mascotaId <= 0) {
            throw new ValidacionException("El ID de la mascota debe ser mayor a 0.");
        }
        return consultaDAO.listarHistorialCompletoMascota(mascotaId);
    }

    // Validaciones internas de consulta medica
    private void validarConsulta(ConsultaMedica c) {
        if (c == null) {
            throw new ValidacionException("Los datos de la atencion medica son obligatorios.");
        }
        if (c.getMascotaId() <= 0) {
            throw new ValidacionException("El ID del paciente es obligatorio.");
        }
        if (c.getVeterinarioId() <= 0) {
            throw new ValidacionException("El ID del profesional veterinario es obligatorio.");
        }
        if (c.getMotivo() == null || c.getMotivo().trim().isEmpty()) {
            throw new ValidacionException("El motivo de la atencion es obligatorio.");
        }
        if (c.getMotivo().trim().length() > 500) {
            throw new ValidacionException("El motivo no puede exceder 500 caracteres.");
        }
        if (c.getDiagnostico() == null || c.getDiagnostico().trim().isEmpty()) {
            throw new ValidacionException("El diagnostico clinico es obligatorio para cerrar la atencion medica.");
        }
        if (c.getPesoRegistrado() != null && c.getPesoRegistrado().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacionException("El peso registrado del paciente debe ser un numero positivo mayor a 0.");
        }
        if (c.getTemperatura() != null) {
            double temp = c.getTemperatura().doubleValue();
            if (temp < 25.0 || temp > 45.0) {
                throw new ValidacionException("La temperatura corporal registrada (" + temp + " °C) esta fuera de rango fisiologico creible.");
            }
        }
    }

    // Validaciones internas de procedimiento quirurgico
    private void validarProcedimiento(ProcedimientoEspecial proc) {
        if (proc == null) {
            throw new ValidacionException("Los datos del procedimiento quirurgico son obligatorios.");
        }
        if (proc.getMascotaId() <= 0) {
            throw new ValidacionException("El ID del paciente es obligatorio.");
        }
        if (proc.getVeterinarioId() <= 0) {
            throw new ValidacionException("El ID del cirujano es obligatorio.");
        }
        if (proc.getTipoProcedimiento() == null || proc.getTipoProcedimiento().trim().isEmpty()) {
            throw new ValidacionException("El tipo de procedimiento es obligatorio.");
        }
        if (proc.getNombreProcedimiento() == null || proc.getNombreProcedimiento().trim().isEmpty()) {
            throw new ValidacionException("El nombre del procedimiento es obligatorio.");
        }
        if (proc.getDetalleProcedimiento() == null || proc.getDetalleProcedimiento().trim().isEmpty()) {
            throw new ValidacionException("El reporte o detalle quirurgico es obligatorio.");
        }
        if (proc.getCostoProcedimiento() != null && proc.getCostoProcedimiento().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionException("El costo del procedimiento no puede ser negativo.");
        }
    }
}
