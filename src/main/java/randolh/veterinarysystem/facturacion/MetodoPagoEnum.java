package randolh.veterinarysystem.facturacion;

// Metodos de pago aceptados para la liquidacion de facturas
public enum MetodoPagoEnum {
    EFECTIVO("Efectivo"),
    TARJETA("Tarjeta"),
    TRANSFERENCIA("Transferencia"),
    MIXTO("Mixto");

    private final String valorDb;

    MetodoPagoEnum(String valorDb) {
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

    public static MetodoPagoEnum desdeString(String texto) {
        if (texto == null) return EFECTIVO;
        for (MetodoPagoEnum m : values()) {
            if (m.valorDb.equalsIgnoreCase(texto.trim()) || m.name().equalsIgnoreCase(texto.trim())) {
                return m;
            }
        }
        return EFECTIVO;
    }

    public static MetodoPagoEnum desdeTexto(String texto) {
        return desdeString(texto);
    }
}
