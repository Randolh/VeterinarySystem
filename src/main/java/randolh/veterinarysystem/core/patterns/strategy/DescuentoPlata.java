package randolh.veterinarysystem.core.patterns.strategy;

import randolh.veterinarysystem.facturacion.ItemFactura;

import java.util.List;

// Estrategia de descuento y puntos para nivel Plata del Club de Mascotas
public class DescuentoPlata implements EstrategiaDescuento {

    @Override
    public double aplicarDescuento(double subtotal, List<ItemFactura> items) {
        return Math.round(subtotal * 0.05 * 100.0) / 100.0;
    }

    @Override
    public int calcularPuntosGanados(double totalPagado) {
        return (int) (totalPagado / 30.0);
    }

    @Override
    public String obtenerNombreNivel() {
        return "Plata";
    }

    @Override
    public double obtenerPorcentajeDescuento() {
        return 5.0;
    }
}
