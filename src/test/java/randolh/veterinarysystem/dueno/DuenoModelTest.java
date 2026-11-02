package randolh.veterinarysystem.dueno;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Pruebas unitarias para entidad Dueno
public class DuenoModelTest {

    @Test
    public void testCreacionDuenoValoresPorDefecto() {
        Dueno dueno = new Dueno();

        assertEquals("CC", dueno.getTipoDocumento());
        assertTrue(dueno.isActivo());
        assertNotNull(dueno.getFechaRegistro());
    }

    @Test
    public void testCreacionDuenoConstructorCompleto() {
        Dueno dueno = new Dueno("CC", "12345678", "Carlos Gomez", "Calle 10 #20",
                "3001234567", "carlos@example.com", "Maria Gomez - 3009876543");

        assertEquals("CC", dueno.getTipoDocumento());
        assertEquals("12345678", dueno.getNumeroDocumento());
        assertEquals("Carlos Gomez", dueno.getNombreCompleto());
        assertEquals("Calle 10 #20", dueno.getDireccion());
        assertEquals("3001234567", dueno.getTelefono());
        assertEquals("carlos@example.com", dueno.getEmail());
        assertEquals("Maria Gomez - 3009876543", dueno.getContactoEmergencia());
        assertTrue(dueno.isActivo());
    }
}
