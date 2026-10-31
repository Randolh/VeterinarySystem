package randolh.veterinarysystem.alerta;

// Tipos de alerta automatica del sistema
public enum TipoAlertaEnum {
    STOCK_MINIMO("STOCK_MINIMO", "Stock Minimo"),
    POR_VENCER("POR_VENCER", "Proximo a Vencer"),
    VENCIDO("VENCIDO", "Producto Vencido"),
    CITA_PROXIMA("CITA_PROXIMA", "Cita Proxima");

    private final String valorDb;
    private final String descripcion;

    TipoAlertaEnum(String valorDb, String descripcion) {
        this.valorDb = valorDb;
        this.descripcion = descripcion;
    }

    public String getValorDb() {
        return valorDb;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static TipoAlertaEnum desdeString(String texto) {
        if (texto == null) return STOCK_MINIMO;
        for (TipoAlertaEnum t : values()) {
            if (t.valorDb.equalsIgnoreCase(texto.trim()) || t.name().equalsIgnoreCase(texto.trim())) {
                return t;
            }
        }
        return STOCK_MINIMO;
    }
}
