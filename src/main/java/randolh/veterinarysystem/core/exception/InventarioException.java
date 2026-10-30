package randolh.veterinarysystem.core.exception;

// Excepcion no comprobada para stock insuficiente o insumos caducados
public class InventarioException extends RuntimeException {

    public InventarioException(String message) {
        super(message);
    }

    public InventarioException(String message, Throwable cause) {
        super(message, cause);
    }
}
