package randolh.veterinarysystem.core.patterns.observer;

// Interfaz para componentes suscriptores en el patron Observer
public interface Observador {
    void actualizar(String tipoEvento, Object datos);
}
