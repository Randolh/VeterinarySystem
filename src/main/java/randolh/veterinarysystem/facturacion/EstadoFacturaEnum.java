package randolh.veterinarysystem.facturacion;

// Estados para el ciclo contable y fiscal de las facturas
public enum EstadoFacturaEnum {
    PENDIENTE("Pendiente"),
    PAGADA("Pagada"),
    ANULADA("Anulada");

    private final String valorDb;

    EstadoFacturaEnum(String valorDb) {
        this.valorDb = valorDb;
    }

    public String getValorDb() {
        return valorDb;
    }

    public String getValor() {
        return valorDb;
    }

    public String getDescripcion() {
        return valorDb;
    }

    public static EstadoFacturaEnum desdeString(String texto) {
        if (texto == null) return PENDIENTE;
        for (EstadoFacturaEnum e : values()) {
            if (e.valorDb.equalsIgnoreCase(texto.trim()) || e.name().equalsIgnoreCase(texto.trim())) {
                return e;
            }
        }
        return PENDIENTE;
    }

    public static EstadoFacturaEnum desdeTexto(String texto) {
        return desdeString(texto);
    }
}
