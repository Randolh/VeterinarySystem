package randolh.veterinarysystem.actividad;

// Estados del proceso de adopcion de animales rescatados
public enum EstadoAdopcionEnum {
    DISPONIBLE("Disponible"),
    EN_PROCESO("En Proceso"),
    ADOPTADA("Adoptada"),
    RETIRADA("Retirada");

    private final String valorDb;

    EstadoAdopcionEnum(String valorDb) {
        this.valorDb = valorDb;
    }

    public String getValorDb() {
        return valorDb;
    }

    public String getValor() {
        return valorDb;
    }

    public static EstadoAdopcionEnum desdeString(String texto) {
        if (texto == null) return DISPONIBLE;
        for (EstadoAdopcionEnum e : values()) {
            if (e.valorDb.equalsIgnoreCase(texto.trim()) || e.name().equalsIgnoreCase(texto.trim())) {
                return e;
            }
        }
        return DISPONIBLE;
    }

    public static EstadoAdopcionEnum desdeTexto(String texto) {
        return desdeString(texto);
    }
}
