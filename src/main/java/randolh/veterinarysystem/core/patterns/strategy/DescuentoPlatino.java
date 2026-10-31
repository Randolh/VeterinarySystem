package randolh.veterinarysystem.core.patterns.strategy;

import randolh.veterinarysystem.facturacion.ItemFactura;

import java.util.List;

// Estrategia de descuento del 15% y puntos para nivel Platino del Club de Mascotas
public class DescuentoPlatino implements EstrategiaDescuento {

    @Override
    public double aplicarDescuento(double subtotal, List<ItemFactura> items) {
        return Math.round(subtotal * 0.15 * 100.0) / 100.0;
    }

    @Override
    public int calcularPuntosGanados(double totalPagado) {
        return (int) (totalPagado / 10.0);
    }

    @Override
    public String obtenerNombreNivel() {
        return "Platino";
    }

    @Override
    public double obtenerPorcentajeDescuento() {
        return 15.0;
    }
}
