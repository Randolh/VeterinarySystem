package randolh.veterinarysystem.inventario;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Pruebas unitarias para reglas de negocio de Inventario y Caducidades
public class ProductoInventarioTest {

    @Test
    public void testIsBajoStockMinimo() {
        Producto p = new Producto();
        p.setStockMinimo(10);

        // Stock superior al minimo
        p.setCantidadStock(15);
        assertFalse(p.isBajoStockMinimo());

        // Stock igual al minimo
        p.setCantidadStock(10);
        assertTrue(p.isBajoStockMinimo());

        // Stock inferior al minimo
        p.setCantidadStock(5);
        assertTrue(p.isBajoStockMinimo());

        // Stock en cero
        p.setCantidadStock(0);
        assertTrue(p.isBajoStockMinimo());
    }

    @Test
    public void testIsVencido() {
        Producto p = new Producto();

        // Sin fecha de vencimiento
        p.setFechaVencimiento(null);
        assertFalse(p.isVencido());

        // Vencido ayer
        p.setFechaVencimiento(LocalDate.now().minusDays(1));
        assertTrue(p.isVencido());

        // Vence hoy
        p.setFechaVencimiento(LocalDate.now());
        assertFalse(p.isVencido());

        // Vigente en el futuro
        p.setFechaVencimiento(LocalDate.now().plusMonths(6));
        assertFalse(p.isVencido());
    }

    @Test
    public void testIsProximoAVencer() {
        Producto p = new Producto();

        // Vence en 15 dias (dentro de ventana de 30 dias)
        p.setFechaVencimiento(LocalDate.now().plusDays(15));
        assertTrue(p.isProximoAVencer(30));

        // Vence en 45 dias (fuera de ventana de 30 dias)
        p.setFechaVencimiento(LocalDate.now().plusDays(45));
        assertFalse(p.isProximoAVencer(30));

        // Ya vencido no cuenta como proximo a vencer
        p.setFechaVencimiento(LocalDate.now().minusDays(5));
        assertFalse(p.isProximoAVencer(30));
    }
}
