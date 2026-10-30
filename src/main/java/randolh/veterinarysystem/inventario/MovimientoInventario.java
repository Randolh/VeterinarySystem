package randolh.veterinarysystem.inventario;

import java.time.LocalDateTime;

// Modelo entidad para el registro historico de movimientos de kardex
public class MovimientoInventario {

    private Integer id;
    private int productoId;
    private TipoMovimientoEnum tipoMovimiento;
    private int cantidad;
    private int stockAnterior;
    private int stockNuevo;
    private String motivo;
    private Integer referenciaConsultaId;
    private Integer referenciaProcedimientoId;
    private String usuario;
    private LocalDateTime fechaMovimiento;

    // Campo auxiliar para visualizacion
    private String nombreProducto;

    public MovimientoInventario() {
        this.tipoMovimiento = TipoMovimientoEnum.AJUSTE;
        this.fechaMovimiento = LocalDateTime.now();
        this.usuario = "Sistema";
    }

    public MovimientoInventario(Integer id, int productoId, TipoMovimientoEnum tipoMovimiento,
                                int cantidad, int stockAnterior, int stockNuevo, String motivo,
                                Integer referenciaConsultaId, Integer referenciaProcedimientoId,
                                String usuario, LocalDateTime fechaMovimiento) {
        this.id = id;
        this.productoId = productoId;
        this.tipoMovimiento = tipoMovimiento != null ? tipoMovimiento : TipoMovimientoEnum.AJUSTE;
        this.cantidad = cantidad;
        this.stockAnterior = stockAnterior;
        this.stockNuevo = stockNuevo;
        this.motivo = motivo;
        this.referenciaConsultaId = referenciaConsultaId;
        this.referenciaProcedimientoId = referenciaProcedimientoId;
        this.usuario = usuario != null ? usuario : "Sistema";
        this.fechaMovimiento = fechaMovimiento != null ? fechaMovimiento : LocalDateTime.now();
    }

    public MovimientoInventario(int productoId, TipoMovimientoEnum tipoMovimiento,
                                int cantidad, int stockAnterior, int stockNuevo,
                                String motivo, String usuario) {
        this(null, productoId, tipoMovimiento, cantidad, stockAnterior, stockNuevo,
                motivo, null, null, usuario, LocalDateTime.now());
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public TipoMovimientoEnum getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(TipoMovimientoEnum tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public int getStockAnterior() { return stockAnterior; }
    public void setStockAnterior(int stockAnterior) { this.stockAnterior = stockAnterior; }

    public int getStockNuevo() { return stockNuevo; }
    public void setStockNuevo(int stockNuevo) { this.stockNuevo = stockNuevo; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Integer getReferenciaConsultaId() { return referenciaConsultaId; }
    public void setReferenciaConsultaId(Integer referenciaConsultaId) { this.referenciaConsultaId = referenciaConsultaId; }

    public Integer getReferenciaProcedimientoId() { return referenciaProcedimientoId; }
    public void setReferenciaProcedimientoId(Integer referenciaProcedimientoId) { this.referenciaProcedimientoId = referenciaProcedimientoId; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public LocalDateTime getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDateTime fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    @Override
    public String toString() {
        return String.format("Movimiento[id=%d, productoId=%d, tipo=%s, cant=%d, anterior=%d, nuevo=%d, fecha=%s]",
                id, productoId, tipoMovimiento, cantidad, stockAnterior, stockNuevo, fechaMovimiento);
    }
}
