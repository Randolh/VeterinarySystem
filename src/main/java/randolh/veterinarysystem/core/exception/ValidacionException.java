package randolh.veterinarysystem.core.exception;

// Excepcion no comprobada para infracciones de reglas y validaciones de negocio
public class ValidacionException extends RuntimeException {

    public ValidacionException(String message) {
        super(message);
    }

    public ValidacionException(String message, Throwable cause) {
        super(message, cause);
    }
}
