package randolh.veterinarysystem.alerta;

import randolh.veterinarysystem.core.exception.ValidacionException;
import randolh.veterinarysystem.core.utils.ConsolaUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

// Vista de consola interactiva para el panel y centro de notificaciones de alertas
public class AlertaView {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AlertaController controller;

    public AlertaView() {
        this.controller = new AlertaController();
    }

    public AlertaView(AlertaController controller) {
        this.controller = controller;
    }

    // Menu principal del centro de notificaciones y alertas
    public void mostrarMenu() {
        Runnable menu = () -> {
            int pendientes = controller.contarAlertasPendientes();
            String badgePendientes = pendientes > 0 ?
                    ConsolaUtils.COLOR_ERROR + " [" + pendientes + " PENDIENTES]" + ConsolaUtils.ANSI_RESET :
                    ConsolaUtils.COLOR_EXITO + " [SIN PENDIENTES]" + ConsolaUtils.ANSI_RESET;

            ConsolaUtils.imprimirTitulo("CENTRO DE ALERTAS Y NOTIFICACIONES" + badgePendientes);
            System.out.println(ConsolaUtils.COLOR_INFO + "1. Ver dashboard de alertas pendientes (no atendidas)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "2. Marcar una alerta como atendida / leida" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "3. Marcar TODAS las alertas pendientes como atendidas" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "4. Ver historial completo de alertas (tabla paginada)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "5. Sincronizar y generar alertas automaticas de inventario" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "6. Emitir alerta o aviso manual" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "0. Volver al menu anterior" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "----------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);
        };

        ConsolaUtils.setMenuActual(menu);

        boolean continuar = true;
        while (continuar) {
            menu.run();
            int opcion = ConsolaUtils.leerEnteroEnRango("Seleccione una opcion [0-6]: ", 0, 6);

            switch (opcion) {
                case 1 -> mostrarDashboardPendientes();
                case 2 -> marcarAlertaAtendida();
                case 3 -> marcarTodasAtendidas();
                case 4 -> mostrarHistorialPaginado();
                case 5 -> sincronizarAlertas();
                case 6 -> emitirAlertaManual();
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

    // Dashboard visual de alertas pendientes priorizadas
    private void mostrarDashboardPendientes() {
        ConsolaUtils.imprimirSubtitulo("Dashboard de Alertas Pendientes de Atencion");

        try {
            List<Alerta> pendientes = controller.obtenerAlertasPendientes();
            if (pendientes.isEmpty()) {
                ConsolaUtils.imprimirExito("Excelente: El sistema no registra alertas pendientes.");
                return;
            }

            System.out.println(ConsolaUtils.ANSI_GRAY + "Total de alertas pendientes: " + pendientes.size() + "\n" + ConsolaUtils.ANSI_RESET);

            for (Alerta a : pendientes) {
                String badgeSeveridad = formatearBadgeSeveridad(a.getNivelSeveridad());
                String fStr = a.getFechaGeneracion() != null ? a.getFechaGeneracion().format(FORMATO_FECHA_HORA) : "--";

                System.out.println(ConsolaUtils.COLOR_BORDE + "+----------------------------------------------------------------------------+" + ConsolaUtils.ANSI_RESET);
                System.out.printf("| [%d] %-14s %-45s %s |\n",
                        a.getId(), a.getTipoAlerta().getDescripcion(), a.getTitulo(), badgeSeveridad);
                System.out.println(ConsolaUtils.COLOR_BORDE + "+----------------------------------------------------------------------------+" + ConsolaUtils.ANSI_RESET);
                System.out.printf("| Fecha: %-67s |\n", fStr);
                if (a.getNombreProducto() != null) {
                    System.out.printf("| Articulo vinculado: %-54s |\n", a.getNombreProducto() + " (ID: " + a.getProductoId() + ")");
                }
                System.out.printf("| Mensaje: %-65s |\n", truncar(a.getMensaje(), 65));
                System.out.println(ConsolaUtils.COLOR_BORDE + "+----------------------------------------------------------------------------+\n" + ConsolaUtils.ANSI_RESET);
            }

        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al consultar dashboard de alertas: " + e.getMessage());
        }
    }

    // Marca una alerta como atendida
    private void marcarAlertaAtendida() {
        ConsolaUtils.imprimirSubtitulo("Marcar Alerta como Atendida");

        int id = ConsolaUtils.leerEntero("ID de la alerta a marcar como atendida: ");
        try {
            Optional<Alerta> opt = controller.buscarPorId(id);
            if (opt.isEmpty()) {
                ConsolaUtils.imprimirError("No existe alerta con ID: " + id);
                return;
            }

            boolean exito = controller.marcarComoAtendida(id);
            if (exito) {
                ConsolaUtils.imprimirExito("Alerta ID " + id + " marcada como atendida exitosamente.");
            } else {
                ConsolaUtils.imprimirAdvertencia("No fue posible actualizar el estado de la alerta.");
            }

        } catch (ValidacionException e) {
            ConsolaUtils.imprimirAdvertencia(e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al atender alerta: " + e.getMessage());
        }
    }

    // Marca todas las alertas como atendidas
    private void marcarTodasAtendidas() {
        ConsolaUtils.imprimirSubtitulo("Marcar Todas las Alertas como Atendidas");

        boolean confirma = ConsolaUtils.leerBooleano("Confirma que desea marcar TODAS las alertas pendientes como leidas/atendidas?");
        if (!confirma) {
            ConsolaUtils.imprimirInfo("Operacion cancelada.");
            return;
        }

        try {
            int total = controller.marcarTodasComoAtendidas();
            ConsolaUtils.imprimirExito("Se marcaron " + total + " alertas como atendidas.");
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al marcar todas las alertas: " + e.getMessage());
        }
    }

    // Historial paginado de todas las alertas
    private void mostrarHistorialPaginado() {
        ConsolaUtils.imprimirSubtitulo("Historial Completo de Alertas del Sistema");

        try {
            List<Alerta> todas = controller.obtenerTodas();
            if (todas.isEmpty()) {
                ConsolaUtils.imprimirInfo("No hay registros historicos de alertas en el sistema.");
                return;
            }

            ConsolaUtils.mostrarTablaPaginada(todas, 8, this::renderizarTablaAlertas);

        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al consultar historial de alertas: " + e.getMessage());
        }
    }

    // Renderiza la tabla de alertas
    private void renderizarTablaAlertas(List<Alerta> lista) {
        String formatoEncabezado = "| %-4s | %-16s | %-10s | %-12s | %-24s | %-6s |%n";
        String formatoFila = "| %-4d | %-16s | %-10s | %-12s | %-24s | %-6s |%n";
        String separador = "+------+------------------+------------+--------------+--------------------------+--------+";

        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);
        System.out.printf(ConsolaUtils.COLOR_TITULO + formatoEncabezado + ConsolaUtils.ANSI_RESET,
                "ID", "FECHA/HORA", "SEVERIDAD", "TIPO", "TITULO", "ESTADO");
        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);

        for (Alerta a : lista) {
            String fStr = a.getFechaGeneracion() != null ? a.getFechaGeneracion().format(FORMATO_FECHA_HORA) : "--";
            String estado = a.isLeida() ? "LEIDA" : "PEND";
            String titulo = truncar(a.getTitulo(), 24);

            System.out.printf(formatoFila, a.getId(), fStr, a.getNivelSeveridad().getValorDb(),
                    a.getTipoAlerta().getDescripcion(), titulo, estado);
        }
        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);
    }

    // Sincroniza y ejecuta deteccion de alertas
    private void sincronizarAlertas() {
        ConsolaUtils.imprimirSubtitulo("Sincronizar y Generar Alertas Automaticas");

        try {
            int nuevas = controller.sincronizarAlertas();
            ConsolaUtils.imprimirExito("Sincronizacion completada con exito. Nuevas alertas identificadas: " + nuevas);
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error durante la sincronizacion: " + e.getMessage());
        }
    }

    // Emite una alerta manual
    private void emitirAlertaManual() {
        ConsolaUtils.imprimirSubtitulo("Emitir Alerta o Notificacion Manual");

        try {
            System.out.println("Seleccione tipo de alerta:");
            System.out.println("1. Stock Minimo");
            System.out.println("2. Proximo a Vencer");
            System.out.println("3. Vencido");
            System.out.println("4. Cita Proxima");
            int tipoOp = ConsolaUtils.leerEnteroEnRango("Opcion [1-4]: ", 1, 4);
            TipoAlertaEnum tipo = switch (tipoOp) {
                case 1 -> TipoAlertaEnum.STOCK_MINIMO;
                case 2 -> TipoAlertaEnum.POR_VENCER;
                case 3 -> TipoAlertaEnum.VENCIDO;
                case 4 -> TipoAlertaEnum.CITA_PROXIMA;
                default -> TipoAlertaEnum.STOCK_MINIMO;
            };

            System.out.println("Seleccione severidad:");
            System.out.println("1. Informativa (INFO)");
            System.out.println("2. Advertencia (ADVERTENCIA)");
            System.out.println("3. Critica (CRITICO)");
            int sevOp = ConsolaUtils.leerEnteroEnRango("Opcion [1-3]: ", 1, 3);
            NivelSeveridadEnum sev = switch (sevOp) {
                case 1 -> NivelSeveridadEnum.INFO;
                case 2 -> NivelSeveridadEnum.ADVERTENCIA;
                case 3 -> NivelSeveridadEnum.CRITICO;
                default -> NivelSeveridadEnum.INFO;
            };

            String titulo = ConsolaUtils.leerTextoObligatorio("Titulo de la alerta: ", 150);
            String mensaje = ConsolaUtils.leerTextoObligatorio("Mensaje explicativo: ", 500);

            Alerta guardada = controller.emitirAlerta(tipo, sev, titulo, mensaje, null, null);
            ConsolaUtils.imprimirExito("Alerta emitida correctamente con ID: " + guardada.getId());

        } catch (ValidacionException e) {
            ConsolaUtils.imprimirAdvertencia(e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al emitir alerta manual: " + e.getMessage());
        }
    }

    // Formato visual segun severidad
    private String formatearBadgeSeveridad(NivelSeveridadEnum severidad) {
        if (severidad == null) return "-";
        return switch (severidad) {
            case CRITICO -> ConsolaUtils.badgeError("CRITICO");
            case ADVERTENCIA -> ConsolaUtils.badgeAdvertencia("ADVERTENCIA");
            case INFO -> ConsolaUtils.badgeInfo("INFO");
        };
    }

    private String truncar(String texto, int max) {
        if (texto == null) return "-";
        return texto.length() <= max ? texto : texto.substring(0, max - 3) + "...";
    }
}
