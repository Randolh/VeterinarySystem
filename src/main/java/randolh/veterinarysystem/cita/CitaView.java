package randolh.veterinarysystem.cita;

import randolh.veterinarysystem.core.exception.ValidacionException;
import randolh.veterinarysystem.core.utils.ConsolaUtils;
import randolh.veterinarysystem.mascota.Mascota;
import randolh.veterinarysystem.mascota.MascotaDAO;
import randolh.veterinarysystem.veterinario.Veterinario;
import randolh.veterinarysystem.veterinario.VeterinarioDAO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

// Vista de consola interactiva para la gestion de citas clinicas y agenda medica
public class CitaView {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CitaController controller;
    private final MascotaDAO mascotaDAO;
    private final VeterinarioDAO veterinarioDAO;

    public CitaView() {
        this.controller = new CitaController();
        this.mascotaDAO = new MascotaDAO();
        this.veterinarioDAO = new VeterinarioDAO();
    }

    public CitaView(CitaController controller, MascotaDAO mascotaDAO, VeterinarioDAO veterinarioDAO) {
        this.controller = controller;
        this.mascotaDAO = mascotaDAO;
        this.veterinarioDAO = veterinarioDAO;
    }

    // Menu principal del modulo de citas y agenda
    public void mostrarMenu() {
        Runnable menu = () -> {
            ConsolaUtils.imprimirTitulo("MODULO DE CITAS MEDICAS Y AGENDA CLINICA");
            System.out.println(ConsolaUtils.COLOR_INFO + "1. Agendar nueva cita medica" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "2. Ver agenda medica por fecha" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "3. Ver agenda del dia de hoy" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "4. Listar todas las citas (tabla paginada)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "5. Consultar detalle de cita por ID" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "6. Confirmar cita programada" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "7. Iniciar atencion medica (pasar a En Curso)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "8. Finalizar atencion medica (pasar a Finalizada)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "9. Cancelar cita medica (con motivo)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "10. Reprogramar fecha u horario de cita" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "0. Volver al menu anterior" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "----------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);
        };

        ConsolaUtils.setMenuActual(menu);

        boolean continuar = true;
        while (continuar) {
            menu.run();
            int opcion = ConsolaUtils.leerEnteroEnRango("Seleccione una opcion [0-10]: ", 0, 10);

            switch (opcion) {
                case 1 -> agendarCita();
                case 2 -> verAgendaPorFecha();
                case 3 -> verAgendaHoy();
                case 4 -> listarTodasPaginado();
                case 5 -> consultarPorId();
                case 6 -> confirmarCita();
                case 7 -> iniciarAtencion();
                case 8 -> finalizarCita();
                case 9 -> cancelarCita();
                case 10 -> reprogramarCita();
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

    // Formulario de agendamiento guiado
    private void agendarCita() {
        ConsolaUtils.imprimirSubtitulo("Agendar Nueva Cita Medica");

        try {
            int mascotaId = ConsolaUtils.leerEntero("ID del paciente (Mascota): ");
            Optional<Mascota> mascotaOpt = mascotaDAO.buscarPorId(mascotaId);
            if (mascotaOpt.isEmpty()) {
                ConsolaUtils.imprimirError("No existe ninguna mascota con ID: " + mascotaId);
                return;
            }
            Mascota m = mascotaOpt.get();
            if (!m.isActivo()) {
                ConsolaUtils.imprimirAdvertencia("La mascota se encuentra inactiva.");
                return;
            }
            ConsolaUtils.imprimirInfo("Paciente seleccionado: " + m.getNombre() + " (" + m.getNombreEspecie() + ") - Dueno: " + m.getNombreDueno());

            System.out.println(ConsolaUtils.ANSI_GRAY + "Veterinarios disponibles:" + ConsolaUtils.ANSI_RESET);
            List<Veterinario> vets = veterinarioDAO.listarTodos().stream().filter(Veterinario::isActivo).toList();
            for (Veterinario v : vets) {
                System.out.printf("  [%d] Dr(a). %s (%s)\n", v.getId(), v.getNombreCompleto(), v.getEspecialidad());
            }

            int vetIdInput = ConsolaUtils.leerEntero("ID del veterinario asignado (0 si aun no asigna): ");
            Integer vetId = vetIdInput > 0 ? vetIdInput : null;

            LocalDate fecha = ConsolaUtils.leerFecha("Fecha de la cita");
            LocalTime hora = ConsolaUtils.leerHora("Hora de la cita");
            LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);

            if (fechaHora.isBefore(LocalDateTime.now())) {
                ConsolaUtils.imprimirError("La fecha y hora no pueden ser anteriores al momento actual.");
                return;
            }

            String motivo = ConsolaUtils.leerTextoObligatorio("Motivo de la consulta: ", 500);
            String notas = ConsolaUtils.leerTextoOpcional("Notas preparatorias o sintomas previos", "");

            Cita nuevaCita = new Cita(mascotaId, vetId, fechaHora, motivo, notas);
            Cita guardada = controller.agendarCita(nuevaCita);

            ConsolaUtils.imprimirExito("Cita medica agendada exitosamente con ID: " + guardada.getId());
            ConsolaUtils.imprimirInfo("Estado inicial: " + formatearBadgeEstado(guardada.getEstado()));

        } catch (ValidacionException e) {
            ConsolaUtils.imprimirAdvertencia("No fue posible agendar la cita: " + e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error inesperado al agendar cita: " + e.getMessage());
        }
    }

    // Consulta agenda para una fecha seleccionada
    private void verAgendaPorFecha() {
        ConsolaUtils.imprimirSubtitulo("Agenda Medica por Fecha");
        LocalDate fecha = ConsolaUtils.leerFecha("Ingrese la fecha a consultar");
        mostrarAgendaDeFecha(fecha);
    }

    // Consulta agenda para el dia de hoy
    private void verAgendaHoy() {
        ConsolaUtils.imprimirSubtitulo("Agenda Medica de Hoy (" + LocalDate.now() + ")");
        mostrarAgendaDeFecha(LocalDate.now());
    }

    // Renderiza la agenda de una fecha dada
    private void mostrarAgendaDeFecha(LocalDate fecha) {
        try {
            List<Cita> citas = controller.listarPorFecha(fecha);
            if (citas.isEmpty()) {
                ConsolaUtils.imprimirInfo("No hay citas registradas para el dia " + fecha);
                return;
            }

            ConsolaUtils.imprimirInfo("Citas encontradas para " + fecha + ": " + citas.size());
            ConsolaUtils.mostrarTablaPaginada(citas, 8, this::renderizarTablaCitas);

        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al consultar agenda: " + e.getMessage());
        }
    }

    // Lista todas las citas con paginacion
    private void listarTodasPaginado() {
        ConsolaUtils.imprimirSubtitulo("Listado Historico General de Citas");

        try {
            List<Cita> lista = controller.listarTodas();
            if (lista.isEmpty()) {
                ConsolaUtils.imprimirInfo("No hay citas registradas en el sistema.");
                return;
            }

            ConsolaUtils.mostrarTablaPaginada(lista, 8, this::renderizarTablaCitas);

        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al listar citas: " + e.getMessage());
        }
    }

    // Renderiza la tabla de citas
    private void renderizarTablaCitas(List<Cita> lista) {
        String formatoEncabezado = "| %-4s | %-16s | %-15s | %-18s | %-18s | %-12s | %-20s |%n";
        String formatoFila = "| %-4d | %-16s | %-15s | %-18s | %-18s | %-12s | %-20s |%n";
        String separador = "+------+------------------+-----------------+--------------------+--------------------+--------------+----------------------+";

        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);
        System.out.printf(ConsolaUtils.COLOR_TITULO + formatoEncabezado + ConsolaUtils.ANSI_RESET,
                "ID", "FECHA/HORA", "PACIENTE", "DUENO", "VETERINARIO", "ESTADO", "MOTIVO");
        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);

        for (Cita c : lista) {
            String fStr = c.getFechaHora() != null ? c.getFechaHora().format(FORMATO_FECHA_HORA) : "--";
            String mascota = truncar(c.getNombreMascota() != null ? c.getNombreMascota() : String.valueOf(c.getMascotaId()), 15);
            String dueno = truncar(c.getNombreDueno() != null ? c.getNombreDueno() : "-", 18);
            String vet = truncar(c.getNombreVeterinario() != null ? c.getNombreVeterinario() : "Sin asignar", 18);
            String estado = truncar(c.getEstado().getDescripcion(), 12);
            String motivo = truncar(c.getMotivo(), 20);

            System.out.printf(formatoFila, c.getId(), fStr, mascota, dueno, vet, estado, motivo);
        }
        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);
    }

    // Trunca texto a longitud maxima
    private String truncar(String texto, int max) {
        if (texto == null) return "-";
        return texto.length() <= max ? texto : texto.substring(0, max - 3) + "...";
    }

    // Consulta detalle de una cita
    private void consultarPorId() {
        ConsolaUtils.imprimirSubtitulo("Consultar Detalle de Cita");

        int id = ConsolaUtils.leerEntero("Ingrese el ID de la cita: ");
        try {
            Optional<Cita> opt = controller.obtenerPorId(id);
            if (opt.isEmpty()) {
                ConsolaUtils.imprimirError("No se encontro ninguna cita con ID: " + id);
                return;
            }

            mostrarFichaDetalle(opt.get());

        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al consultar cita: " + e.getMessage());
        }
    }

    // Confirma una cita
    private void confirmarCita() {
        ConsolaUtils.imprimirSubtitulo("Confirmar Cita Medica");

        int id = ConsolaUtils.leerEntero("ID de la cita a confirmar: ");
        try {
            boolean exito = controller.confirmarCita(id);
            if (exito) {
                ConsolaUtils.imprimirExito("Cita ID " + id + " confirmada exitosamente.");
            } else {
                ConsolaUtils.imprimirAdvertencia("No fue posible confirmar la cita.");
            }
        } catch (ValidacionException | IllegalStateException e) {
            ConsolaUtils.imprimirAdvertencia(e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al confirmar cita: " + e.getMessage());
        }
    }

    // Inicia atencion medica
    private void iniciarAtencion() {
        ConsolaUtils.imprimirSubtitulo("Iniciar Atencion Medica (En Curso)");

        int id = ConsolaUtils.leerEntero("ID de la cita a iniciar: ");
        try {
            boolean exito = controller.iniciarAtencion(id);
            if (exito) {
                ConsolaUtils.imprimirExito("Atencion medica iniciada para la cita ID: " + id);
            } else {
                ConsolaUtils.imprimirAdvertencia("No fue posible iniciar la atencion de la cita.");
            }
        } catch (ValidacionException | IllegalStateException e) {
            ConsolaUtils.imprimirAdvertencia(e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al iniciar atencion: " + e.getMessage());
        }
    }

    // Finaliza cita
    private void finalizarCita() {
        ConsolaUtils.imprimirSubtitulo("Finalizar Cita Medica");

        int id = ConsolaUtils.leerEntero("ID de la cita a finalizar: ");
        try {
            boolean exito = controller.finalizarCita(id);
            if (exito) {
                ConsolaUtils.imprimirExito("Cita ID " + id + " finalizada exitosamente.");
            } else {
                ConsolaUtils.imprimirAdvertencia("No fue posible finalizar la cita.");
            }
        } catch (ValidacionException | IllegalStateException e) {
            ConsolaUtils.imprimirAdvertencia(e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al finalizar cita: " + e.getMessage());
        }
    }

    // Cancela cita
    private void cancelarCita() {
        ConsolaUtils.imprimirSubtitulo("Cancelar Cita Medica");

        int id = ConsolaUtils.leerEntero("ID de la cita a cancelar: ");
        String motivo = ConsolaUtils.leerTextoObligatorio("Motivo de la cancelacion: ", 500);

        try {
            boolean exito = controller.cancelarCita(id, motivo);
            if (exito) {
                ConsolaUtils.imprimirExito("Cita ID " + id + " cancelada correctamente.");
            } else {
                ConsolaUtils.imprimirAdvertencia("No se pudo cancelar la cita.");
            }
        } catch (ValidacionException | IllegalStateException e) {
            ConsolaUtils.imprimirAdvertencia(e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al cancelar cita: " + e.getMessage());
        }
    }

    // Reprograma fecha y horario
    private void reprogramarCita() {
        ConsolaUtils.imprimirSubtitulo("Reprogramar Fecha u Horario de Cita");

        int id = ConsolaUtils.leerEntero("ID de la cita a reprogramar: ");
        try {
            Optional<Cita> opt = controller.obtenerPorId(id);
            if (opt.isEmpty()) {
                ConsolaUtils.imprimirError("No existe cita con ID: " + id);
                return;
            }

            Cita actual = opt.get();
            ConsolaUtils.imprimirInfo("Cita actual: " + actual.getFechaHora() + " - Estado: " + actual.getEstado().getDescripcion());

            LocalDate nuevaFecha = ConsolaUtils.leerFecha("Nueva fecha");
            LocalTime nuevaHora = ConsolaUtils.leerHora("Nueva hora");
            LocalDateTime nuevaFechaHora = LocalDateTime.of(nuevaFecha, nuevaHora);

            int vetIdInput = ConsolaUtils.leerEntero("Nuevo ID de veterinario (0 para mantener actual [" + actual.getVeterinarioId() + "]): ");
            Integer nuevoVetId = vetIdInput > 0 ? vetIdInput : actual.getVeterinarioId();

            boolean exito = controller.reprogramarCita(id, nuevaFechaHora, nuevoVetId);
            if (exito) {
                ConsolaUtils.imprimirExito("Cita ID " + id + " reprogramada para " + nuevaFechaHora);
            } else {
                ConsolaUtils.imprimirAdvertencia("No fue posible reprogramar la cita.");
            }

        } catch (ValidacionException | IllegalStateException e) {
            ConsolaUtils.imprimirAdvertencia(e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al reprogramar cita: " + e.getMessage());
        }
    }

    // Imprime la ficha detallada de la cita
    private void mostrarFichaDetalle(Cita c) {
        System.out.println();
        System.out.println(ConsolaUtils.COLOR_BORDE + "+----------------------------------------------------------------------------+" + ConsolaUtils.ANSI_RESET);
        System.out.printf(ConsolaUtils.COLOR_BORDE + "| " + ConsolaUtils.COLOR_TITULO + "FICHA CLINICA DE CITA MEDICA #%-44d" + ConsolaUtils.COLOR_BORDE + " |\n" + ConsolaUtils.ANSI_RESET, c.getId());
        System.out.println(ConsolaUtils.COLOR_BORDE + "+----------------------------------------------------------------------------+" + ConsolaUtils.ANSI_RESET);
        System.out.printf("| Estado:             %-55s |\n", formatearBadgeEstado(c.getEstado()));
        System.out.printf("| Fecha y Hora:       %-55s |\n", c.getFechaHora() != null ? c.getFechaHora().format(FORMATO_FECHA_HORA) : "--");
        System.out.printf("| Paciente (Mascota): %-55s |\n", c.getNombreMascota() != null ? c.getNombreMascota() + " (ID: " + c.getMascotaId() + ")" : String.valueOf(c.getMascotaId()));
        System.out.printf("| Dueño:              %-55s |\n", c.getNombreDueno() != null ? c.getNombreDueno() : "--");
        System.out.printf("| Teléfono Dueño:     %-55s |\n", c.getTelefonoDueno() != null ? c.getTelefonoDueno() : "--");
        System.out.printf("| Médico Veterinario: %-55s |\n", c.getNombreVeterinario() != null ? c.getNombreVeterinario() : "Sin asignar");
        System.out.printf("| Motivo Consulta:    %-55s |\n", c.getMotivo());
        System.out.printf("| Notas:              %-55s |\n", c.getNotas() != null && !c.getNotas().isEmpty() ? c.getNotas() : "Ninguna");

        if (c.getEstado() == EstadoCitaEnum.CANCELADA) {
            System.out.println(ConsolaUtils.COLOR_BORDE + "+----------------------------------------------------------------------------+" + ConsolaUtils.ANSI_RESET);
            System.out.printf("| Fecha Cancelación:  %-55s |\n", c.getFechaCancelacion() != null ? c.getFechaCancelacion().format(FORMATO_FECHA_HORA) : "--");
            System.out.printf("| Motivo Cancelación: %-55s |\n", c.getMotivoCancelacion() != null ? c.getMotivoCancelacion() : "--");
        }

        System.out.println(ConsolaUtils.COLOR_BORDE + "+----------------------------------------------------------------------------+" + ConsolaUtils.ANSI_RESET);
    }

    // Formatea el estado con color visual apropiado
    private String formatearBadgeEstado(EstadoCitaEnum estado) {
        if (estado == null) return "-";
        return switch (estado) {
            case PROGRAMADA -> ConsolaUtils.badgeInfo(estado.getDescripcion());
            case CONFIRMADA -> ConsolaUtils.COLOR_INFO + "[" + estado.getDescripcion() + "]" + ConsolaUtils.ANSI_RESET;
            case EN_CURSO -> ConsolaUtils.badgeAdvertencia(estado.getDescripcion());
            case FINALIZADA -> ConsolaUtils.badgeExito(estado.getDescripcion());
            case CANCELADA -> ConsolaUtils.badgeError(estado.getDescripcion());
            case NO_ASISTIO -> ConsolaUtils.COLOR_BORDE + "[" + estado.getDescripcion() + "]" + ConsolaUtils.ANSI_RESET;
        };
    }
}
