package randolh.veterinarysystem.alerta;

// Niveles de severidad para priorizacion visual de alertas
public enum NivelSeveridadEnum {
    INFO("INFO", "Informacion"),
    ADVERTENCIA("ADVERTENCIA", "Advertencia"),
    CRITICO("CRITICO", "Critico");

    private final String valorDb;
    private final String descripcion;

    NivelSeveridadEnum(String valorDb, String descripcion) {
        this.valorDb = valorDb;
        this.descripcion = descripcion;
    }

    public String getValorDb() {
        return valorDb;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static NivelSeveridadEnum desdeString(String texto) {
        if (texto == null) return INFO;
        for (NivelSeveridadEnum n : values()) {
            if (n.valorDb.equalsIgnoreCase(texto.trim()) || n.name().equalsIgnoreCase(texto.trim())) {
                return n;
            }
        }
        return INFO;
    }
}
