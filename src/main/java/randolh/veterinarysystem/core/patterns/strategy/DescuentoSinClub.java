package randolh.veterinarysystem.core.patterns.strategy;

import randolh.veterinarysystem.facturacion.ItemFactura;

import java.util.List;

// Estrategia para clientes no afiliados al Club de Mascotas
public class DescuentoSinClub implements EstrategiaDescuento {

    @Override
    public double aplicarDescuento(double subtotal, List<ItemFactura> items) {
        return 0.0;
    }

    @Override
    public int calcularPuntosGanados(double totalPagado) {
        return 0;
    }

    @Override
    public String obtenerNombreNivel() {
        return "Sin Club";
    }

    @Override
    public double obtenerPorcentajeDescuento() {
        return 0.0;
    }
}
