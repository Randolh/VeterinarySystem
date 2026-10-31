package randolh.veterinarysystem.core.patterns.strategy;

import randolh.veterinarysystem.facturacion.ItemFactura;

import java.util.List;

// Estrategia de descuento del 10% y puntos para nivel Oro del Club de Mascotas
public class DescuentoOro implements EstrategiaDescuento {

    @Override
    public double aplicarDescuento(double subtotal, List<ItemFactura> items) {
        return Math.round(subtotal * 0.10 * 100.0) / 100.0;
    }

    @Override
    public int calcularPuntosGanados(double totalPagado) {
        return (int) (totalPagado / 20.0);
    }

    @Override
    public String obtenerNombreNivel() {
        return "Oro";
    }

    @Override
    public double obtenerPorcentajeDescuento() {
        return 10.0;
    }
}
