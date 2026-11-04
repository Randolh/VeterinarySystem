package randolh.veterinarysystem.inventario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Modelo entidad para articulos, medicamentos e insumos de farmacia y almacen
public class Producto {

    private Integer id;
    private String nombreProducto;
    private int productoTipoId;
    private String descripcion;
    private String fabricante;
    private Integer proveedorId;
    private String lote;
    private int cantidadStock;
    private int stockMinimo;
    private String unidadMedida;
    private LocalDate fechaVencimiento;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private boolean requiereReceta;
    private boolean activo;
    private LocalDateTime fechaRegistro;

    // Campos auxiliares para visualizacion
    private String tipoNombre;
    private String proveedorNombre;

    public Producto() {
        this.unidadMedida = "unidad";
        this.requiereReceta = false;
        this.activo = true;
        this.fechaRegistro = LocalDateTime.now();
    }

    public Producto(Integer id, String nombreProducto, int productoTipoId, String descripcion,
                    String fabricante, Integer proveedorId, String lote, int cantidadStock,
                    int stockMinimo, String unidadMedida, LocalDate fechaVencimiento,
                    BigDecimal precioCompra, BigDecimal precioVenta, boolean requiereReceta,
                    boolean activo, LocalDateTime fechaRegistro) {
        this.id = id;
        this.nombreProducto = nombreProducto;
        this.productoTipoId = productoTipoId;
        this.descripcion = descripcion;
        this.fabricante = fabricante;
        this.proveedorId = proveedorId;
        this.lote = lote;
        this.cantidadStock = cantidadStock;
        this.stockMinimo = stockMinimo;
        this.unidadMedida = unidadMedida != null ? unidadMedida : "unidad";
        this.fechaVencimiento = fechaVencimiento;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta != null ? precioVenta : BigDecimal.ZERO;
        this.requiereReceta = requiereReceta;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro != null ? fechaRegistro : LocalDateTime.now();
    }

    // Metodos auxiliares de diagnostico de stock y caducidad
    public boolean isBajoStockMinimo() {
        return cantidadStock <= stockMinimo;
    }

    public boolean isVencido() {
        return fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now());
    }

    public boolean isProximoAVencer(int dias) {
        if (fechaVencimiento == null || isVencido()) {
            return false;
        }
        return fechaVencimiento.isBefore(LocalDate.now().plusDays(dias));
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getProductoTipoId() { return productoTipoId; }
    public void setProductoTipoId(int productoTipoId) { this.productoTipoId = productoTipoId; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getFabricante() { return fabricante; }
    public void setFabricante(String fabricante) { this.fabricante = fabricante; }

    public Integer getProveedorId() { return proveedorId; }
    public void setProveedorId(Integer proveedorId) { this.proveedorId = proveedorId; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public int getCantidadStock() { return cantidadStock; }
    public void setCantidadStock(int cantidadStock) { this.cantidadStock = cantidadStock; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public BigDecimal getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(BigDecimal precioCompra) { this.precioCompra = precioCompra; }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    public boolean isRequiereReceta() { return requiereReceta; }
    public void setRequiereReceta(boolean requiereReceta) { this.requiereReceta = requiereReceta; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getTipoNombre() { return tipoNombre; }
    public void setTipoNombre(String tipoNombre) { this.tipoNombre = tipoNombre; }

    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }

    @Override
    public String toString() {
        return String.format("Producto[id=%d, nombre='%s', stock=%d, min=%d, venc=%s, activo=%b]",
                id, nombreProducto, cantidadStock, stockMinimo, fechaVencimiento, activo);
    }
}
