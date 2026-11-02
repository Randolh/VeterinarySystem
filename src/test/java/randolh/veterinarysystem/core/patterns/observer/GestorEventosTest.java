package randolh.veterinarysystem.core.patterns.observer;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Pruebas unitarias para patron Observer
public class GestorEventosTest {

    @Test
    public void testSuscripcionYNotificacion() {
        GestorEventos gestor = new GestorEventos();
        AtomicBoolean notificado = new AtomicBoolean(false);

        Observador obs = (tipo, datos) -> notificado.set(true);

        gestor.suscribir("STOCK_MINIMO", obs);
        gestor.notificar("STOCK_MINIMO", "Producto XYZ");

        assertTrue(notificado.get());
    }

    @Test
    public void testAislamientoDeEventos() {
        GestorEventos gestor = new GestorEventos();
        AtomicInteger contador = new AtomicInteger(0);

        Observador obs = (tipo, datos) -> contador.incrementAndGet();

        gestor.suscribir("EVENTO_A", obs);
        gestor.notificar("EVENTO_B", "Datos");

        assertEquals(0, contador.get());

        gestor.notificar("EVENTO_A", "Datos");
        assertEquals(1, contador.get());
    }

    @Test
    public void testDesuscripcion() {
        GestorEventos gestor = new GestorEventos();
        AtomicInteger contador = new AtomicInteger(0);

        Observador obs = (tipo, datos) -> contador.incrementAndGet();

        gestor.suscribir("ALERTA", obs);
        gestor.notificar("ALERTA", "1");
        assertEquals(1, contador.get());

        gestor.desuscribir("ALERTA", obs);
        gestor.notificar("ALERTA", "2");
        assertEquals(1, contador.get());
    }
}
