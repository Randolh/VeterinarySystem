package randolh.veterinarysystem.facturacion;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// Pruebas unitarias para calculos de ItemFactura
public class ItemFacturaCalculosTest {

    @Test
    public void testItemParaProductoCalculoSubtotal() {
        ItemFactura item = ItemFactura.paraProducto(1, "Vacuna Rabia", 3, BigDecimal.valueOf(25.50));

        assertNotNull(item);
        assertEquals(TipoItemEnum.PRODUCTO, item.getTipoItem());
        assertEquals(3, item.getCantidad());
        assertEquals(BigDecimal.valueOf(25.50), item.getPrecioUnitario());
        assertEquals(BigDecimal.valueOf(76.50).setScale(2), item.getSubtotal());
    }

    @Test
    public void testItemParaServicioCalculoSubtotal() {
        ItemFactura item = ItemFactura.paraServicio(2, "Consulta General", 1, BigDecimal.valueOf(45.00));

        assertNotNull(item);
        assertEquals(TipoItemEnum.SERVICIO, item.getTipoItem());
        assertEquals(1, item.getCantidad());
        assertEquals(BigDecimal.valueOf(45.00).setScale(2), item.getSubtotal());
    }

    @Test
    public void testRecalculoSubtotalAlModificarCantidad() {
        ItemFactura item = ItemFactura.paraProducto(1, "Jeringas", 2, BigDecimal.valueOf(1.50));
        assertEquals(BigDecimal.valueOf(3.00).setScale(2), item.getSubtotal());

        item.setCantidad(5);
        assertEquals(BigDecimal.valueOf(7.50).setScale(2), item.getSubtotal());
    }

    @Test
    public void testRecalculoSubtotalAlModificarPrecio() {
        ItemFactura item = ItemFactura.paraProducto(1, "Gasa Esteril", 4, BigDecimal.valueOf(2.00));
        assertEquals(BigDecimal.valueOf(8.00).setScale(2), item.getSubtotal());

        item.setPrecioUnitario(BigDecimal.valueOf(2.50));
        assertEquals(BigDecimal.valueOf(10.00).setScale(2), item.getSubtotal());
    }
}
