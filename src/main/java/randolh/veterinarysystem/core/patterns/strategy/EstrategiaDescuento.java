package randolh.veterinarysystem.core.patterns.strategy;

import randolh.veterinarysystem.facturacion.ItemFactura;

import java.util.List;

// Interfaz para el calculo de descuentos y fidelizacion en el patron Strategy
public interface EstrategiaDescuento {

    double aplicarDescuento(double subtotal, List<ItemFactura> items);

    int calcularPuntosGanados(double totalPagado);

    String obtenerNombreNivel();

    double obtenerPorcentajeDescuento();
}
