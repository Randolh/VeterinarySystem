package randolh.veterinarysystem.reporte;

import java.time.LocalDate;

// DTO para el analisis de criticidad de existencias y ventana de caducidad de inventario
public class AlertaInventarioCriticaDTO {

    private int productoId;
    private String codigoBarras;
    private String nombreProducto;
    private String categoria;
    private String lote;
    private int cantidadStock;
    private int stockMinimo;
    private LocalDate fechaVencimiento;
    private int diasParaVencer;
    private String estadoAlerta;

    public AlertaInventarioCriticaDTO() {
    }

    public AlertaInventarioCriticaDTO(int productoId, String codigoBarras, String nombreProducto,
                                      String categoria, String lote, int cantidadStock, int stockMinimo,
                                      LocalDate fechaVencimiento, int diasParaVencer, String estadoAlerta) {
        this.productoId = productoId;
        this.codigoBarras = codigoBarras;
        this.nombreProducto = nombreProducto;
        this.categoria = categoria;
        this.lote = lote;
        this.cantidadStock = cantidadStock;
        this.stockMinimo = stockMinimo;
        this.fechaVencimiento = fechaVencimiento;
        this.diasParaVencer = diasParaVencer;
        this.estadoAlerta = estadoAlerta;
    }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public int getCantidadStock() { return cantidadStock; }
    public void setCantidadStock(int cantidadStock) { this.cantidadStock = cantidadStock; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public int getDiasParaVencer() { return diasParaVencer; }
    public void setDiasParaVencer(int diasParaVencer) { this.diasParaVencer = diasParaVencer; }

    public String getEstadoAlerta() { return estadoAlerta; }
    public void setEstadoAlerta(String estadoAlerta) { this.estadoAlerta = estadoAlerta; }

    @Override
    public String toString() {
        return String.format("[%d] %-28s | Lote: %-12s | Stock: %3d/%-3d | Vence: %s (%3d d) | %s",
                productoId, nombreProducto, lote, cantidadStock, stockMinimo,
                fechaVencimiento != null ? fechaVencimiento : "-", diasParaVencer, estadoAlerta);
    }
}
