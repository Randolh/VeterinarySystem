package randolh.veterinarysystem.consulta;

// Estados para el ciclo de vida de procedimientos quirurgicos y especiales
public enum EstadoProcedimientoEnum {
    PROGRAMADO("Programado"),
    EN_PROCESO("En Proceso"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado");

    private final String valorDb;

    EstadoProcedimientoEnum(String valorDb) {
        this.valorDb = valorDb;
    }

    public String getValorDb() {
        return valorDb;
    }

    public String getDescripcion() {
        return valorDb;
    }

    public static EstadoProcedimientoEnum desdeString(String texto) {
        if (texto == null) return PROGRAMADO;
        for (EstadoProcedimientoEnum e : values()) {
            if (e.valorDb.equalsIgnoreCase(texto.trim()) || e.name().equalsIgnoreCase(texto.trim())) {
                return e;
            }
        }
        return PROGRAMADO;
    }
}
