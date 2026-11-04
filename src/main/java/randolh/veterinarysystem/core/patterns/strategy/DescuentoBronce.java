package randolh.veterinarysystem.core.patterns.strategy;

import randolh.veterinarysystem.facturacion.ItemFactura;

import java.util.List;

// Estrategia de descuento y puntos para nivel Bronce del Club de Mascotas
public class DescuentoBronce implements EstrategiaDescuento {

    @Override
    public double aplicarDescuento(double subtotal, List<ItemFactura> items) {
        return Math.round(subtotal * 0.02 * 100.0) / 100.0;
    }

    @Override
    public int calcularPuntosGanados(double totalPagado) {
        return (int) (totalPagado / 50.0);
    }

    @Override
    public String obtenerNombreNivel() {
        return "Bronce";
    }

    @Override
    public double obtenerPorcentajeDescuento() {
        return 2.0;
    }
}
