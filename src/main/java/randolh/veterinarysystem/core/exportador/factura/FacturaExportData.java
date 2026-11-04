package randolh.veterinarysystem.core.exportador.factura;

import java.util.Collections;
import java.util.List;

// Modelo inmutable que encapsula los datos requeridos para exportar una factura
public class FacturaExportData {

    private final String clinicaNombre;
    private final String clinicaNit;
    private final String clinicaTel;
    private final String clinicaDir;
    private final String numeroFactura;
    private final String fechaEmision;
    private final String clienteNombre;
    private final String clienteDoc;
    private final String clienteEmail;
    private final String clienteTel;
    private final String nivelClub;
    private final int puntosGanados;
    private final List<String[]> items;
    private final double subtotal;
    private final double descuento;
    private final double impuesto;
    private final double total;
    private final String metodoPago;
    private final String estado;

    public FacturaExportData(Builder builder) {
        this.clinicaNombre = builder.clinicaNombre;
        this.clinicaNit = builder.clinicaNit;
        this.clinicaTel = builder.clinicaTel;
        this.clinicaDir = builder.clinicaDir;
        this.numeroFactura = builder.numeroFactura;
        this.fechaEmision = builder.fechaEmision;
        this.clienteNombre = builder.clienteNombre;
        this.clienteDoc = builder.clienteDoc;
        this.clienteEmail = builder.clienteEmail;
        this.clienteTel = builder.clienteTel;
        this.nivelClub = builder.nivelClub;
        this.puntosGanados = builder.puntosGanados;
        this.items = builder.items != null ? List.copyOf(builder.items) : Collections.emptyList();
        this.subtotal = builder.subtotal;
        this.descuento = builder.descuento;
        this.impuesto = builder.impuesto;
        this.total = builder.total;
        this.metodoPago = builder.metodoPago;
        this.estado = builder.estado;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getClinicaNombre() { return clinicaNombre; }
    public String getClinicaNit() { return clinicaNit; }
    public String getClinicaTel() { return clinicaTel; }
    public String getClinicaDir() { return clinicaDir; }
    public String getNumeroFactura() { return numeroFactura; }
    public String getFechaEmision() { return fechaEmision; }
    public String getClienteNombre() { return clienteNombre; }
    public String getClienteDoc() { return clienteDoc; }
    public String getClienteEmail() { return clienteEmail; }
    public String getClienteTel() { return clienteTel; }
    public String getNivelClub() { return nivelClub; }
    public int getPuntosGanados() { return puntosGanados; }
    public List<String[]> getItems() { return items; }
    public double getSubtotal() { return subtotal; }
    public double getDescuento() { return descuento; }
    public double getImpuesto() { return impuesto; }
    public double getTotal() { return total; }
    public String getMetodoPago() { return metodoPago; }
    public String getEstado() { return estado; }

    public static class Builder {
        private String clinicaNombre = "";
        private String clinicaNit = "";
        private String clinicaTel = "";
        private String clinicaDir = "";
        private String numeroFactura = "";
        private String fechaEmision = "";
        private String clienteNombre = "";
        private String clienteDoc = "";
        private String clienteEmail = "";
        private String clienteTel = "";
        private String nivelClub = "Sin Club";
        private int puntosGanados = 0;
        private List<String[]> items;
        private double subtotal = 0.0;
        private double descuento = 0.0;
        private double impuesto = 0.0;
        private double total = 0.0;
        private String metodoPago = "EFECTIVO";
        private String estado = "PAGADA";

        public Builder conClinica(String nombre, String nit, String tel, String dir) {
            this.clinicaNombre = nombre;
            this.clinicaNit = nit;
            this.clinicaTel = tel;
            this.clinicaDir = dir;
            return this;
        }

        public Builder conFactura(String numero, String fechaEmision) {
            this.numeroFactura = numero;
            this.fechaEmision = fechaEmision;
            return this;
        }

        public Builder conCliente(String nombre, String doc, String email, String tel) {
            this.clienteNombre = nombre;
            this.clienteDoc = doc;
            this.clienteEmail = email;
            this.clienteTel = tel;
            return this;
        }

        public Builder conClub(String nivelClub, int puntosGanados) {
            this.nivelClub = nivelClub;
            this.puntosGanados = puntosGanados;
            return this;
        }

        public Builder conItems(List<String[]> items) {
            this.items = items;
            return this;
        }

        public Builder conMontos(double subtotal, double descuento, double impuesto, double total) {
            this.subtotal = subtotal;
            this.descuento = descuento;
            this.impuesto = impuesto;
            this.total = total;
            return this;
        }

        public Builder conPago(String metodoPago, String estado) {
            this.metodoPago = metodoPago;
            this.estado = estado;
            return this;
        }

        public FacturaExportData build() {
            return new FacturaExportData(this);
        }
    }
}

