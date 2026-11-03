package randolh.veterinarysystem.facturacion;

import randolh.veterinarysystem.core.exception.ValidacionException;
import randolh.veterinarysystem.core.patterns.strategy.EstrategiaDescuento;
import randolh.veterinarysystem.core.utils.ConsolaUtils;
import randolh.veterinarysystem.dueno.Dueno;
import randolh.veterinarysystem.inventario.Producto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Vista de consola para facturacion comercial, aplicacion de estrategia de descuentos y exportacion dual
public class FacturacionView {

    private final FacturacionController controller;

    public FacturacionView() {
        this.controller = new FacturacionController();
    }

    public FacturacionView(FacturacionController controller) {
        this.controller = controller != null ? controller : new FacturacionController();
    }

    // Muestra el menu principal del modulo de facturacion
    public void mostrarMenu() {
        Runnable menu = () -> {
            ConsolaUtils.imprimirTitulo("MODULO DE FACTURACION Y LIQUIDACION FISCAL");
            System.out.println(ConsolaUtils.COLOR_INFO + "1. Emitir nueva factura de venta" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "2. Listar todas las facturas emitidas (tabla paginada)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "3. Consultar detalle de factura por ID o Numero" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "4. Reimprimir / exportar comprobantes (PDF y Texto plano)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "5. Anular factura de venta" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "0. Volver al menu anterior" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "----------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);
        };

        ConsolaUtils.setMenuActual(menu);

        boolean continuar = true;
        while (continuar) {
            menu.run();
            int opcion = ConsolaUtils.leerEnteroEnRango("Seleccione una opcion [0-5]: ", 0, 5);

            switch (opcion) {
                case 1 -> emitirNuevaFactura();
                case 2 -> listarFacturasPaginadas();
                case 3 -> consultarDetalleFactura();
                case 4 -> reimprimirComprobantes();
                case 5 -> anularFactura();
                case 0 -> {
                    ConsolaUtils.imprimirInfo("Regresando al menu principal...");
                    continuar = false;
                }
                default -> ConsolaUtils.imprimirError("Opcion no reconocida.");
            }

            if (continuar) {
                ConsolaUtils.pausar();
            }
        }
    }

    // Flujo interactivo de emision de factura aplicando Strategy y generacion dual de documentos
    private void emitirNuevaFactura() {
        ConsolaUtils.imprimirSubtitulo("EMISION DE NUEVA FACTURA DE VENTA");

        // 1. Seleccionar cliente / dueno
        int duenoId = ConsolaUtils.leerEntero("ID del Dueno / Cliente a facturar: ");
        Optional<Dueno> duenoOpt = controller.getDuenoDAO().buscarPorId(duenoId);
        if (duenoOpt.isEmpty()) {
            ConsolaUtils.imprimirError("No se encontro dueno con el ID especificado.");
            return;
        }

        Dueno dueno = duenoOpt.get();
        if (!dueno.isActivo()) {
            ConsolaUtils.imprimirAdvertencia("El cliente " + dueno.getNombreCompleto() + " esta inactivo en el sistema.");
            return;
        }

        // Mostrar identificacion del cliente y nivel de fidelizacion Strategy
        EstrategiaDescuento estrategia = controller.resolverEstrategiaDueno(duenoId);
        Optional<FacturacionDAO.InfoClubDueno> clubOpt = controller.obtenerInfoClub(duenoId);

        System.out.println(ConsolaUtils.ANSI_CYAN + "Cliente: " + dueno.getNombreCompleto() +
                " | Doc: " + dueno.getNumeroDocumento() + " | Tel: " + dueno.getTelefono() + ConsolaUtils.ANSI_RESET);

        if (clubOpt.isPresent() && clubOpt.get().isActivo()) {
            System.out.println(ConsolaUtils.badgeExito("CLUB MASCOTAS: " + estrategia.obtenerNombreNivel().toUpperCase()) +
                    " " + ConsolaUtils.ANSI_YELLOW + "Descuento aplicable: " + estrategia.obtenerPorcentajeDescuento() + "%" +
                    " | Puntos acumulados actuales: " + clubOpt.get().getPuntosDisponibles() + " pts" + ConsolaUtils.ANSI_RESET);
        } else {
            System.out.println(ConsolaUtils.badgeInfo("CLIENTE GENERAL (SIN CLUB)") +
                    " " + ConsolaUtils.ANSI_GRAY + "Sin descuentos especiales de fidelizacion." + ConsolaUtils.ANSI_RESET);
        }

        // 2. Armar lista interactiva de renglones o items a facturar
        List<ItemFactura> items = new ArrayList<>();
        boolean agregandoItems = true;

        while (agregandoItems) {
            System.out.println("\n" + ConsolaUtils.ANSI_BOLD + "Agregar concepto a la factura:" + ConsolaUtils.ANSI_RESET);
            System.out.println("  1. Agregar Medicamento / Insumo de Farmacia (Inventario)");
            System.out.println("  2. Agregar Servicio Clinico / Prestacion Medica (Catalogo)");
            System.out.println("  3. Finalizar y proceder a la liquidacion");
            System.out.println("  0. Cancelar emision de factura");

            int opcItem = ConsolaUtils.leerEnteroEnRango("Seleccione opcion [0-3]: ", 0, 3);
            switch (opcItem) {
                case 1 -> agregarItemProducto(items);
                case 2 -> agregarItemServicio(items);
                case 3 -> {
                    if (items.isEmpty()) {
                        ConsolaUtils.imprimirAdvertencia("Debe agregar al menos un item para liquidar la factura.");
                    } else {
                        agregandoItems = false;
                    }
                }
                case 0 -> {
                    ConsolaUtils.imprimirAdvertencia("Emision de factura cancelada por el usuario.");
                    return;
                }
            }
        }

        liquidarYProcesarFactura(duenoId, items, estrategia, "");
    }

    // Emite factura formal a partir de una atencion clinica o quirurgica con items precargados
    public void emitirFacturaConItemsPredefinidos(int duenoId, List<ItemFactura> itemsIniciales, String observacionesSugeridas) {
        ConsolaUtils.imprimirSubtitulo("FACTURACION DIRECTA DE ATENCION MEDICA");

        Optional<Dueno> duenoOpt = controller.getDuenoDAO().buscarPorId(duenoId);
        if (duenoOpt.isEmpty()) {
            ConsolaUtils.imprimirError("No se encontro cliente/dueno con ID: " + duenoId);
            return;
        }

        Dueno dueno = duenoOpt.get();
        if (!dueno.isActivo()) {
            ConsolaUtils.imprimirAdvertencia("El cliente " + dueno.getNombreCompleto() + " esta inactivo en el sistema.");
            return;
        }

        EstrategiaDescuento estrategia = controller.resolverEstrategiaDueno(duenoId);
        Optional<FacturacionDAO.InfoClubDueno> clubOpt = controller.obtenerInfoClub(duenoId);

        System.out.println(ConsolaUtils.ANSI_CYAN + "Cliente: " + dueno.getNombreCompleto() +
                " | Doc: " + dueno.getNumeroDocumento() + " | Tel: " + dueno.getTelefono() + ConsolaUtils.ANSI_RESET);

        if (clubOpt.isPresent() && clubOpt.get().isActivo()) {
            System.out.println(ConsolaUtils.badgeExito("CLUB MASCOTAS: " + estrategia.obtenerNombreNivel().toUpperCase()) +
                    " " + ConsolaUtils.ANSI_YELLOW + "Descuento aplicable: " + estrategia.obtenerPorcentajeDescuento() + "%" +
                    " | Puntos acumulados actuales: " + clubOpt.get().getPuntosDisponibles() + " pts" + ConsolaUtils.ANSI_RESET);
        } else {
            System.out.println(ConsolaUtils.badgeInfo("CLIENTE GENERAL (SIN CLUB)") +
                    " " + ConsolaUtils.ANSI_GRAY + "Sin descuentos especiales de fidelizacion." + ConsolaUtils.ANSI_RESET);
        }

        List<ItemFactura> items = new ArrayList<>(itemsIniciales != null ? itemsIniciales : List.of());

        System.out.println("\n" + ConsolaUtils.ANSI_CYAN + "Conceptos precargados de la atencion:" + ConsolaUtils.ANSI_RESET);
        for (int i = 0; i < items.size(); i++) {
            ItemFactura it = items.get(i);
            System.out.printf("  %d. [%s] %s x%d = $%.2f%n",
                    i + 1, it.getTipoItem(), it.getNombreItem(), it.getCantidad(), it.getSubtotal());
        }

        boolean agregarMas = ConsolaUtils.leerBooleano("¿Desea agregar articulos o servicios adicionales a la factura?");
        if (agregarMas) {
            boolean agregando = true;
            while (agregando) {
                System.out.println("\n" + ConsolaUtils.ANSI_BOLD + "Agregar concepto adicional:" + ConsolaUtils.ANSI_RESET);
                System.out.println("  1. Agregar Medicamento / Insumo de Farmacia (Inventario)");
                System.out.println("  2. Agregar Servicio Clinico / Prestacion Medica (Catalogo)");
                System.out.println("  3. Proceder a la liquidacion");
                System.out.println("  0. Cancelar emision de factura");

                int opc = ConsolaUtils.leerEnteroEnRango("Seleccione opcion [0-3]: ", 0, 3);
                switch (opc) {
                    case 1 -> agregarItemProducto(items);
                    case 2 -> agregarItemServicio(items);
                    case 3 -> {
                        if (items.isEmpty()) {
                            ConsolaUtils.imprimirAdvertencia("Debe haber al menos un item para facturar.");
                        } else {
                            agregando = false;
                        }
                    }
                    case 0 -> {
                        ConsolaUtils.imprimirAdvertencia("Facturacion cancelada.");
                        return;
                    }
                }
            }
        }

        liquidarYProcesarFactura(duenoId, items, estrategia, observacionesSugeridas);
    }

    // Liquidacion financiera, calculo de impuestos/descuentos y emision dual
    private void liquidarYProcesarFactura(int duenoId, List<ItemFactura> items, EstrategiaDescuento estrategia, String observacionesDefecto) {
        if (items.isEmpty()) {
            ConsolaUtils.imprimirAdvertencia("No hay conceptos para facturar.");
            return;
        }

        // Mostrar previsualizacion del calculo financiero preliminar
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemFactura it : items) {
            subtotal = subtotal.add(it.getSubtotal());
        }
        double descVal = estrategia.aplicarDescuento(subtotal.doubleValue(), items);
        BigDecimal descuento = BigDecimal.valueOf(descVal).setScale(2, RoundingMode.HALF_UP);
        BigDecimal base = subtotal.subtract(descuento);
        if (base.compareTo(BigDecimal.ZERO) < 0) base = BigDecimal.ZERO;
        double ivaPorc = 19.0;
        BigDecimal impuesto = base.multiply(BigDecimal.valueOf(ivaPorc / 100.0)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = base.add(impuesto).setScale(2, RoundingMode.HALF_UP);
        int puntosGanados = estrategia.calcularPuntosGanados(total.doubleValue());

        System.out.println("\n" + ConsolaUtils.COLOR_TITULO + "=== RESUMEN PREVIO DE FACTURACION ===" + ConsolaUtils.ANSI_RESET);
        System.out.printf(" Cantidad de items:       %d articulos/servicios%n", items.size());
        System.out.printf(" Subtotal Bruto:          $ %10.2f%n", subtotal);
        System.out.printf(" Descuento (%s - %.0f%%):  -$ %9.2f%n", estrategia.obtenerNombreNivel(), estrategia.obtenerPorcentajeDescuento(), descuento);
        System.out.printf(" Base Gravable:           $ %10.2f%n", base);
        System.out.printf(" Impuesto IVA (%.0f%%):     $ %10.2f%n", ivaPorc, impuesto);
        System.out.printf(ConsolaUtils.ANSI_BOLD + " TOTAL NETO A PAGAR:      $ %10.2f%n" + ConsolaUtils.ANSI_RESET, total);
        if (puntosGanados > 0) {
            System.out.printf(ConsolaUtils.ANSI_GREEN + " Puntos Club a acumular:  +%d puntos%n" + ConsolaUtils.ANSI_RESET, puntosGanados);
        }

        // Metodo de pago
        System.out.println("\n" + ConsolaUtils.ANSI_BOLD + "Seleccione Metodo de Pago:" + ConsolaUtils.ANSI_RESET);
        System.out.println("  1. Efectivo");
        System.out.println("  2. Tarjeta");
        System.out.println("  3. Transferencia");
        System.out.println("  4. Mixto");
        int opcPago = ConsolaUtils.leerEnteroEnRango("Opcion de pago [1-4]: ", 1, 4);
        MetodoPagoEnum metodoPago = switch (opcPago) {
            case 2 -> MetodoPagoEnum.TARJETA;
            case 3 -> MetodoPagoEnum.TRANSFERENCIA;
            case 4 -> MetodoPagoEnum.MIXTO;
            default -> MetodoPagoEnum.EFECTIVO;
        };

        String defaultObs = (observacionesDefecto != null && !observacionesDefecto.trim().isEmpty()) ? observacionesDefecto.trim() : "";
        String obsPrompt = !defaultObs.isEmpty()
                ? "Observaciones [Enter para '" + defaultObs + "']: "
                : "Observaciones o notas contables (opcional): ";
        String observaciones = ConsolaUtils.leerTextoOpcional(obsPrompt, defaultObs);

        boolean confirmar = ConsolaUtils.leerBooleano("Desea confirmar y emitir formalmente la factura?");
        if (!confirmar) {
            ConsolaUtils.imprimirAdvertencia("Operacion abortada. No se emitio la factura.");
            return;
        }

        try {
            Factura emitida = controller.emitirFactura(duenoId, items, metodoPago, observaciones);
            ConsolaUtils.imprimirExito("Factura emitida exitosamente!");
            System.out.println(ConsolaUtils.badgeExito("FACTURA: " + emitida.getNumeroFactura()) +
                    " Total: $" + emitida.getTotal() + " | Estado: " + emitida.getEstado());
            System.out.println(ConsolaUtils.ANSI_CYAN + "Documento TXT generado en: exportaciones/facturas/factura_" + emitida.getNumeroFactura() + ".txt" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.ANSI_CYAN + "Documento PDF generado en: exportaciones/facturas/factura_" + emitida.getNumeroFactura() + ".pdf" + ConsolaUtils.ANSI_RESET);

        } catch (ValidacionException ve) {
            ConsolaUtils.imprimirAdvertencia("Error de validacion: " + ve.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al procesar la factura: " + e.getMessage());
        }
    }

    // Agrega un producto de farmacia/inventario como renglon de factura
    private void agregarItemProducto(List<ItemFactura> items) {
        int prodId = ConsolaUtils.leerEntero("ID del producto de inventario: ");
        Optional<Producto> prodOpt = controller.getInventarioDAO().buscarProductoPorId(prodId);
        if (prodOpt.isEmpty()) {
            ConsolaUtils.imprimirError("No se encontro producto con ID: " + prodId);
            return;
        }

        Producto p = prodOpt.get();
        if (!p.isActivo()) {
            ConsolaUtils.imprimirAdvertencia("El producto '" + p.getNombreProducto() + "' esta inactivo.");
            return;
        }

        System.out.printf("Producto: %s | Stock actual: %d %s | Precio Venta: $%.2f%n",
                p.getNombreProducto(), p.getCantidadStock(), p.getUnidadMedida(), p.getPrecioVenta());

        int cantidad = ConsolaUtils.leerEnteroEnRango("Cantidad a facturar: ", 1, Math.max(1, p.getCantidadStock()));
        if (cantidad > p.getCantidadStock()) {
            ConsolaUtils.imprimirAdvertencia("Alerta: La cantidad supera el stock registrado (" + p.getCantidadStock() + ").");
        }

        double precioUnitario = ConsolaUtils.leerDecimalOpcional("Precio unitario [Enter para $ " + p.getPrecioVenta() + "]: ", p.getPrecioVenta().doubleValue());
        ItemFactura item = ItemFactura.paraProducto(p.getId(), p.getNombreProducto(), cantidad, BigDecimal.valueOf(precioUnitario));
        items.add(item);
        ConsolaUtils.imprimirExito(String.format("Item agregado: %s x%d = $%.2f", item.getNombreItem(), cantidad, item.getSubtotal()));
    }

    // Agrega un servicio clinico del catalogo como renglon de factura
    private void agregarItemServicio(List<ItemFactura> items) {
        List<FacturacionDAO.ServicioCatalogo> catalogo = controller.listarCatalogoServicios();
        System.out.println("\n" + ConsolaUtils.ANSI_BOLD + "Catalogo de Servicios Clinicos Disponibles:" + ConsolaUtils.ANSI_RESET);
        for (FacturacionDAO.ServicioCatalogo s : catalogo) {
            System.out.printf("  [%d] %-35s (%-15s) - $%.2f%n", s.getId(), s.getNombre(), s.getCategoria(), s.getPrecioBase());
        }

        int servId = ConsolaUtils.leerEntero("ID del servicio (o 0 para servicio personalizado): ");
        String nombreServicio;
        double precioBase;

        if (servId > 0) {
            Optional<FacturacionDAO.ServicioCatalogo> match = catalogo.stream().filter(s -> s.getId() == servId).findFirst();
            if (match.isPresent()) {
                nombreServicio = match.get().getNombre();
                precioBase = match.get().getPrecioBase().doubleValue();
            } else {
                nombreServicio = ConsolaUtils.leerTexto("Nombre/Descripcion del servicio: ");
                precioBase = ConsolaUtils.leerDecimal("Precio base: ");
            }
        } else {
            nombreServicio = ConsolaUtils.leerTexto("Nombre/Descripcion del servicio personalizado: ");
            precioBase = ConsolaUtils.leerDecimal("Precio base: ");
        }

        int cantidad = ConsolaUtils.leerEnteroEnRango("Cantidad de servicios: ", 1, 100);
        double precioUnitario = ConsolaUtils.leerDecimalOpcional("Precio unitario [Enter para $ " + precioBase + "]: ", precioBase);

        ItemFactura item = ItemFactura.paraServicio(servId > 0 ? servId : null, nombreServicio, cantidad, BigDecimal.valueOf(precioUnitario));
        items.add(item);
        ConsolaUtils.imprimirExito(String.format("Servicio agregado: %s x%d = $%.2f", item.getNombreItem(), cantidad, item.getSubtotal()));
    }

    // Listado paginado de facturas emitidas
    private void listarFacturasPaginadas() {
        ConsolaUtils.imprimirSubtitulo("LISTADO DE FACTURAS EMITIDAS");
        List<Factura> facturas = controller.listarFacturas();
        if (facturas.isEmpty()) {
            ConsolaUtils.imprimirInfo("No hay facturas registradas en el sistema.");
            return;
        }

        ConsolaUtils.mostrarTablaPaginada(facturas, 8, sublista -> {
            System.out.printf(ConsolaUtils.ANSI_BOLD + "%-5s | %-15s | %-16s | %-24s | %-10s | %-10s | %-10s%n" + ConsolaUtils.ANSI_RESET,
                    "ID", "NUMERO", "FECHA", "CLIENTE", "SUBTOTAL", "TOTAL", "ESTADO");
            System.out.println(ConsolaUtils.COLOR_BORDE + "-".repeat(105) + ConsolaUtils.ANSI_RESET);

            for (Factura f : sublista) {
                String cliente = f.getNombreDueno() != null ? f.getNombreDueno() : "Dueno #" + f.getDuenoId();
                if (cliente.length() > 24) cliente = cliente.substring(0, 21) + "...";
                String fechaStr = f.getFechaEmision() != null ? f.getFechaEmision().toLocalDate().toString() : "-";

                String estadoBadge = f.getEstado() == EstadoFacturaEnum.PAGADA
                        ? ConsolaUtils.badgeExito(f.getEstado().getValor())
                        : (f.getEstado() == EstadoFacturaEnum.ANULADA
                        ? ConsolaUtils.badgeError(f.getEstado().getValor())
                        : ConsolaUtils.badgeAdvertencia(f.getEstado().getValor()));

                System.out.printf("%-5d | %-15s | %-16s | %-24s | $ %8.2f | $ %8.2f | %s%n",
                        f.getId(), f.getNumeroFactura(), fechaStr, cliente, f.getSubtotal(), f.getTotal(), estadoBadge);
            }
        });
    }

    // Consulta el detalle exhaustivo de una factura
    private void consultarDetalleFactura() {
        ConsolaUtils.imprimirSubtitulo("CONSULTAR DETALLE DE FACTURA");
        System.out.println("1. Buscar por ID de factura");
        System.out.println("2. Buscar por Numero correlativo (ej. FAC-2026-0001)");
        int opcion = ConsolaUtils.leerEnteroEnRango("Opcion [1-2]: ", 1, 2);

        Optional<Factura> facOpt;
        if (opcion == 1) {
            int id = ConsolaUtils.leerEntero("Ingrese ID de la factura: ");
            facOpt = controller.buscarFacturaPorId(id);
        } else {
            String num = ConsolaUtils.leerTexto("Ingrese numero de factura: ");
            facOpt = controller.buscarFacturaPorNumero(num);
        }

        if (facOpt.isEmpty()) {
            ConsolaUtils.imprimirError("Factura no encontrada.");
            return;
        }

        mostrarDetalleFacturaCompleta(facOpt.get());
    }

    // Muestra en consola la factura con encabezado y renglones de detalle
    private void mostrarDetalleFacturaCompleta(Factura f) {
        System.out.println("\n" + ConsolaUtils.COLOR_TITULO + "================================================================================" + ConsolaUtils.ANSI_RESET);
        System.out.println(ConsolaUtils.ANSI_BOLD + "  FACTURA DE VENTA: " + f.getNumeroFactura() + "  |  ID: " + f.getId() + ConsolaUtils.ANSI_RESET);
        System.out.println(ConsolaUtils.COLOR_TITULO + "================================================================================" + ConsolaUtils.ANSI_RESET);
        System.out.printf(" Fecha Emision:  %-35s Estado: %s%n", f.getFechaEmision(), f.getEstado());
        System.out.printf(" Cliente / Dueno: %-35s Documento: %s%n", f.getNombreDueno(), f.getDocumentoDueno());
        System.out.printf(" Telefono:       %-35s Correo:    %s%n", f.getTelefonoDueno(), f.getEmailDueno());
        System.out.printf(" Nivel Club:     %-35s Metodo:    %s%n", f.getNivelClub(), f.getMetodoPago());
        System.out.println(ConsolaUtils.COLOR_BORDE + "--------------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);
        System.out.printf(ConsolaUtils.ANSI_BOLD + " %-4s | %-10s | %-34s | %-5s | %-10s | %-10s%n" + ConsolaUtils.ANSI_RESET,
                "ITEM", "TIPO", "DESCRIPCION", "CANT", "PRECIO U.", "SUBTOTAL");
        System.out.println(ConsolaUtils.COLOR_BORDE + "--------------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);

        int idx = 1;
        if (f.getItems() != null && !f.getItems().isEmpty()) {
            for (ItemFactura it : f.getItems()) {
                String desc = it.getNombreItem();
                if (desc != null && desc.length() > 34) desc = desc.substring(0, 31) + "...";
                System.out.printf(" %-4d | %-10s | %-34s | %-5d | $ %8.2f | $ %8.2f%n",
                        idx++, it.getTipoItem(), desc, it.getCantidad(), it.getPrecioUnitario(), it.getSubtotal());
            }
        } else {
            System.out.println("  (Sin detalles registrados)");
        }

        System.out.println(ConsolaUtils.COLOR_BORDE + "--------------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);
        System.out.printf("%65s: $ %10.2f%n", "Subtotal Bruto", f.getSubtotal());
        if (f.getDescuento().compareTo(BigDecimal.ZERO) > 0) {
            System.out.printf("%65s: -$ %9.2f%n", "Descuento Club (" + f.getNivelClub() + ")", f.getDescuento());
        }
        System.out.printf("%65s: $ %10.2f%n", "Impuesto (IVA 19%)", f.getImpuesto());
        System.out.printf(ConsolaUtils.ANSI_BOLD + "%65s: $ %10.2f%n" + ConsolaUtils.ANSI_RESET, "TOTAL NETO", f.getTotal());
        System.out.println(ConsolaUtils.COLOR_TITULO + "================================================================================" + ConsolaUtils.ANSI_RESET);
    }

    // Reimprime los documentos de texto plano y PDF
    private void reimprimirComprobantes() {
        ConsolaUtils.imprimirSubtitulo("REIMPRIMIR / EXPORTAR COMPROBANTES");
        int id = ConsolaUtils.leerEntero("ID de la factura: ");
        Optional<Factura> facOpt = controller.buscarFacturaPorId(id);
        if (facOpt.isEmpty()) {
            ConsolaUtils.imprimirError("Factura no encontrada.");
            return;
        }

        Factura f = facOpt.get();
        controller.exportarComprobantes(f);
        ConsolaUtils.imprimirExito("Archivos reexportados exitosamente en 'exportaciones/facturas/':");
        System.out.println("  - factura_" + f.getNumeroFactura() + ".txt");
        System.out.println("  - factura_" + f.getNumeroFactura() + ".pdf");
    }

    // Anulacion logica de factura
    private void anularFactura() {
        ConsolaUtils.imprimirSubtitulo("ANULAR FACTURA DE VENTA");
        int id = ConsolaUtils.leerEntero("ID de la factura a anular: ");
        Optional<Factura> facOpt = controller.buscarFacturaPorId(id);
        if (facOpt.isEmpty()) {
            ConsolaUtils.imprimirError("Factura no encontrada.");
            return;
        }

        Factura f = facOpt.get();
        if (f.getEstado() == EstadoFacturaEnum.ANULADA) {
            ConsolaUtils.imprimirAdvertencia("La factura " + f.getNumeroFactura() + " ya se encuentra anulada.");
            return;
        }

        mostrarDetalleFacturaCompleta(f);
        boolean confirmar = ConsolaUtils.leerBooleano("Confirma la anulacion de la factura " + f.getNumeroFactura() + "?");
        if (!confirmar) {
            ConsolaUtils.imprimirAdvertencia("Operacion cancelada.");
            return;
        }

        try {
            boolean exito = controller.anularFactura(id);
            if (exito) {
                ConsolaUtils.imprimirExito("Factura " + f.getNumeroFactura() + " anulada correctamente.");
            } else {
                ConsolaUtils.imprimirError("No se pudo actualizar el estado de la factura.");
            }
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al anular factura: " + e.getMessage());
        }
    }
}
