package randolh.veterinarysystem.core.patterns.strategy;

// Factoria para resolucion de estrategias de descuento del Club de Mascotas
public final class FactoriaEstrategias {

    private static final EstrategiaDescuento SIN_CLUB = new DescuentoSinClub();
    private static final EstrategiaDescuento BRONCE = new DescuentoBronce();
    private static final EstrategiaDescuento PLATA = new DescuentoPlata();
    private static final EstrategiaDescuento ORO = new DescuentoOro();
    private static final EstrategiaDescuento PLATINO = new DescuentoPlatino();

    private FactoriaEstrategias() {}

    // Resuelve la estrategia adecuada a partir del nivel de membresia
    public static EstrategiaDescuento obtenerEstrategia(String nivelClub) {
        if (nivelClub == null || nivelClub.trim().isEmpty()) {
            return SIN_CLUB;
        }

        return switch (nivelClub.trim().toLowerCase()) {
            case "bronce" -> BRONCE;
            case "plata" -> PLATA;
            case "oro" -> ORO;
            case "platino" -> PLATINO;
            default -> SIN_CLUB;
        };
    }
}
