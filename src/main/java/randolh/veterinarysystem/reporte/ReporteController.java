package randolh.veterinarysystem.reporte;

import randolh.veterinarysystem.core.exportador.ExportadorFactory;
import randolh.veterinarysystem.core.exportador.FormatoExportacion;
import randolh.veterinarysystem.core.exportador.reporte.ReporteExportData;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// Controlador del modulo de reportes y business intelligence mediante Java 17 Stream API
public class ReporteController {

    private final ReporteDAO reporteDAO;

    public ReporteController() {
        this.reporteDAO = new ReporteDAO();
    }

    public ReporteController(ReporteDAO reporteDAO) {
        this.reporteDAO = reporteDAO != null ? reporteDAO : new ReporteDAO();
    }

    // Obtiene y procesa con Stream API el ranking de servicios y productos mas solicitados
    public List<ServicioSolicitadoDTO> obtenerTopServicios(int limite, String filtroTipo) {
        return reporteDAO.obtenerServiciosMasSolicitados().stream()
                .filter(dto -> filtroTipo == null || filtroTipo.trim().isEmpty() ||
                        "TODOS".equalsIgnoreCase(filtroTipo) ||
                        (dto.getTipoItem() != null && dto.getTipoItem().equalsIgnoreCase(filtroTipo.trim())))
                .sorted(Comparator.comparingDouble(ServicioSolicitadoDTO::getIngresosTotales).reversed())
                .limit(limite > 0 ? limite : 50)
                .collect(Collectors.toList());
    }

    // Calcula el total acumulado de ingresos del top de servicios usando mapToDouble y sum
    public double calcularIngresosTotalesTop(List<ServicioSolicitadoDTO> lista) {
        return lista.stream()
                .mapToDouble(ServicioSolicitadoDTO::getIngresosTotales)
                .sum();
    }

    // Obtiene la lista completa de desempeno de veterinarios
    public List<DesempenoVeterinarioDTO> obtenerDesempenoVeterinarios() {
        return reporteDAO.obtenerDesempenoVeterinarios().stream()
                .sorted(Comparator.comparingDouble(DesempenoVeterinarioDTO::getIngresosGenerados).reversed())
                .collect(Collectors.toList());
    }

    // Agrupa veterinarios por especialidad medica usando Collectors.groupingBy
    public Map<String, List<DesempenoVeterinarioDTO>> agruparVeterinariosPorEspecialidad() {
        return reporteDAO.obtenerDesempenoVeterinarios().stream()
                .collect(Collectors.groupingBy(v -> v.getEspecialidad() != null && !v.getEspecialidad().isBlank() ?
                        v.getEspecialidad() : "GENERAL"));
    }

    // Calcula estadisticas resumidas de ingresos generados por veterinarios
    public DoubleSummaryStatistics obtenerEstadisticasIngresosVeterinarios() {
        return reporteDAO.obtenerDesempenoVeterinarios().stream()
                .mapToDouble(DesempenoVeterinarioDTO::getIngresosGenerados)
                .summaryStatistics();
    }

    // Obtiene el veterinario con mayor numero de atenciones totales
    public Optional<DesempenoVeterinarioDTO> obtenerVeterinarioMasProductivo() {
        return reporteDAO.obtenerDesempenoVeterinarios().stream()
                .max(Comparator.comparingInt(DesempenoVeterinarioDTO::getTotalAtenciones));
    }

    // Obtiene alertas criticas de inventario filtrando por ventana de dias para vencer
    public List<AlertaInventarioCriticaDTO> obtenerAlertasInventario(Integer diasVentana, String filtroCategoria) {
        LocalDate hoy = LocalDate.now();
        return reporteDAO.obtenerAlertasInventarioCriticas().stream()
                .filter(a -> {
                    if (filtroCategoria == null || filtroCategoria.isBlank() || "TODAS".equalsIgnoreCase(filtroCategoria)) {
                        return true;
                    }
                    return a.getCategoria() != null && a.getCategoria().equalsIgnoreCase(filtroCategoria.trim());
                })
                .filter(a -> {
                    if (diasVentana == null || diasVentana <= 0) {
                        return true;
                    }
                    if (a.getFechaVencimiento() == null) {
                        return false;
                    }
                    return !a.getFechaVencimiento().isAfter(hoy.plusDays(diasVentana));
                })
                .sorted(Comparator.comparingInt(AlertaInventarioCriticaDTO::getCantidadStock))
                .collect(Collectors.toList());
    }

    // Agrupa alertas criticas por estado de alerta (CRITICO, BAJO, VENCIDO, etc.)
    public Map<String, List<AlertaInventarioCriticaDTO>> agruparAlertasPorEstado(List<AlertaInventarioCriticaDTO> alertas) {
        return alertas.stream()
                .collect(Collectors.groupingBy(a -> a.getEstadoAlerta() != null ? a.getEstadoAlerta() : "DESCONOCIDO"));
    }

    // Cuenta productos que se encuentran con stock agotado (0 unidades)
    public long contarProductosAgotados(List<AlertaInventarioCriticaDTO> alertas) {
        return alertas.stream()
                .filter(a -> a.getCantidadStock() <= 0)
                .count();
    }

    // Obtiene resumen de facturacion mensual ordenado cronologicamente
    public List<ResumenFacturacionMensualDTO> obtenerResumenFacturacion() {
        return reporteDAO.obtenerResumenFacturacionMensual().stream()
                .sorted(Comparator.comparingInt(ResumenFacturacionMensualDTO::getAnio)
                        .thenComparingInt(ResumenFacturacionMensualDTO::getMes))
                .collect(Collectors.toList());
    }

    // Agrupa facturacion mensual por anio
    public Map<Integer, List<ResumenFacturacionMensualDTO>> agruparFacturacionPorAnio() {
        return reporteDAO.obtenerResumenFacturacionMensual().stream()
                .collect(Collectors.groupingBy(ResumenFacturacionMensualDTO::getAnio));
    }

    // Calcula el total facturado global sumando todos los periodos
    public double calcularTotalFacturadoHistorico() {
        return reporteDAO.obtenerResumenFacturacionMensual().stream()
                .mapToDouble(ResumenFacturacionMensualDTO::getTotalFacturado)
                .sum();
    }

    // Identifica el mes con mayor facturacion registrada
    public Optional<ResumenFacturacionMensualDTO> obtenerMesRecordFacturacion() {
        return reporteDAO.obtenerResumenFacturacionMensual().stream()
                .max(Comparator.comparingDouble(ResumenFacturacionMensualDTO::getTotalFacturado));
    }

    // Exporta el top de servicios y productos mas solicitados a PDF formal
    public String exportarTopServiciosPdf(int limite, String filtroTipo) {
        List<ServicioSolicitadoDTO> datos = obtenerTopServicios(limite, filtroTipo);
        String subtitulo = (filtroTipo != null && !filtroTipo.isBlank()) ? " (Tipo: " + filtroTipo + ")" : "";
        String titulo = "REPORTE EJECUTIVO - SERVICIOS Y PRODUCTOS MAS DEMANDADOS" + subtitulo;

        List<String> cabeceras = Arrays.asList(
                "CONCEPTO", "TIPO", "SOLICITUDES", "CANTIDAD", "TOTAL INGRESOS"
        );

        List<List<String>> filas = datos.stream()
                .map(d -> Arrays.asList(
                        d.getConcepto(),
                        d.getTipoItem(),
                        String.valueOf(d.getTotalSolicitudes()),
                        String.valueOf(d.getCantidadTotal()),
                        String.format("$%.2f", d.getIngresosTotales())
                ))
                .collect(Collectors.toList());

        return exportarReporte(titulo, cabeceras, filas);
    }

    // Exporta desempeno de veterinarios a PDF formal
    public String exportarDesempenoVeterinariosPdf() {
        List<DesempenoVeterinarioDTO> datos = obtenerDesempenoVeterinarios();
        String titulo = "REPORTE EJECUTIVO - PRODUCTIVIDAD Y DESEMPENO VETERINARIO";

        List<String> cabeceras = Arrays.asList(
                "ID", "VETERINARIO", "LICENCIA", "ESPECIALIDAD", "CONSULTAS", "PROCEDIMIENTOS", "TOTAL ATENCIONES", "INGRESOS"
        );

        List<List<String>> filas = datos.stream()
                .map(d -> Arrays.asList(
                        String.valueOf(d.getVeterinarioId()),
                        d.getNombreCompleto(),
                        d.getLicenciaProfesional(),
                        d.getEspecialidad(),
                        String.valueOf(d.getTotalConsultas()),
                        String.valueOf(d.getTotalProcedimientos()),
                        String.valueOf(d.getTotalAtenciones()),
                        String.format("$%.2f", d.getIngresosGenerados())
                ))
                .collect(Collectors.toList());

        return exportarReporte(titulo, cabeceras, filas);
    }

    // Exporta alertas criticas de inventario a PDF formal
    public String exportarInventarioCriticoPdf(Integer diasVentana, String filtroCategoria) {
        List<AlertaInventarioCriticaDTO> datos = obtenerAlertasInventario(diasVentana, filtroCategoria);
        String titulo = "REPORTE EJECUTIVO - ALERTAS CRITICAS DE INVENTARIO Y VENCIMIENTOS";

        List<String> cabeceras = Arrays.asList(
                "ID", "CODIGO", "PRODUCTO", "CATEGORIA", "LOTE", "STOCK", "MINIMO", "VENCIMIENTO", "DIAS", "ESTADO"
        );

        List<List<String>> filas = datos.stream()
                .map(d -> Arrays.asList(
                        String.valueOf(d.getProductoId()),
                        d.getCodigoBarras() != null ? d.getCodigoBarras() : "-",
                        d.getNombreProducto(),
                        d.getCategoria() != null ? d.getCategoria() : "-",
                        d.getLote() != null ? d.getLote() : "-",
                        String.valueOf(d.getCantidadStock()),
                        String.valueOf(d.getStockMinimo()),
                        d.getFechaVencimiento() != null ? d.getFechaVencimiento().toString() : "N/A",
                        String.valueOf(d.getDiasParaVencer()),
                        d.getEstadoAlerta()
                ))
                .collect(Collectors.toList());

        return exportarReporte(titulo, cabeceras, filas);
    }

    // Exporta resumen mensual de facturacion a PDF formal
    public String exportarResumenFacturacionPdf() {
        List<ResumenFacturacionMensualDTO> datos = obtenerResumenFacturacion();
        String titulo = "REPORTE EJECUTIVO - ANALISIS Y FACTURACION MENSUAL";

        List<String> cabeceras = Arrays.asList(
                "AÑO", "MES", "EMITIDAS", "PAGADAS", "PENDIENTES", "ANULADAS", "SUBTOTAL", "DESCUENTOS", "IMPUESTOS", "TOTAL"
        );

        List<List<String>> filas = datos.stream()
                .map(d -> Arrays.asList(
                        String.valueOf(d.getAnio()),
                        String.format("%02d", d.getMes()),
                        String.valueOf(d.getFacturasEmitidas()),
                        String.valueOf(d.getFacturasPagadas()),
                        String.valueOf(d.getFacturasPendientes()),
                        String.valueOf(d.getFacturasAnuladas()),
                        String.format("$%.2f", d.getSubtotalNeto()),
                        String.format("$%.2f", d.getTotalDescuentos()),
                        String.format("$%.2f", d.getTotalImpuestos()),
                        String.format("$%.2f", d.getTotalFacturado())
                ))
                .collect(Collectors.toList());

        return exportarReporte(titulo, cabeceras, filas);
    }

    private String exportarReporte(String titulo, List<String> cabeceras, List<List<String>> filas) {
        ReporteExportData data = new ReporteExportData(titulo, cabeceras, filas);
        return ExportadorFactory.getExportadorReporte(FormatoExportacion.PDF).exportar(data);
    }
}
