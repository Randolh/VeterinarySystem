package randolh.veterinarysystem.core.exportador;

import randolh.veterinarysystem.core.exportador.carnet.CarnetSaludExportData;
import randolh.veterinarysystem.core.exportador.carnet.CarnetSaludPdfExportador;
import randolh.veterinarysystem.core.exportador.carnet.CarnetSaludTextoExportador;
import randolh.veterinarysystem.core.exportador.contrato.ContratoAdopcionExportData;
import randolh.veterinarysystem.core.exportador.contrato.ContratoAdopcionPdfExportador;
import randolh.veterinarysystem.core.exportador.contrato.ContratoAdopcionTextoExportador;
import randolh.veterinarysystem.core.exportador.factura.FacturaExportData;
import randolh.veterinarysystem.core.exportador.factura.FacturaPdfExportador;
import randolh.veterinarysystem.core.exportador.factura.FacturaTextoExportador;
import randolh.veterinarysystem.core.exportador.reporte.ReporteExportData;
import randolh.veterinarysystem.core.exportador.reporte.ReportePdfExportador;
import randolh.veterinarysystem.core.exportador.reporte.ReporteTextoExportador;

// Fabrica desacoplada para resolver estrategias de exportacion segun el formato (Open/Closed Principle)
public final class ExportadorFactory {

    private ExportadorFactory() {}

    // Obtiene la estrategia adecuada para exportacion de facturas
    public static ExportadorDocumento<FacturaExportData> getExportadorFactura(FormatoExportacion formato) {
        return switch (formato) {
            case PDF -> new FacturaPdfExportador();
            case TEXTO -> new FacturaTextoExportador();
        };
    }

    // Obtiene la estrategia adecuada para exportacion de contratos de adopcion
    public static ExportadorDocumento<ContratoAdopcionExportData> getExportadorContrato(FormatoExportacion formato) {
        return switch (formato) {
            case PDF -> new ContratoAdopcionPdfExportador();
            case TEXTO -> new ContratoAdopcionTextoExportador();
        };
    }

    // Obtiene la estrategia adecuada para exportacion de reportes tabulares
    public static ExportadorDocumento<ReporteExportData> getExportadorReporte(FormatoExportacion formato) {
        return switch (formato) {
            case PDF -> new ReportePdfExportador();
            case TEXTO -> new ReporteTextoExportador();
        };
    }

    // Obtiene la estrategia adecuada para exportacion de carnet de salud
    public static ExportadorDocumento<CarnetSaludExportData> getExportadorCarnet(FormatoExportacion formato) {
        return switch (formato) {
            case PDF -> new CarnetSaludPdfExportador();
            case TEXTO -> new CarnetSaludTextoExportador();
        };
    }
}

