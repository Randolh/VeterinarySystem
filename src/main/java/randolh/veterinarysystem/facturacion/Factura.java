package randolh.veterinarysystem.facturacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Modelo entidad cabecera de facturas y comprobantes fiscales
public class Factura {

    private Integer id;
    private int duenoId;
    private String numeroFactura;
    private LocalDateTime fechaEmision;
    private BigDecimal subtotal;
    private BigDecimal impuesto;
    private BigDecimal descuento;
    private BigDecimal total;
    private MetodoPagoEnum metodoPago;
    private EstadoFacturaEnum estado;
    private String observaciones;

    // Lista de conceptos o items de la factura
    private List<ItemFactura> items;

    // Campos auxiliares para impresion y visualizacion
    private String nombreDueno;
    private String documentoDueno;
    private String emailDueno;
    private String telefonoDueno;
    private String direccionDueno;
    private String nivelClub;
    private int puntosGanados;

    public Factura() {
        this.fechaEmision = LocalDateTime.now();
        this.subtotal = BigDecimal.ZERO;
        this.impuesto = BigDecimal.ZERO;
        this.descuento = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.metodoPago = MetodoPagoEnum.EFECTIVO;
        this.estado = EstadoFacturaEnum.PAGADA;
        this.items = new ArrayList<>();
    }

    public Factura(Integer id, int duenoId, String numeroFactura, LocalDateTime fechaEmision,
                   BigDecimal subtotal, BigDecimal impuesto, BigDecimal descuento,
                   BigDecimal total, MetodoPagoEnum metodoPago, EstadoFacturaEnum estado,
                   String observaciones) {
        this.id = id;
        this.duenoId = duenoId;
        this.numeroFactura = numeroFactura;
        this.fechaEmision = fechaEmision != null ? fechaEmision : LocalDateTime.now();
        this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        this.impuesto = impuesto != null ? impuesto : BigDecimal.ZERO;
        this.descuento = descuento != null ? descuento : BigDecimal.ZERO;
        this.total = total != null ? total : BigDecimal.ZERO;
        this.metodoPago = metodoPago != null ? metodoPago : MetodoPagoEnum.EFECTIVO;
        this.estado = estado != null ? estado : EstadoFacturaEnum.PAGADA;
        this.observaciones = observaciones;
        this.items = new ArrayList<>();
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getDuenoId() { return duenoId; }
    public void setDuenoId(int duenoId) { this.duenoId = duenoId; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getImpuesto() { return impuesto; }
    public void setImpuesto(BigDecimal impuesto) { this.impuesto = impuesto; }

    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public MetodoPagoEnum getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPagoEnum metodoPago) { this.metodoPago = metodoPago; }

    public EstadoFacturaEnum getEstado() { return estado; }
    public void setEstado(EstadoFacturaEnum estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public List<ItemFactura> getItems() { return items; }
    public void setItems(List<ItemFactura> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void agregarItem(ItemFactura item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }

    public String getNombreDueno() { return nombreDueno; }
    public void setNombreDueno(String nombreDueno) { this.nombreDueno = nombreDueno; }

    public String getDocumentoDueno() { return documentoDueno; }
    public void setDocumentoDueno(String documentoDueno) { this.documentoDueno = documentoDueno; }

    public String getEmailDueno() { return emailDueno; }
    public void setEmailDueno(String emailDueno) { this.emailDueno = emailDueno; }

    public String getTelefonoDueno() { return telefonoDueno; }
    public void setTelefonoDueno(String telefonoDueno) { this.telefonoDueno = telefonoDueno; }

    public String getDireccionDueno() { return direccionDueno; }
    public void setDireccionDueno(String direccionDueno) { this.direccionDueno = direccionDueno; }

    public String getNivelClub() { return nivelClub; }
    public void setNivelClub(String nivelClub) { this.nivelClub = nivelClub; }

    public int getPuntosGanados() { return puntosGanados; }
    public void setPuntosGanados(int puntosGanados) { this.puntosGanados = puntosGanados; }

    @Override
    public String toString() {
        return String.format("Factura[num='%s', cliente='%s', total=%.2f, estado=%s, pago=%s]",
                numeroFactura, nombreDueno, total, estado, metodoPago);
    }
}
