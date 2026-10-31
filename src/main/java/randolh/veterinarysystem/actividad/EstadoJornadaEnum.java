package randolh.veterinarysystem.actividad;

// Estados del ciclo operativo de las jornadas de vacunacion comunitaria
public enum EstadoJornadaEnum {
    PLANIFICADA("Planificada"),
    EN_CURSO("En Curso"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada");

    private final String valorDb;

    EstadoJornadaEnum(String valorDb) {
        this.valorDb = valorDb;
    }

    public String getValorDb() {
        return valorDb;
    }

    public String getValor() {
        return valorDb;
    }

    public static EstadoJornadaEnum desdeString(String texto) {
        if (texto == null) return PLANIFICADA;
        for (EstadoJornadaEnum e : values()) {
            if (e.valorDb.equalsIgnoreCase(texto.trim()) || e.name().equalsIgnoreCase(texto.trim())) {
                return e;
            }
        }
        return PLANIFICADA;
    }

    public static EstadoJornadaEnum desdeTexto(String texto) {
        return desdeString(texto);
    }
}
