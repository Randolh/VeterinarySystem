package randolh.veterinarysystem.reporte;

import java.math.BigDecimal;

// DTO para el analisis de demanda de servicios clinicos y articulos de farmacia
public class ServicioSolicitadoDTO {

    private String concepto;
    private String tipoItem;
    private int totalSolicitudes;
    private int cantidadTotal;
    private double ingresosTotales;

    public ServicioSolicitadoDTO() {
    }

    public ServicioSolicitadoDTO(String concepto, String tipoItem, int totalSolicitudes, int cantidadTotal, double ingresosTotales) {
        this.concepto = concepto;
        this.tipoItem = tipoItem;
        this.totalSolicitudes = totalSolicitudes;
        this.cantidadTotal = cantidadTotal;
        this.ingresosTotales = ingresosTotales;
    }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public String getTipoItem() { return tipoItem; }
    public void setTipoItem(String tipoItem) { this.tipoItem = tipoItem; }

    public int getTotalSolicitudes() { return totalSolicitudes; }
    public void setTotalSolicitudes(int totalSolicitudes) { this.totalSolicitudes = totalSolicitudes; }

    public int getCantidadTotal() { return cantidadTotal; }
    public void setCantidadTotal(int cantidadTotal) { this.cantidadTotal = cantidadTotal; }

    public double getIngresosTotales() { return ingresosTotales; }
    public void setIngresosTotales(double ingresosTotales) { this.ingresosTotales = ingresosTotales; }

    @Override
    public String toString() {
        return String.format("%-32s | %-10s | %5d sol. | %5d unid. | $ %10.2f",
                concepto, tipoItem, totalSolicitudes, cantidadTotal, ingresosTotales);
    }
}
