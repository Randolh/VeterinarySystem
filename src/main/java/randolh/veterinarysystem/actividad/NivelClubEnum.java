package randolh.veterinarysystem.actividad;

// Niveles jerarquicos del programa de fidelizacion Club de Mascotas
public enum NivelClubEnum {
    BRONCE("Bronce", 1),
    PLATA("Plata", 2),
    ORO("Oro", 3),
    PLATINO("Platino", 4);

    private final String valorDb;
    private final int orden;

    NivelClubEnum(String valorDb, int orden) {
        this.valorDb = valorDb;
        this.orden = orden;
    }

    public String getValorDb() {
        return valorDb;
    }

    public String getValor() {
        return valorDb;
    }

    public int getOrden() {
        return orden;
    }

    public boolean esMayorOIgualQue(NivelClubEnum otro) {
        if (otro == null) return true;
        return this.orden >= otro.orden;
    }

    public static NivelClubEnum desdeString(String texto) {
        if (texto == null) return BRONCE;
        for (NivelClubEnum n : values()) {
            if (n.valorDb.equalsIgnoreCase(texto.trim()) || n.name().equalsIgnoreCase(texto.trim())) {
                return n;
            }
        }
        return BRONCE;
    }

    public static NivelClubEnum desdeTexto(String texto) {
        return desdeString(texto);
    }
}
