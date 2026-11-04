package randolh.veterinarysystem.reporte;

import randolh.veterinarysystem.core.utils.ConsolaUtils;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Vista de consola para el modulo de reportes analiticos e inteligencia de negocio
public class ReporteView {

    private final ReporteController controller;

    public ReporteView() {
        this.controller = new ReporteController();
    }

    public ReporteView(ReporteController controller) {
        this.controller = controller != null ? controller : new ReporteController();
    }

    // Despliega el menu principal del modulo de reportes
    public void mostrarMenu() {
        Runnable menu = () -> {
            ConsolaUtils.imprimirTitulo("MODULO DE REPORTES Y BUSINESS INTELLIGENCE (STREAM API)");
            System.out.println(ConsolaUtils.COLOR_INFO + "1. Top servicios y productos mas demandados (Ranking + PDF)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "2. Desempeno y productividad del personal veterinario (Metricas + PDF)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "3. Alertas criticas de inventario y vencimientos (Filtrado + PDF)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "4. Resumen y analisis de facturacion mensual (Consolidado + PDF)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "0. Volver al menu anterior" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "----------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);
        };

        ConsolaUtils.setMenuActual(menu);

        boolean continuar = true;
        while (continuar) {
            menu.run();
            int opcion = ConsolaUtils.leerEntero("Seleccione una opcion [0-4]: ");

            switch (opcion) {
                case 1:
                    mostrarTopServicios();
                    break;
                case 2:
                    mostrarDesempenoVeterinarios();
                    break;
                case 3:
                    mostrarAlertasInventario();
                    break;
                case 4:
                    mostrarResumenFacturacion();
                    break;
                case 0:
                    continuar = false;
                    break;
                default:
                    ConsolaUtils.imprimirAdvertencia("Opcion no valida. Seleccione un valor entre 0 y 4.");
            }

            if (continuar) {
                ConsolaUtils.pausar();
            }
        }
    }

    // Muestra el reporte analitico de servicios y productos mas demandados
    private void mostrarTopServicios() {
        ConsolaUtils.imprimirSubtitulo("RANKING DE SERVICIOS Y PRODUCTOS MAS DEMANDADOS");
        int limite = ConsolaUtils.leerEntero("Cantidad maxima de registros a mostrar (ej. 10): ");
        if (limite <= 0) {
            limite = 10;
        }

        System.out.println("Filtro de tipo [TODOS / SERVICIO / PRODUCTO]:");
        String tipo = ConsolaUtils.leerTexto("Tipo [TODOS]: ");
        if (tipo == null || tipo.isBlank()) {
            tipo = "TODOS";
        }

        List<ServicioSolicitadoDTO> lista = controller.obtenerTopServicios(limite, tipo);
        if (lista.isEmpty()) {
            ConsolaUtils.imprimirAdvertencia("No se encontraron registros de demanda con los filtros especificados.");
            return;
        }

        System.out.printf(ConsolaUtils.ANSI_BOLD + "%-35s %-12s %12s %10s %15s%n" + ConsolaUtils.ANSI_RESET,
                "CONCEPTO", "TIPO", "SOLICITUDES", "CANTIDAD", "TOTAL INGRESOS");
        System.out.println("-".repeat(90));

        lista.forEach(s -> System.out.printf("%-35s %-12s %12d %10d %15.2f%n",
                truncar(s.getConcepto(), 33),
                s.getTipoItem(),
                s.getTotalSolicitudes(),
                s.getCantidadTotal(),
                s.getIngresosTotales()));

        System.out.println("-".repeat(90));
        double total = controller.calcularIngresosTotalesTop(lista);
        System.out.printf(ConsolaUtils.COLOR_EXITO + "Total Ingresos Top Analizado: $%.2f%n" + ConsolaUtils.ANSI_RESET, total);

        String exportar = ConsolaUtils.leerTexto("Desea exportar este reporte a PDF formal? [S/N]: ");
        if ("S".equalsIgnoreCase(exportar)) {
            String ruta = controller.exportarTopServiciosPdf(limite, tipo);
            if (ruta != null) {
                ConsolaUtils.imprimirExito("Reporte exportado correctamente a: " + ruta);
            } else {
                ConsolaUtils.imprimirError("No fue posible exportar el reporte a PDF.");
            }
        }
    }

    // Muestra metricas de productividad y desempeno de los medicos veterinarios
    private void mostrarDesempenoVeterinarios() {
        ConsolaUtils.imprimirSubtitulo("PRODUCTIVIDAD Y DESEMPENO DEL EQUIPO VETERINARIO");

        List<DesempenoVeterinarioDTO> lista = controller.obtenerDesempenoVeterinarios();
        if (lista.isEmpty()) {
            ConsolaUtils.imprimirAdvertencia("No se registran atenciones veterinarias en el sistema.");
            return;
        }

        System.out.printf(ConsolaUtils.ANSI_BOLD + "%-4s %-25s %-15s %-28s %10s %14s %11s %12s%n" + ConsolaUtils.ANSI_RESET,
                "ID", "VETERINARIO", "LICENCIA", "ESPECIALIDAD", "CONSULTAS", "PROCEDIMIENTOS", "ATENCIONES", "INGRESOS");
        System.out.println("-".repeat(125));

        lista.forEach(v -> System.out.printf("%-4d %-25s %-15s %-28s %10d %14d %11d %12.2f%n",
                v.getVeterinarioId(),
                truncar(v.getNombreCompleto(), 24),
                truncar(v.getLicenciaProfesional() != null ? v.getLicenciaProfesional() : "-", 15),
                truncar(v.getEspecialidad() != null ? v.getEspecialidad() : "GENERAL", 28),
                v.getTotalConsultas(),
                v.getTotalProcedimientos(),
                v.getTotalAtenciones(),
                v.getIngresosGenerados()));

        System.out.println("-".repeat(125));

        DoubleSummaryStatistics stats = controller.obtenerEstadisticasIngresosVeterinarios();
        System.out.printf(ConsolaUtils.COLOR_INFO + "Veterinarios Evaluados: %d | Ingresos Totales: $%.2f | Promedio por Medico: $%.2f%n" + ConsolaUtils.ANSI_RESET,
                stats.getCount(), stats.getSum(), stats.getAverage());

        Optional<DesempenoVeterinarioDTO> lider = controller.obtenerVeterinarioMasProductivo();
        lider.ifPresent(l -> System.out.println(ConsolaUtils.COLOR_EXITO + "Veterinario Lider en Atenciones: " +
                l.getNombreCompleto() + " (" + l.getTotalAtenciones() + " atenciones totales)" + ConsolaUtils.ANSI_RESET));

        // Resumen de agrupacion funcional por especialidad
        System.out.println("\n" + ConsolaUtils.ANSI_BOLD + "Distribucion por Especialidad Medica:" + ConsolaUtils.ANSI_RESET);
        Map<String, List<DesempenoVeterinarioDTO>> porEsp = controller.agruparVeterinariosPorEspecialidad();
        porEsp.forEach((esp, meds) -> {
            int totalAten = meds.stream().mapToInt(DesempenoVeterinarioDTO::getTotalAtenciones).sum();
            double totalIng = meds.stream().mapToDouble(DesempenoVeterinarioDTO::getIngresosGenerados).sum();
            System.out.printf("  - %-20s: %d medicos | %d atenciones | $%.2f en ingresos%n",
                    esp, meds.size(), totalAten, totalIng);
        });

        String exportar = ConsolaUtils.leerTexto("\nDesea exportar este reporte a PDF formal? [S/N]: ");
        if ("S".equalsIgnoreCase(exportar)) {
            String ruta = controller.exportarDesempenoVeterinariosPdf();
            if (ruta != null) {
                ConsolaUtils.imprimirExito("Reporte exportado correctamente a: " + ruta);
            } else {
                ConsolaUtils.imprimirError("No fue posible exportar el reporte a PDF.");
            }
        }
    }

    // Muestra alertas criticas de inventario y proximidad de vencimiento
    private void mostrarAlertasInventario() {
        ConsolaUtils.imprimirSubtitulo("ALERTAS CRITICAS DE INVENTARIO Y VENCIMIENTOS");
        System.out.println("Ventana de dias para vencimiento (ingrese 0 para ver todas las alertas):");
        int dias = ConsolaUtils.leerEntero("Dias ventana [ej. 30]: ");
        Integer ventanaDias = dias > 0 ? dias : null;

        String categoria = ConsolaUtils.leerTexto("Filtro por categoria (o ENTER para todas): ");

        List<AlertaInventarioCriticaDTO> alertas = controller.obtenerAlertasInventario(ventanaDias, categoria);
        if (alertas.isEmpty()) {
            ConsolaUtils.imprimirExito("No se detectan alertas criticas de inventario segun los criterios indicados.");
            return;
        }

        System.out.printf(ConsolaUtils.ANSI_BOLD + "%-4s %-12s %-25s %-14s %-10s %6s %6s %-12s %6s %-14s%n" + ConsolaUtils.ANSI_RESET,
                "ID", "CODIGO", "PRODUCTO", "CATEGORIA", "LOTE", "STOCK", "MIN", "VENCE", "DIAS", "ESTADO");
        System.out.println("-".repeat(116));

        alertas.forEach(a -> System.out.printf("%-4d %-12s %-25s %-14s %-10s %6d %6d %-12s %6s %-14s%n",
                a.getProductoId(),
                a.getCodigoBarras() != null ? a.getCodigoBarras() : "-",
                truncar(a.getNombreProducto(), 24),
                a.getCategoria() != null ? truncar(a.getCategoria(), 13) : "-",
                a.getLote() != null ? a.getLote() : "-",
                a.getCantidadStock(),
                a.getStockMinimo(),
                a.getFechaVencimiento() != null ? a.getFechaVencimiento().toString() : "N/A",
                String.valueOf(a.getDiasParaVencer()),
                colorearEstadoAlerta(a.getEstadoAlerta())));

        System.out.println("-".repeat(116));

        long agotados = controller.contarProductosAgotados(alertas);
        System.out.printf(ConsolaUtils.COLOR_ADVERTENCIA + "Total Productos en Alerta: %d | Productos con Stock Agotado: %d%n" + ConsolaUtils.ANSI_RESET,
                alertas.size(), agotados);

        Map<String, List<AlertaInventarioCriticaDTO>> porEstado = controller.agruparAlertasPorEstado(alertas);
        System.out.println(ConsolaUtils.ANSI_BOLD + "Desglose por Estado:" + ConsolaUtils.ANSI_RESET);
        porEstado.forEach((est, prods) -> System.out.printf("  - %-16s: %d productos%n", est, prods.size()));

        String exportar = ConsolaUtils.leerTexto("\nDesea exportar este reporte a PDF formal? [S/N]: ");
        if ("S".equalsIgnoreCase(exportar)) {
            String ruta = controller.exportarInventarioCriticoPdf(ventanaDias, categoria);
            if (ruta != null) {
                ConsolaUtils.imprimirExito("Reporte exportado correctamente a: " + ruta);
            } else {
                ConsolaUtils.imprimirError("No fue posible exportar el reporte a PDF.");
            }
        }
    }

    // Muestra consolidado mensual y analisis historico de facturacion
    private void mostrarResumenFacturacion() {
        ConsolaUtils.imprimirSubtitulo("RESUMEN Y ANALISIS MENSUAL DE FACTURACION");

        List<ResumenFacturacionMensualDTO> lista = controller.obtenerResumenFacturacion();
        if (lista.isEmpty()) {
            ConsolaUtils.imprimirAdvertencia("No se registran periodos de facturacion en la base de datos.");
            return;
        }

        System.out.printf(ConsolaUtils.ANSI_BOLD + "%-6s %-6s %8s %8s %8s %8s %14s %12s %12s %14s%n" + ConsolaUtils.ANSI_RESET,
                "AÑO", "MES", "EMITIDAS", "PAGADAS", "PEND", "ANUL", "SUBTOTAL", "DESCUENTOS", "IMPUESTOS", "TOTAL");
        System.out.println("-".repeat(104));

        lista.forEach(r -> System.out.printf("%-6d %-6s %8d %8d %8d %8d %14.2f %12.2f %12.2f %14.2f%n",
                r.getAnio(),
                String.format("%02d", r.getMes()),
                r.getFacturasEmitidas(),
                r.getFacturasPagadas(),
                r.getFacturasPendientes(),
                r.getFacturasAnuladas(),
                r.getSubtotalNeto(),
                r.getTotalDescuentos(),
                r.getTotalImpuestos(),
                r.getTotalFacturado()));

        System.out.println("-".repeat(104));

        double granTotal = controller.calcularTotalFacturadoHistorico();
        System.out.printf(ConsolaUtils.COLOR_EXITO + "Total Facturado Historico: $%.2f%n" + ConsolaUtils.ANSI_RESET, granTotal);

        Optional<ResumenFacturacionMensualDTO> record = controller.obtenerMesRecordFacturacion();
        record.ifPresent(rc -> System.out.printf(ConsolaUtils.COLOR_INFO + "Periodo Record: %02d/%d con un total facturado de $%.2f%n" + ConsolaUtils.ANSI_RESET,
                rc.getMes(), rc.getAnio(), rc.getTotalFacturado()));

        String exportar = ConsolaUtils.leerTexto("\nDesea exportar este reporte a PDF formal? [S/N]: ");
        if ("S".equalsIgnoreCase(exportar)) {
            String ruta = controller.exportarResumenFacturacionPdf();
            if (ruta != null) {
                ConsolaUtils.imprimirExito("Reporte exportado correctamente a: " + ruta);
            } else {
                ConsolaUtils.imprimirError("No fue posible exportar el reporte a PDF.");
            }
        }
    }

    // Colorea semantica y limpiamente el estado de la alerta
    private String colorearEstadoAlerta(String estado) {
        if (estado == null) return "-";
        switch (estado.toUpperCase()) {
            case "CRITICO":
            case "VENCIDO":
                return ConsolaUtils.COLOR_ERROR + estado + ConsolaUtils.ANSI_RESET;
            case "BAJO":
            case "PROXIMO_VENCER":
                return ConsolaUtils.COLOR_ADVERTENCIA + estado + ConsolaUtils.ANSI_RESET;
            default:
                return estado;
        }
    }

    // Trunca cadenas largas para evitar desfases en tablas de consola
    private String truncar(String texto, int longitudMaxima) {
        if (texto == null) return "";
        if (texto.length() <= longitudMaxima) return texto;
        return texto.substring(0, longitudMaxima - 3) + "...";
    }
}
