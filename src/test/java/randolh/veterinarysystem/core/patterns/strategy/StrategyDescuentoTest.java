package randolh.veterinarysystem.core.patterns.strategy;

import org.junit.jupiter.api.Test;
import randolh.veterinarysystem.facturacion.ItemFactura;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Pruebas unitarias para patron Strategy de descuentos del Club
public class StrategyDescuentoTest {

    @Test
    public void testFactoriaEstrategias() {
        EstrategiaDescuento sSinClub = FactoriaEstrategias.obtenerEstrategia(null);
        assertEquals("Sin Club", sSinClub.obtenerNombreNivel());

        EstrategiaDescuento sBronce = FactoriaEstrategias.obtenerEstrategia("bronce");
        assertEquals("Bronce", sBronce.obtenerNombreNivel());

        EstrategiaDescuento sPlata = FactoriaEstrategias.obtenerEstrategia("PLATA");
        assertEquals("Plata", sPlata.obtenerNombreNivel());

        EstrategiaDescuento sOro = FactoriaEstrategias.obtenerEstrategia("Oro");
        assertEquals("Oro", sOro.obtenerNombreNivel());

        EstrategiaDescuento sPlatino = FactoriaEstrategias.obtenerEstrategia("platino");
        assertEquals("Platino", sPlatino.obtenerNombreNivel());

        EstrategiaDescuento sInvalido = FactoriaEstrategias.obtenerEstrategia("desconocido");
        assertEquals("Sin Club", sInvalido.obtenerNombreNivel());
    }

    @Test
    public void testDescuentoSinClub() {
        EstrategiaDescuento estrategia = new DescuentoSinClub();
        List<ItemFactura> items = new ArrayList<>();
        items.add(ItemFactura.paraProducto(1, "Vacuna", 2, BigDecimal.valueOf(50.0)));

        double descuento = estrategia.aplicarDescuento(100.0, items);
        int puntos = estrategia.calcularPuntosGanados(100.0);

        assertEquals(0.0, descuento, 0.001);
        assertEquals(0, puntos);
        assertEquals(0.0, estrategia.obtenerPorcentajeDescuento(), 0.001);
    }

    @Test
    public void testDescuentoBronce() {
        EstrategiaDescuento estrategia = new DescuentoBronce();
        List<ItemFactura> items = new ArrayList<>();
        items.add(ItemFactura.paraProducto(1, "Antipulgas", 1, BigDecimal.valueOf(100.0)));

        double descuento = estrategia.aplicarDescuento(100.0, items);
        int puntos = estrategia.calcularPuntosGanados(100.0);

        assertEquals(2.0, descuento, 0.001);
        assertEquals(2, puntos);
        assertEquals("Bronce", estrategia.obtenerNombreNivel());
        assertEquals(2.0, estrategia.obtenerPorcentajeDescuento(), 0.001);
    }

    @Test
    public void testDescuentoPlata() {
        EstrategiaDescuento estrategia = new DescuentoPlata();
        List<ItemFactura> items = new ArrayList<>();
        items.add(ItemFactura.paraServicio(1, "Consulta", 1, BigDecimal.valueOf(100.0)));

        double descuento = estrategia.aplicarDescuento(100.0, items);
        int puntos = estrategia.calcularPuntosGanados(90.0);

        assertEquals(5.0, descuento, 0.001);
        assertEquals(3, puntos);
        assertEquals(5.0, estrategia.obtenerPorcentajeDescuento(), 0.001);
    }

    @Test
    public void testDescuentoOro() {
        EstrategiaDescuento estrategia = new DescuentoOro();
        List<ItemFactura> items = new ArrayList<>();
        items.add(ItemFactura.paraProducto(1, "Antibiotico", 2, BigDecimal.valueOf(100.0)));

        double descuento = estrategia.aplicarDescuento(200.0, items);
        int puntos = estrategia.calcularPuntosGanados(180.0);

        assertEquals(20.0, descuento, 0.001);
        assertEquals(9, puntos);
        assertEquals(10.0, estrategia.obtenerPorcentajeDescuento(), 0.001);
    }

    @Test
    public void testDescuentoPlatino() {
        EstrategiaDescuento estrategia = new DescuentoPlatino();
        List<ItemFactura> items = new ArrayList<>();
        items.add(ItemFactura.paraServicio(1, "Cirugia", 1, BigDecimal.valueOf(500.0)));

        double descuento = estrategia.aplicarDescuento(500.0, items);
        int puntos = estrategia.calcularPuntosGanados(425.0);

        assertEquals(75.0, descuento, 0.001);
        assertEquals(42, puntos);
        assertEquals(15.0, estrategia.obtenerPorcentajeDescuento(), 0.001);
    }
}
