package randolh.veterinarysystem.facturacion;

// Discriminador de tipos de linea en el detalle de la factura
public enum TipoItemEnum {
    PRODUCTO("Producto"),
    SERVICIO("Servicio");

    private final String valorDb;

    TipoItemEnum(String valorDb) {
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

    public static TipoItemEnum desdeString(String texto) {
        if (texto == null) return PRODUCTO;
        for (TipoItemEnum t : values()) {
            if (t.valorDb.equalsIgnoreCase(texto.trim()) || t.name().equalsIgnoreCase(texto.trim())) {
                return t;
            }
        }
        return PRODUCTO;
    }

    public static TipoItemEnum desdeTexto(String texto) {
        return desdeString(texto);
    }
}
