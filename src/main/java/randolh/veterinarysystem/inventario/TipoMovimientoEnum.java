package randolh.veterinarysystem.inventario;

// Enumeracion de tipos de movimiento para el kardex de inventario
public enum TipoMovimientoEnum {
    ENTRADA("Entrada"),
    SALIDA("Salida"),
    AJUSTE("Ajuste"),
    VENCIMIENTO("Vencimiento");

    private final String valorDb;

    TipoMovimientoEnum(String valorDb) {
        this.valorDb = valorDb;
    }

    public String getValorDb() {
        return valorDb;
    }

    public String getDescripcion() {
        return valorDb;
    }

    public static TipoMovimientoEnum desdeString(String texto) {
        if (texto == null) return AJUSTE;
        for (TipoMovimientoEnum t : values()) {
            if (t.valorDb.equalsIgnoreCase(texto.trim()) || t.name().equalsIgnoreCase(texto.trim())) {
                return t;
            }
        }
        return AJUSTE;
    }
}
