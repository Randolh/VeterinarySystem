package randolh.veterinarysystem.facturacion;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Linea o concepto individual facturado (producto o servicio)
public class ItemFactura {

    private Integer id;
    private Integer facturaId;
    private TipoItemEnum tipoItem;
    private Integer productoId;
    private Integer servicioId;
    private String servicioDescripcion;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    // Campo auxiliar para visualizacion e impresion
    private String nombreItem;

    public ItemFactura() {
        this.tipoItem = TipoItemEnum.PRODUCTO;
        this.cantidad = 1;
        this.precioUnitario = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
    }

    public ItemFactura(Integer id, Integer facturaId, TipoItemEnum tipoItem, Integer productoId,
                       Integer servicioId, String servicioDescripcion, int cantidad,
                       BigDecimal precioUnitario, BigDecimal subtotal) {
        this.id = id;
        this.facturaId = facturaId;
        this.tipoItem = tipoItem != null ? tipoItem : TipoItemEnum.PRODUCTO;
        this.productoId = productoId;
        this.servicioId = servicioId;
        this.servicioDescripcion = servicioDescripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
        this.subtotal = subtotal != null ? subtotal :
                this.precioUnitario.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
    }

    // Constructor de conveniencia para producto de inventario
    public static ItemFactura paraProducto(int productoId, String nombreProducto, int cantidad, BigDecimal precioUnitario) {
        ItemFactura item = new ItemFactura(null, null, TipoItemEnum.PRODUCTO, productoId, null,
                nombreProducto, cantidad, precioUnitario, null);
        item.setNombreItem(nombreProducto);
        return item;
    }

    // Constructor de conveniencia para servicio o prestacion medica
    public static ItemFactura paraServicio(Integer servicioId, String descripcionServicio, int cantidad, BigDecimal precioUnitario) {
        ItemFactura item = new ItemFactura(null, null, TipoItemEnum.SERVICIO, null, servicioId != null && servicioId > 0 ? servicioId : 1,
                descripcionServicio, cantidad, precioUnitario, null);
        item.setNombreItem(descripcionServicio);
        return item;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getFacturaId() { return facturaId; }
    public void setFacturaId(Integer facturaId) { this.facturaId = facturaId; }

    public TipoItemEnum getTipoItem() { return tipoItem; }
    public void setTipoItem(TipoItemEnum tipoItem) { this.tipoItem = tipoItem; }

    public Integer getProductoId() { return productoId; }
    public void setProductoId(Integer productoId) { this.productoId = productoId; }

    public Integer getServicioId() { return servicioId; }
    public void setServicioId(Integer servicioId) { this.servicioId = servicioId; }

    public String getServicioDescripcion() { return servicioDescripcion; }
    public void setServicioDescripcion(String servicioDescripcion) { this.servicioDescripcion = servicioDescripcion; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        recalcularSubtotal();
    }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        recalcularSubtotal();
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public String getNombreItem() {
        if (nombreItem != null && !nombreItem.isEmpty()) {
            return nombreItem;
        }
        return servicioDescripcion != null ? servicioDescripcion : "Item #" + (productoId != null ? productoId : servicioId);
    }
    public void setNombreItem(String nombreItem) { this.nombreItem = nombreItem; }

    private void recalcularSubtotal() {
        if (precioUnitario != null && cantidad > 0) {
            this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
        }
    }

    @Override
    public String toString() {
        return String.format("ItemFactura[id=%d, tipo=%s, nombre='%s', cant=%d, unitario=%.2f, subtotal=%.2f]",
                id, tipoItem, getNombreItem(), cantidad, precioUnitario, subtotal);
    }
}
