package randolh.veterinarysystem.mascota;

// Enumeracion para el sexo de la mascota segun MySQL ENUM
public enum SexoEnum {
    MACHO("Macho"),
    HEMBRA("Hembra");

    private final String valorDb;

    SexoEnum(String valorDb) {
        this.valorDb = valorDb;
    }

    public String getValorDb() {
        return valorDb;
    }

    public static SexoEnum desdeString(String texto) {
        if (texto == null) return MACHO;
        for (SexoEnum s : values()) {
            if (s.valorDb.equalsIgnoreCase(texto.trim()) || s.name().equalsIgnoreCase(texto.trim())) {
                return s;
            }
        }
        return MACHO;
    }
}
