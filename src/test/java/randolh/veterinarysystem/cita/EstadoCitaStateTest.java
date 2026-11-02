package randolh.veterinarysystem.cita;

import org.junit.jupiter.api.Test;
import randolh.veterinarysystem.core.exception.ValidacionException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Pruebas unitarias para patron State del ciclo de vida de Citas
public class EstadoCitaStateTest {

    @Test
    public void testTransicionExitosaCicloCompleto() {
        Cita cita = new Cita(1, 2, LocalDateTime.now().plusDays(1), "Vacunacion general", "Notas");
        assertEquals(EstadoCitaEnum.PROGRAMADA, cita.getEstado());

        // 1. Programada -> Confirmada
        cita.confirmar();
        assertEquals(EstadoCitaEnum.CONFIRMADA, cita.getEstado());

        // 2. Confirmada -> En Curso
        cita.iniciarAtencion();
        assertEquals(EstadoCitaEnum.EN_CURSO, cita.getEstado());

        // 3. En Curso -> Finalizada
        cita.finalizar();
        assertEquals(EstadoCitaEnum.FINALIZADA, cita.getEstado());
    }

    @Test
    public void testCancelarDesdeProgramada() {
        Cita cita = new Cita(1, 2, LocalDateTime.now().plusDays(2), "Chequeo rutinario", "Notas");
        cita.cancelar("El dueno cancela por viaje");

        assertEquals(EstadoCitaEnum.CANCELADA, cita.getEstado());
        assertEquals("El dueno cancela por viaje", cita.getMotivoCancelacion());
        assertNotNull(cita.getFechaCancelacion());
    }

    @Test
    public void testCancelarDesdeConfirmada() {
        Cita cita = new Cita(1, 2, LocalDateTime.now().plusDays(2), "Revision", "Notas");
        cita.confirmar();
        cita.cancelar("Emergencia medica");

        assertEquals(EstadoCitaEnum.CANCELADA, cita.getEstado());
    }

    @Test
    public void testFinalizarSinIniciarLanzaExcepcion() {
        Cita cita = new Cita(1, 2, LocalDateTime.now().plusDays(1), "Consulta", "Notas");

        assertThrows(ValidacionException.class, cita::finalizar);
    }

    @Test
    public void testCancelarEnCursoLanzaExcepcion() {
        Cita cita = new Cita(1, 2, LocalDateTime.now().plusDays(1), "Cirugia menor", "Notas");
        cita.iniciarAtencion();

        assertThrows(ValidacionException.class, () -> cita.cancelar("Intento de cancelacion"));
    }

    @Test
    public void testModificarFinalizadaLanzaExcepcion() {
        Cita cita = new Cita(1, 2, LocalDateTime.now().plusDays(1), "Consulta", "Notas");
        cita.iniciarAtencion();
        cita.finalizar();

        assertThrows(ValidacionException.class, cita::confirmar);
        assertThrows(ValidacionException.class, cita::iniciarAtencion);
        assertThrows(ValidacionException.class, () -> cita.cancelar("Cancelar"));
    }

    @Test
    public void testMarcarNoAsistio() {
        Cita cita = new Cita(1, 2, LocalDateTime.now().plusDays(1), "Control", "Notas");
        cita.marcarNoAsistio();

        assertEquals(EstadoCitaEnum.NO_ASISTIO, cita.getEstado());
        assertThrows(ValidacionException.class, cita::iniciarAtencion);
    }
}
