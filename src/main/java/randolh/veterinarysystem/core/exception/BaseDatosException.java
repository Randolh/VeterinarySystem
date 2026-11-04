package randolh.veterinarysystem.core.exception;

// Excepcion no comprobada para errores de acceso a MySQL
public class BaseDatosException extends RuntimeException {

    public BaseDatosException(String message) {
        super(message);
    }

    public BaseDatosException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseDatosException(Throwable cause) {
        super(cause);
    }
}
