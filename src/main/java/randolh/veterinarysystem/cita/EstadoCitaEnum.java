package randolh.veterinarysystem.cita;

// Enumeracion de estados para el ciclo de vida de citas
public enum EstadoCitaEnum {
    PROGRAMADA("Programada"),
    CONFIRMADA("Confirmada"),
    EN_CURSO("En Curso"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada"),
    NO_ASISTIO("No Asistio");

    private final String valorDb;

    EstadoCitaEnum(String valorDb) {
        this.valorDb = valorDb;
    }

    public String getValorDb() {
        return valorDb;
    }

    public String getDescripcion() {
        return valorDb;
    }

    public static EstadoCitaEnum desdeString(String texto) {
        if (texto == null) return PROGRAMADA;
        for (EstadoCitaEnum e : values()) {
            if (e.valorDb.equalsIgnoreCase(texto.trim()) || e.name().equalsIgnoreCase(texto.trim())) {
                return e;
            }
        }
        return PROGRAMADA;
    }
}
