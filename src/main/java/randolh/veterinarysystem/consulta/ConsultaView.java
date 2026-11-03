package randolh.veterinarysystem.consulta;

import randolh.veterinarysystem.core.exception.InventarioException;
import randolh.veterinarysystem.core.exception.ValidacionException;
import randolh.veterinarysystem.core.utils.ConsolaUtils;
import randolh.veterinarysystem.facturacion.FacturacionView;
import randolh.veterinarysystem.facturacion.ItemFactura;
import randolh.veterinarysystem.inventario.InventarioDAO;
import randolh.veterinarysystem.inventario.Producto;
import randolh.veterinarysystem.mascota.Mascota;
import randolh.veterinarysystem.mascota.MascotaDAO;
import randolh.veterinarysystem.veterinario.Veterinario;
import randolh.veterinarysystem.veterinario.VeterinarioDAO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Vista de consola interactiva para consultas medicas, procedimientos quirurgicos y deduccion
public class ConsultaView {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ConsultaController controller;
    private final MascotaDAO mascotaDAO;
    private final VeterinarioDAO veterinarioDAO;
    private final InventarioDAO inventarioDAO;
    private final FacturacionView facturacionView;

    public ConsultaView() {
        this.controller = new ConsultaController();
        this.mascotaDAO = new MascotaDAO();
        this.veterinarioDAO = new VeterinarioDAO();
        this.inventarioDAO = new InventarioDAO();
        this.facturacionView = new FacturacionView();
    }

    public ConsultaView(ConsultaController controller, MascotaDAO mascotaDAO,
                        VeterinarioDAO veterinarioDAO, InventarioDAO inventarioDAO) {
        this.controller = controller;
        this.mascotaDAO = mascotaDAO;
        this.veterinarioDAO = veterinarioDAO;
        this.inventarioDAO = inventarioDAO;
        this.facturacionView = new FacturacionView();
    }

    public ConsultaView(ConsultaController controller, MascotaDAO mascotaDAO,
                        VeterinarioDAO veterinarioDAO, InventarioDAO inventarioDAO,
                        FacturacionView facturacionView) {
        this.controller = controller;
        this.mascotaDAO = mascotaDAO;
        this.veterinarioDAO = veterinarioDAO;
        this.inventarioDAO = inventarioDAO;
        this.facturacionView = facturacionView;
    }

    // Menu principal del modulo clinico y quirurgico
    public void mostrarMenu() {
        Runnable menu = () -> {
            ConsolaUtils.imprimirTitulo("MODULO CLINICO: CONSULTAS, CIRUGIAS Y DEDUCCION DE INSUMOS");
            System.out.println(ConsolaUtils.COLOR_INFO + "1. Registrar nueva consulta medica (con deduccion de receta)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "2. Registrar procedimiento especial / cirugia (con deduccion de insumos)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "3. Consultar detalle de consulta medica por ID" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "4. Consultar detalle de procedimiento quirurgico por ID" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "5. Cambiar estado de procedimiento (Programado -> En Proceso -> Finalizado)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "6. Ver expediente e historial clinico completo de una mascota" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "0. Volver al menu anterior" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "----------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);
        };

        ConsolaUtils.setMenuActual(menu);

        boolean continuar = true;
        while (continuar) {
            menu.run();
            int opcion = ConsolaUtils.leerEnteroEnRango("Seleccione una opcion [0-6]: ", 0, 6);

            switch (opcion) {
                case 1 -> registrarConsulta();
                case 2 -> registrarProcedimiento();
                case 3 -> consultarConsultaPorId();
                case 4 -> consultarProcedimientoPorId();
                case 5 -> cambiarEstadoProcedimiento();
                case 6 -> verExpedienteClinicoMascota();
                case 0 -> {
                    ConsolaUtils.imprimirInfo("Regresando al menu principal...");
                    continuar = false;
                }
                default -> ConsolaUtils.imprimirError("Opcion no reconocida.");
            }

            if (continuar) {
                ConsolaUtils.pausar();
            }
        }
    }

    // Formulario interactivo de consulta medica con receta y deduccion
    private void registrarConsulta() {
        ConsolaUtils.imprimirSubtitulo("Registrar Nueva Consulta Medica");

        try {
            int mascotaId = ConsolaUtils.leerEntero("ID del paciente (Mascota): ");
            Optional<Mascota> mOpt = mascotaDAO.buscarPorId(mascotaId);
            if (mOpt.isEmpty()) {
                ConsolaUtils.imprimirError("No existe ninguna mascota con ID: " + mascotaId);
                return;
            }
            Mascota m = mOpt.get();
            if (!m.isActivo()) {
                ConsolaUtils.imprimirAdvertencia("El paciente se encuentra inactivo.");
                return;
            }
            ConsolaUtils.imprimirInfo("Paciente: " + m.getNombre() + " (" + m.getNombreEspecie() + ") - Dueno: " + m.getNombreDueno());

            int vetId = ConsolaUtils.leerEntero("ID del medico veterinario tratante: ");
            Optional<Veterinario> vOpt = veterinarioDAO.buscarPorId(vetId);
            if (vOpt.isEmpty() || !vOpt.get().isActivo()) {
                ConsolaUtils.imprimirError("Medico veterinario invalido o inactivo.");
                return;
            }
            ConsolaUtils.imprimirInfo("Medico tratante: Dr(a). " + vOpt.get().getNombreCompleto() + " (" + vOpt.get().getEspecialidad() + ")");

            int citaIdInput = ConsolaUtils.leerEntero("ID de cita agendada vinculada (0 si no aplica): ");
            Integer citaId = citaIdInput > 0 ? citaIdInput : null;

            String motivo = ConsolaUtils.leerTextoObligatorio("Motivo de consulta: ", 500);
            String sintomas = ConsolaUtils.leerTextoOpcional("Sintomas observados", "");
            String diagnostico = ConsolaUtils.leerTextoObligatorio("Diagnostico clinico: ", 1000);
            String recomendaciones = ConsolaUtils.leerTextoOpcional("Recomendaciones / Cuidados", "");
            String observaciones = ConsolaUtils.leerTextoOpcional("Observaciones internas", "");

            double peso = ConsolaUtils.leerDecimal("Peso actual registrado (kg): ");
            double temp = ConsolaUtils.leerDecimal("Temperatura corporal (°C): ");

            ConsultaMedica consulta = new ConsultaMedica(
                    mascotaId, vetId, citaId, motivo, sintomas, diagnostico,
                    recomendaciones, observaciones,
                    BigDecimal.valueOf(peso), BigDecimal.valueOf(temp)
            );

            // Seccion de prescripcion de farmacia
            List<Prescripcion> prescripciones = new ArrayList<>();
            System.out.println(ConsolaUtils.ANSI_GRAY + "\n--- Prescripcion de Medicamentos (Deduccion de Farmacia) ---" + ConsolaUtils.ANSI_RESET);
            boolean agregarReceta = ConsolaUtils.leerBooleano("Desea prescribir medicamentos para esta consulta?");

            while (agregarReceta) {
                int prodId = ConsolaUtils.leerEntero("ID del medicamento a recetar: ");
                Optional<Producto> prodOpt = inventarioDAO.buscarProductoPorId(prodId);
                if (prodOpt.isEmpty()) {
                    ConsolaUtils.imprimirError("Medicamento no encontrado en el catalogo.");
                } else {
                    Producto prod = prodOpt.get();
                    ConsolaUtils.imprimirInfo("Medicamento: " + prod.getNombreProducto() + " - Stock actual: " + prod.getCantidadStock() + " " + prod.getUnidadMedida());

                    int cant = ConsolaUtils.leerEntero("Cantidad a prescribir: ");
                    String dosis = ConsolaUtils.leerTextoObligatorio("Dosis (ej: 1 tableta, 5 ml): ", 100);
                    String frec = ConsolaUtils.leerTextoObligatorio("Frecuencia (ej: cada 8 horas): ", 100);
                    int dias = ConsolaUtils.leerEnteroEnRango("Duracion del tratamiento en dias: ", 1, 90);
                    String instr = ConsolaUtils.leerTextoOpcional("Instrucciones de administracion", "");

                    Prescripcion pres = new Prescripcion(prodId, cant, dosis, frec, dias, instr);
                    prescripciones.add(pres);
                    ConsolaUtils.imprimirExito("Medicamento agregado a la receta provisional.");
                }

                agregarReceta = ConsolaUtils.leerBooleano("Desea agregar otro medicamento a la receta?");
            }

            ConsultaMedica guardada = controller.registrarConsulta(consulta, prescripciones);
            ConsolaUtils.imprimirExito("Consulta clinica registrada exitosamente con ID: " + guardada.getId());
            if (!prescripciones.isEmpty()) {
                ConsolaUtils.imprimirInfo("Se dedujeron " + prescripciones.size() + " medicamentos del inventario correctamente.");
            }

            boolean facturarAhora = ConsolaUtils.leerBooleano("¿Desea generar la factura de esta atencion en este momento?");
            if (facturarAhora) {
                procederFacturacionConsulta(m, guardada, prescripciones);
            }

        } catch (ValidacionException | InventarioException e) {
            ConsolaUtils.imprimirAdvertencia(e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al registrar consulta medica: " + e.getMessage());
        }
    }

    // Formulario interactivo de cirugias y procedimientos con deduccion de insumos
    private void registrarProcedimiento() {
        ConsolaUtils.imprimirSubtitulo("Registrar Procedimiento Especial o Quirurgico");

        try {
            int mascotaId = ConsolaUtils.leerEntero("ID del paciente (Mascota): ");
            Optional<Mascota> mOpt = mascotaDAO.buscarPorId(mascotaId);
            if (mOpt.isEmpty()) {
                ConsolaUtils.imprimirError("No existe ninguna mascota con ID: " + mascotaId);
                return;
            }
            Mascota m = mOpt.get();
            ConsolaUtils.imprimirInfo("Paciente: " + m.getNombre() + " (" + m.getNombreEspecie() + ") - Dueno: " + m.getNombreDueno());

            int vetId = ConsolaUtils.leerEntero("ID del cirujano / profesional a cargo: ");
            Optional<Veterinario> vOpt = veterinarioDAO.buscarPorId(vetId);
            if (vOpt.isEmpty() || !vOpt.get().isActivo()) {
                ConsolaUtils.imprimirError("Cirujano veterinario invalido o inactivo.");
                return;
            }

            String tipo = ConsolaUtils.leerTextoObligatorio("Tipo de procedimiento (ej: Cirugia, Limpieza Dental, Radiografia): ", 100);
            String nombre = ConsolaUtils.leerTextoObligatorio("Nombre de la intervencion (ej: Esterilizacion Ovariohisterectomia): ", 255);
            int duracion = ConsolaUtils.leerEnteroEnRango("Duracion estimada en minutos: ", 5, 600);
            String preop = ConsolaUtils.leerTextoOpcional("Informacion preoperatoria / ayuno / anestesia", "");
            String detalle = ConsolaUtils.leerTextoObligatorio("Detalle / tecnica quirurgica aplicada: ", 2000);
            String complicaciones = ConsolaUtils.leerTextoOpcional("Complicaciones transoperatorias", "Sin complicaciones");
            String postop = ConsolaUtils.leerTextoOpcional("Seguimiento postoperatorio y cuidados", "");
            LocalDate control = ConsolaUtils.leerFechaOpcional("Fecha proximo control o retiro de puntos");
            double costo = ConsolaUtils.leerDecimal("Costo total de la intervencion: ");

            ProcedimientoEspecial proc = new ProcedimientoEspecial(
                    null, mascotaId, vetId, tipo, nombre, null, duracion,
                    preop, detalle, complicaciones, postop, control,
                    EstadoProcedimientoEnum.PROGRAMADO, BigDecimal.valueOf(costo)
            );

            // Insumos quirurgicos consumidos
            List<InsumoProcedimiento> insumos = new ArrayList<>();
            System.out.println(ConsolaUtils.ANSI_GRAY + "\n--- Material Medico e Insumos Quirurgicos Consumidos ---" + ConsolaUtils.ANSI_RESET);
            boolean agregarInsumo = ConsolaUtils.leerBooleano("Desea registrar insumos quirurgicos utilizados del almacen?");

            while (agregarInsumo) {
                int prodId = ConsolaUtils.leerEntero("ID del insumo / farmaco usado: ");
                Optional<Producto> prodOpt = inventarioDAO.buscarProductoPorId(prodId);
                if (prodOpt.isEmpty()) {
                    ConsolaUtils.imprimirError("Insumo no encontrado en almacen.");
                } else {
                    Producto p = prodOpt.get();
                    ConsolaUtils.imprimirInfo("Insumo: " + p.getNombreProducto() + " - Stock disponible: " + p.getCantidadStock() + " " + p.getUnidadMedida());

                    int cant = ConsolaUtils.leerEntero("Cantidad consumida: ");
                    String obs = ConsolaUtils.leerTextoOpcional("Observaciones de uso", "");

                    InsumoProcedimiento ins = new InsumoProcedimiento(prodId, cant, obs);
                    insumos.add(ins);
                    ConsolaUtils.imprimirExito("Insumo agregado al registro quirurgico.");
                }

                agregarInsumo = ConsolaUtils.leerBooleano("Desea agregar otro insumo quirurgico?");
            }

            ProcedimientoEspecial guardado = controller.registrarProcedimiento(proc, insumos);
            ConsolaUtils.imprimirExito("Procedimiento quirurgico registrado exitosamente con ID: " + guardado.getId());
            if (!insumos.isEmpty()) {
                ConsolaUtils.imprimirInfo("Se dedujeron " + insumos.size() + " insumos del inventario de forma atomica.");
            }

            boolean facturarAhora = ConsolaUtils.leerBooleano("¿Desea generar la factura de este procedimiento en este momento?");
            if (facturarAhora) {
                procederFacturacionProcedimiento(m, guardado, insumos);
            }

        } catch (ValidacionException | InventarioException e) {
            ConsolaUtils.imprimirAdvertencia(e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al registrar procedimiento: " + e.getMessage());
        }
    }

    // Consulta detalle de una consulta medica
    private void consultarConsultaPorId() {
        ConsolaUtils.imprimirSubtitulo("Consultar Detalle de Atencion Medica");
        int id = ConsolaUtils.leerEntero("Ingrese el ID de la consulta medica: ");

        try {
            Optional<ConsultaMedica> opt = controller.obtenerConsultaPorId(id);
            if (opt.isEmpty()) {
                ConsolaUtils.imprimirError("No se encontro ninguna consulta medica con ID: " + id);
                return;
            }

            ConsultaMedica c = opt.get();
            System.out.println();
            System.out.println(ConsolaUtils.COLOR_BORDE + "+----------------------------------------------------------------------------+" + ConsolaUtils.ANSI_RESET);
            System.out.printf(ConsolaUtils.COLOR_BORDE + "| " + ConsolaUtils.COLOR_TITULO + "FICHA CLINICA DE CONSULTA MEDICA #%-41d" + ConsolaUtils.COLOR_BORDE + " |\n" + ConsolaUtils.ANSI_RESET, c.getId());
            System.out.println(ConsolaUtils.COLOR_BORDE + "+----------------------------------------------------------------------------+" + ConsolaUtils.ANSI_RESET);
            System.out.printf("| Fecha y Hora:    %-58s |\n", c.getFechaHora() != null ? c.getFechaHora().format(FORMATO_FECHA_HORA) : "--");
            System.out.printf("| Paciente:        %-58s |\n", c.getNombreMascota() + " (ID: " + c.getMascotaId() + ")");
            System.out.printf("| Dueno:           %-58s |\n", c.getNombreDueno() != null ? c.getNombreDueno() : "--");
            System.out.printf("| Medico Tratante: %-58s |\n", c.getNombreVeterinario() != null ? c.getNombreVeterinario() : "--");
            System.out.printf("| Peso registrado: %-58s |\n", (c.getPesoRegistrado() != null ? c.getPesoRegistrado() + " kg" : "No medido"));
            System.out.printf("| Temperatura:     %-58s |\n", (c.getTemperatura() != null ? c.getTemperatura() + " °C" : "No medida"));
            System.out.printf("| Motivo Consulta: %-58s |\n", truncar(c.getMotivo(), 58));
            System.out.printf("| Diagnostico:     %-58s |\n", truncar(c.getDiagnostico(), 58));
            System.out.printf("| Recomendaciones: %-58s |\n", truncar(c.getRecomendaciones() != null ? c.getRecomendaciones() : "-", 58));

            List<Prescripcion> prescs = c.getPrescripciones();
            if (prescs != null && !prescs.isEmpty()) {
                System.out.println(ConsolaUtils.COLOR_BORDE + "+----------------------------------------------------------------------------+" + ConsolaUtils.ANSI_RESET);
                System.out.println(ConsolaUtils.COLOR_TITULO + "| RECETA Y TRATAMIENTO PRESCRITO:                                            |" + ConsolaUtils.ANSI_RESET);
                for (Prescripcion p : prescs) {
                    System.out.printf("|  * %s (%d %s) - %s, %s por %d dias%s|\n",
                            p.getNombreProducto(), p.getCantidad(), p.getUnidadMedida(),
                            p.getDosis(), p.getFrecuencia(), p.getDuracionDias(),
                            " ".repeat(Math.max(0, 10)));
                }
            }
            System.out.println(ConsolaUtils.COLOR_BORDE + "+----------------------------------------------------------------------------+" + ConsolaUtils.ANSI_RESET);

        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al consultar ficha medica: " + e.getMessage());
        }
    }

    // Consulta detalle de cirugia por ID
    private void consultarProcedimientoPorId() {
        ConsolaUtils.imprimirSubtitulo("Consultar Detalle de Procedimiento Quirurgico");
        int id = ConsolaUtils.leerEntero("Ingrese el ID del procedimiento: ");

        try {
            Optional<ProcedimientoEspecial> opt = controller.obtenerProcedimientoPorId(id);
            if (opt.isEmpty()) {
                ConsolaUtils.imprimirError("No se encontro ningun procedimiento con ID: " + id);
                return;
            }

            ProcedimientoEspecial p = opt.get();
            System.out.println();
            System.out.println(ConsolaUtils.COLOR_BORDE + "+----------------------------------------------------------------------------+" + ConsolaUtils.ANSI_RESET);
            System.out.printf(ConsolaUtils.COLOR_BORDE + "| " + ConsolaUtils.COLOR_TITULO + "REPORTE QUIRURGICO DE PROCEDIMIENTO #%-35d" + ConsolaUtils.COLOR_BORDE + " |\n" + ConsolaUtils.ANSI_RESET, p.getId());
            System.out.println(ConsolaUtils.COLOR_BORDE + "+----------------------------------------------------------------------------+" + ConsolaUtils.ANSI_RESET);
            System.out.printf("| Tipo:            %-58s |\n", p.getTipoProcedimiento());
            System.out.printf("| Intervencion:    %-58s |\n", truncar(p.getNombreProcedimiento(), 58));
            System.out.printf("| Estado:          %-58s |\n", p.getEstado().getDescripcion());
            System.out.printf("| Fecha y Hora:    %-58s |\n", p.getFechaHora() != null ? p.getFechaHora().format(FORMATO_FECHA_HORA) : "--");
            System.out.printf("| Paciente:        %-58s |\n", p.getNombreMascota());
            System.out.printf("| Cirujano:        %-58s |\n", p.getNombreVeterinario());
            System.out.printf("| Duracion aprox:  %-58s |\n", (p.getDuracionEstimadaMinutos() != null ? p.getDuracionEstimadaMinutos() + " minutos" : "--"));
            System.out.printf("| Detalle tecnico: %-58s |\n", truncar(p.getDetalleProcedimiento(), 58));
            System.out.printf("| Postoperatorio:  %-58s |\n", truncar(p.getSeguimientoPostoperatorio() != null ? p.getSeguimientoPostoperatorio() : "-", 58));
            System.out.printf("| Proximo control: %-58s |\n", (p.getProximoControl() != null ? p.getProximoControl().format(FORMATO_FECHA) : "Sin control fijado"));
            System.out.printf("| Costo total:     $%-57.2f |\n", p.getCostoProcedimiento());

            List<InsumoProcedimiento> insumos = p.getInsumos();
            if (insumos != null && !insumos.isEmpty()) {
                System.out.println(ConsolaUtils.COLOR_BORDE + "+----------------------------------------------------------------------------+" + ConsolaUtils.ANSI_RESET);
                System.out.println(ConsolaUtils.COLOR_TITULO + "| INSUMOS Y FARMACOS CONSUMIDOS EN QUIROFANO:                                |" + ConsolaUtils.ANSI_RESET);
                for (InsumoProcedimiento ins : insumos) {
                    System.out.printf("|  * %s - Cantidad usada: %d %s\n",
                            ins.getNombreProducto(), ins.getCantidadUsada(), ins.getUnidadMedida());
                }
            }
            System.out.println(ConsolaUtils.COLOR_BORDE + "+----------------------------------------------------------------------------+" + ConsolaUtils.ANSI_RESET);

        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al consultar procedimiento: " + e.getMessage());
        }
    }

    // Cambia el estado de un procedimiento
    private void cambiarEstadoProcedimiento() {
        ConsolaUtils.imprimirSubtitulo("Actualizar Estado de Procedimiento Quirurgico");
        int id = ConsolaUtils.leerEntero("ID del procedimiento: ");

        System.out.println("Seleccione nuevo estado:");
        System.out.println("1. Programado");
        System.out.println("2. En Proceso");
        System.out.println("3. Finalizado");
        System.out.println("4. Cancelado");
        int op = ConsolaUtils.leerEnteroEnRango("Opcion [1-4]: ", 1, 4);

        EstadoProcedimientoEnum nuevoEstado = switch (op) {
            case 1 -> EstadoProcedimientoEnum.PROGRAMADO;
            case 2 -> EstadoProcedimientoEnum.EN_PROCESO;
            case 3 -> EstadoProcedimientoEnum.FINALIZADO;
            case 4 -> EstadoProcedimientoEnum.CANCELADO;
            default -> EstadoProcedimientoEnum.PROGRAMADO;
        };

        try {
            boolean exito = controller.cambiarEstadoProcedimiento(id, nuevoEstado);
            if (exito) {
                ConsolaUtils.imprimirExito("Procedimiento ID " + id + " actualizado a estado: " + nuevoEstado.getDescripcion());
            } else {
                ConsolaUtils.imprimirAdvertencia("No fue posible actualizar el estado.");
            }
        } catch (ValidacionException e) {
            ConsolaUtils.imprimirAdvertencia(e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al cambiar estado: " + e.getMessage());
        }
    }

    // Visualizador de expediente clinico consolidado por mascota
    private void verExpedienteClinicoMascota() {
        ConsolaUtils.imprimirSubtitulo("Expediente Clinico Consolidado de Paciente");
        int mascotaId = ConsolaUtils.leerEntero("ID del paciente (Mascota): ");

        try {
            Optional<Mascota> mOpt = mascotaDAO.buscarPorId(mascotaId);
            if (mOpt.isEmpty()) {
                ConsolaUtils.imprimirError("No existe mascota con ID: " + mascotaId);
                return;
            }

            Mascota m = mOpt.get();
            ConsolaUtils.imprimirInfo("Expediente de: " + m.getNombre() + " (" + m.getNombreEspecie() + ") - Dueno: " + m.getNombreDueno());

            List<HistorialMedico> historial = controller.listarHistorialCompletoMascota(mascotaId);
            if (historial.isEmpty()) {
                ConsolaUtils.imprimirInfo("El paciente aun no registra eventos clinicos en su historial.");
                return;
            }

            ConsolaUtils.mostrarTablaPaginada(historial, 6, this::renderizarTablaHistorial);

        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al consultar expediente clinico: " + e.getMessage());
        }
    }

    // Renderiza la tabla de eventos de historial medico
    private void renderizarTablaHistorial(List<HistorialMedico> lista) {
        String formatoEncabezado = "| %-4s | %-10s | %-16s | %-20s | %-20s | %-18s |%n";
        String formatoFila = "| %-4d | %-10s | %-16s | %-20s | %-20s | %-18s |%n";
        String separador = "+------+------------+------------------+----------------------+----------------------+--------------------+";

        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);
        System.out.printf(ConsolaUtils.COLOR_TITULO + formatoEncabezado + ConsolaUtils.ANSI_RESET,
                "ID", "FECHA", "EVENTO", "DESCRIPCION", "DIAGNOSTICO", "VETERINARIO");
        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);

        for (HistorialMedico h : lista) {
            String fStr = h.getFechaEvento() != null ? h.getFechaEvento().format(FORMATO_FECHA) : "--";
            String tipo = truncar(h.getNombreEventoTipo() != null ? h.getNombreEventoTipo() : "Evento #" + h.getEventoTipoId(), 16);
            String desc = truncar(h.getDescripcion(), 20);
            String diag = truncar(h.getDiagnostico() != null ? h.getDiagnostico() : "-", 20);
            String vet = truncar(h.getNombreVeterinario() != null ? h.getNombreVeterinario() : "Sin asignar", 18);

            System.out.printf(formatoFila, h.getId(), fStr, tipo, desc, diag, vet);
        }
        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);
    }

    private String truncar(String texto, int max) {
        if (texto == null) return "-";
        return texto.length() <= max ? texto : texto.substring(0, max - 3) + "...";
    }

    // Inicia el flujo de facturacion directa para una consulta clinica
    private void procederFacturacionConsulta(Mascota m, ConsultaMedica c, List<Prescripcion> prescripciones) {
        List<ItemFactura> items = new ArrayList<>();
        // Servicio: Consulta Medica General (Servicio ID 1 en catalogo, precio base 35.00)
        items.add(ItemFactura.paraServicio(1, "Consulta Medica General - Paciente: " + m.getNombre(), 1, BigDecimal.valueOf(35.00)));

        // Medicamentos prescritos
        if (prescripciones != null) {
            for (Prescripcion p : prescripciones) {
                Optional<Producto> prodOpt = inventarioDAO.buscarProductoPorId(p.getProductoId());
                if (prodOpt.isPresent()) {
                    Producto prod = prodOpt.get();
                    items.add(ItemFactura.paraProducto(prod.getId(), prod.getNombreProducto(), p.getCantidad(), prod.getPrecioVenta()));
                }
            }
        }

        facturacionView.emitirFacturaConItemsPredefinidos(
                m.getDuenoId(),
                items,
                "Consulta medica #" + c.getId() + " - Paciente: " + m.getNombre()
        );
    }

    // Inicia el flujo de facturacion directa para un procedimiento quirurgico
    private void procederFacturacionProcedimiento(Mascota m, ProcedimientoEspecial p, List<InsumoProcedimiento> insumos) {
        List<ItemFactura> items = new ArrayList<>();
        // Concepto principal del procedimiento quirurgico con su costo
        String desc = p.getNombreProcedimiento() + " (" + p.getTipoProcedimiento() + ") - Paciente: " + m.getNombre();
        BigDecimal costo = p.getCostoProcedimiento() != null ? p.getCostoProcedimiento() : BigDecimal.ZERO;
        items.add(ItemFactura.paraServicio(null, desc, 1, costo));

        facturacionView.emitirFacturaConItemsPredefinidos(
                m.getDuenoId(),
                items,
                "Procedimiento especial #" + p.getId() + " (" + p.getNombreProcedimiento() + ") - Paciente: " + m.getNombre()
        );
    }
}
