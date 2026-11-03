package randolh.veterinarysystem.core.exception;

// Excepcion no comprobada lanzada cuando el usuario cancela o aborta un formulario interactivo
public class OperacionCanceladaException extends RuntimeException {

    public OperacionCanceladaException() {
        super("Operacion cancelada por el usuario.");
    }

    public OperacionCanceladaException(String message) {
        super(message);
    }
}

