package randolh.veterinarysystem.core.patterns.observer;

import randolh.veterinarysystem.core.utils.LoggerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// Sujeto emisor y gestor de eventos para el patron Observer
public class GestorEventos {

    private static final String MODULO = "GestorEventos";
    private static final GestorEventos INSTANCIA = new GestorEventos();

    private final Map<String, List<Observador>> suscriptores = new ConcurrentHashMap<>();

    public GestorEventos() {}

    public static GestorEventos getInstancia() {
        return INSTANCIA;
    }

    // Registra un observador para un tipo de evento especifico
    public void suscribir(String tipoEvento, Observador observador) {
        if (tipoEvento == null || observador == null) return;
        suscriptores.computeIfAbsent(tipoEvento, k -> new CopyOnWriteArrayList<>()).add(observador);
        LoggerManager.info(MODULO, "Observador suscrito al evento: " + tipoEvento);
    }

    // Desuscribe un observador
    public void desuscribir(String tipoEvento, Observador observador) {
        if (tipoEvento == null || observador == null) return;
        List<Observador> lista = suscriptores.get(tipoEvento);
        if (lista != null) {
            lista.remove(observador);
        }
    }

    // Emite una notificacion a todos los observadores registrados para el evento
    public void notificar(String tipoEvento, Object datos) {
        if (tipoEvento == null) return;
        List<Observador> lista = suscriptores.get(tipoEvento);
        if (lista != null && !lista.isEmpty()) {
            for (Observador observador : lista) {
                try {
                    observador.actualizar(tipoEvento, datos);
                } catch (Exception e) {
                    LoggerManager.error(MODULO, "Fallo al notificar observador para evento " + tipoEvento, e);
                }
            }
        }
    }

    // Emite una notificacion a todos los tipos de eventos comodin o globales
    public void notificarGlobal(String tipoEvento, Object datos) {
        notificar(tipoEvento, datos);
        notificar("*", datos);
    }
}
