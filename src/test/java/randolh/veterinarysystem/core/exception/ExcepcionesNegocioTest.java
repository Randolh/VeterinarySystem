package randolh.veterinarysystem.core.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Pruebas unitarias para excepciones de negocio personalizadas
public class ExcepcionesNegocioTest {

    @Test
    public void testValidacionException() {
        ValidacionException ex = new ValidacionException("El nombre es obligatorio");
        assertEquals("El nombre es obligatorio", ex.getMessage());
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    public void testInventarioException() {
        InventarioException ex = new InventarioException("Stock insuficiente");
        assertEquals("Stock insuficiente", ex.getMessage());
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    public void testBaseDatosException() {
        Exception causa = new Exception("Fallo de conexion");
        BaseDatosException ex = new BaseDatosException("Error en base de datos", causa);
        assertEquals("Error en base de datos", ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("Fallo de conexion", ex.getCause().getMessage());
        assertTrue(ex instanceof RuntimeException);
    }
}
