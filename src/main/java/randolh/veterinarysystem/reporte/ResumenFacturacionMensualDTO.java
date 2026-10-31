package randolh.veterinarysystem.reporte;

// DTO para el analisis financiero y tributario mensual consolidado
public class ResumenFacturacionMensualDTO {

    private int anio;
    private int mes;
    private int facturasEmitidas;
    private int facturasPagadas;
    private int facturasPendientes;
    private int facturasAnuladas;
    private double subtotalNeto;
    private double totalDescuentos;
    private double totalImpuestos;
    private double totalFacturado;

    public ResumenFacturacionMensualDTO() {
    }

    public ResumenFacturacionMensualDTO(int anio, int mes, int facturasEmitidas, int facturasPagadas,
                                        int facturasPendientes, int facturasAnuladas, double subtotalNeto,
                                        double totalDescuentos, double totalImpuestos, double totalFacturado) {
        this.anio = anio;
        this.mes = mes;
        this.facturasEmitidas = facturasEmitidas;
        this.facturasPagadas = facturasPagadas;
        this.facturasPendientes = facturasPendientes;
        this.facturasAnuladas = facturasAnuladas;
        this.subtotalNeto = subtotalNeto;
        this.totalDescuentos = totalDescuentos;
        this.totalImpuestos = totalImpuestos;
        this.totalFacturado = totalFacturado;
    }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }

    public String getPeriodo() {
        return String.format("%04d-%02d", anio, mes);
    }

    public int getFacturasEmitidas() { return facturasEmitidas; }
    public void setFacturasEmitidas(int facturasEmitidas) { this.facturasEmitidas = facturasEmitidas; }

    public int getFacturasPagadas() { return facturasPagadas; }
    public void setFacturasPagadas(int facturasPagadas) { this.facturasPagadas = facturasPagadas; }

    public int getFacturasPendientes() { return facturasPendientes; }
    public void setFacturasPendientes(int facturasPendientes) { this.facturasPendientes = facturasPendientes; }

    public int getFacturasAnuladas() { return facturasAnuladas; }
    public void setFacturasAnuladas(int facturasAnuladas) { this.facturasAnuladas = facturasAnuladas; }

    public double getSubtotalNeto() { return subtotalNeto; }
    public void setSubtotalNeto(double subtotalNeto) { this.subtotalNeto = subtotalNeto; }

    public double getTotalDescuentos() { return totalDescuentos; }
    public void setTotalDescuentos(double totalDescuentos) { this.totalDescuentos = totalDescuentos; }

    public double getTotalImpuestos() { return totalImpuestos; }
    public void setTotalImpuestos(double totalImpuestos) { this.totalImpuestos = totalImpuestos; }

    public double getTotalFacturado() { return totalFacturado; }
    public void setTotalFacturado(double totalFacturado) { this.totalFacturado = totalFacturado; }

    @Override
    public String toString() {
        return String.format("%04d-%02d | %4d fact. (Pag:%d, Pend:%d, Anul:%d) | Sub: $ %9.2f | Desc: -$ %7.2f | IVA: $ %8.2f | Total: $ %10.2f",
                anio, mes, facturasEmitidas, facturasPagadas, facturasPendientes, facturasAnuladas,
                subtotalNeto, totalDescuentos, totalImpuestos, totalFacturado);
    }
}
