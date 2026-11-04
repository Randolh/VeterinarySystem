package randolh.veterinarysystem.inventario;

import randolh.veterinarysystem.core.exception.InventarioException;
import randolh.veterinarysystem.core.exception.OperacionCanceladaException;
import randolh.veterinarysystem.core.exception.ValidacionException;
import randolh.veterinarysystem.core.utils.ConsolaUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

// Vista de consola interactiva para la gestion de farmacia, almacen y proveedores
public class InventarioView {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final InventarioController controller;

    public InventarioView() {
        this.controller = new InventarioController();
    }

    public InventarioView(InventarioController controller) {
        this.controller = controller;
    }

    // Menu principal del modulo de inventario y almacen
    public void mostrarMenu() {
        Runnable menu = () -> {
            ConsolaUtils.imprimirTitulo("MODULO DE FARMACIA, ALMACEN Y PROVEEDORES");
            System.out.println(ConsolaUtils.COLOR_INFO + "1. Catalogo general de productos (tabla paginada)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "2. Registrar nuevo producto / medicamento" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "3. Actualizar informacion de un producto" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "4. Recepcion de mercaderia (Entrada / Nuevo lote)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "5. Registrar salida o descarte de mercaderia" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "6. Ajuste manual de inventario fisico" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "7. Reporte critico: Articulos bajo stock minimo" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "8. Reporte preventivo: Medicamentos proximos a vencer" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "9. Reporte de medicamentos ya vencidos" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "10. Historial de movimientos de kardex por producto" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "11. Submodulo de gestion de proveedores" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "0. Volver al menu anterior" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "----------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);
        };

        ConsolaUtils.setMenuActual(menu);

        boolean continuar = true;
        while (continuar) {
            menu.run();
            int opcion = ConsolaUtils.leerEnteroEnRango("Seleccione una opcion [0-11]: ", 0, 11);

            switch (opcion) {
                case 1 -> listarCatalogoPaginado();
                case 2 -> registrarNuevoProducto();
                case 3 -> actualizarProducto();
                case 4 -> registrarEntradaMercaderia();
                case 5 -> registrarSalidaMercaderia();
                case 6 -> registrarAjusteFisico();
                case 7 -> reporteBajoStockMinimo();
                case 8 -> reporteProximosAVencer();
                case 9 -> reporteVencidos();
                case 10 -> consultarKardexProducto();
                case 11 -> mostrarMenuProveedores();
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

    // Catalogo general paginado
    private void listarCatalogoPaginado() {
        ConsolaUtils.imprimirSubtitulo("Catalogo General de Productos e Insumos");
        List<Producto> lista = controller.listarTodos();
        if (lista.isEmpty()) {
            ConsolaUtils.imprimirInfo("No hay productos registrados en el inventario.");
            return;
        }
        ConsolaUtils.mostrarTablaPaginada(lista, 8, this::renderizarTablaProductos);
    }

    // Renderiza la tabla formateada de productos
    private void renderizarTablaProductos(List<Producto> lista) {
        String formatoEncabezado = "| %-4s | %-20s | %-12s | %-8s | %-6s | %-5s | %-10s | %-8s |%n";
        String formatoFila = "| %-4d | %-20s | %-12s | %-8s | %-6s | %-5d | %-10s | %-8.2f |%n";
        String separador = "+------+----------------------+--------------+----------+--------+-------+------------+----------+";

        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);
        System.out.printf(ConsolaUtils.COLOR_TITULO + formatoEncabezado + ConsolaUtils.ANSI_RESET,
                "ID", "PRODUCTO", "TIPO", "LOTE", "STOCK", "MIN", "VENCIMIENTO", "PRECIO");
        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);

        for (Producto p : lista) {
            String nombreCorto = truncar(p.getNombreProducto(), 20);
            String tipoCorto = truncar(p.getTipoNombre() != null ? p.getTipoNombre() : String.valueOf(p.getProductoTipoId()), 12);
            String lote = truncar(p.getLote() != null ? p.getLote() : "-", 8);
            String venc = p.getFechaVencimiento() != null ? p.getFechaVencimiento().format(FORMATO_FECHA) : "Sin venc";

            String stockColor;
            if (p.isBajoStockMinimo()) {
                stockColor = ConsolaUtils.COLOR_ERROR + String.format("%-6d", p.getCantidadStock()) + ConsolaUtils.ANSI_RESET;
            } else {
                stockColor = String.format("%-6d", p.getCantidadStock());
            }

            System.out.printf(formatoFila, p.getId(), nombreCorto, tipoCorto, lote,
                    stockColor, p.getStockMinimo(), venc, p.getPrecioVenta());
        }
        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);
    }

    // Formulario de alta para un producto
    private void registrarNuevoProducto() {
        ConsolaUtils.imprimirSubtitulo("Registrar Nuevo Producto en Almacen");

        try {
            String nombre = ConsolaUtils.leerTextoObligatorio("Nombre del producto/medicamento: ", 255);

            System.out.println(ConsolaUtils.ANSI_GRAY + "Tipos de producto disponibles:" + ConsolaUtils.ANSI_RESET);
            List<ProductoTipo> tipos = controller.listarTiposProducto();
            for (ProductoTipo t : tipos) {
                System.out.printf("  [%d] %s (%s)\n", t.getId(), t.getNombre(), t.getDescripcion());
            }
            int tipoId = ConsolaUtils.leerEntero("ID del tipo de producto: ");

            String descripcion = ConsolaUtils.leerTextoOpcional("Descripcion del producto", "");
            String fabricante = ConsolaUtils.leerTextoOpcional("Laboratorio / Fabricante", "");

            System.out.println(ConsolaUtils.ANSI_GRAY + "Proveedores registrados:" + ConsolaUtils.ANSI_RESET);
            List<Proveedor> provs = controller.listarProveedores();
            for (Proveedor pr : provs) {
                System.out.printf("  [%d] %s\n", pr.getId(), pr.getNombreEmpresa());
            }
            int provIdInput = ConsolaUtils.leerEntero("ID del proveedor asignado (0 si no aplica): ");
            Integer provId = provIdInput > 0 ? provIdInput : null;

            String lote = ConsolaUtils.leerTextoOpcional("Numero de lote", "");
            int stockInicial = ConsolaUtils.leerEntero("Cantidad de stock inicial: ");
            int stockMinimo = ConsolaUtils.leerEntero("Stock minimo de seguridad: ");
            String unidadMedida = ConsolaUtils.leerTextoOpcional("Unidad de medida", "unidad");
            LocalDate fechaVencimiento = ConsolaUtils.leerFechaOpcional("Fecha de vencimiento");

            double pCompra = ConsolaUtils.leerDecimal("Precio de compra unitario: ");
            double pVenta = ConsolaUtils.leerDecimal("Precio de venta al publico: ");
            boolean requiereReceta = ConsolaUtils.leerBooleano("Requiere prescripcion medica?");

            Producto p = new Producto(
                    null,
                    nombre,
                    tipoId,
                    descripcion,
                    fabricante,
                    provId,
                    lote,
                    stockInicial,
                    stockMinimo,
                    unidadMedida,
                    fechaVencimiento,
                    BigDecimal.valueOf(pCompra),
                    BigDecimal.valueOf(pVenta),
                    requiereReceta,
                    true,
                    null
            );

            Producto guardado = controller.registrarProducto(p);
            ConsolaUtils.imprimirExito("Producto registrado correctamente con ID: " + guardado.getId());

        } catch (ValidacionException e) {
            ConsolaUtils.imprimirAdvertencia(e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al registrar producto: " + e.getMessage());
        }
    }

    // Actualiza datos de un producto
    private void actualizarProducto() {
        ConsolaUtils.imprimirSubtitulo("Actualizar Informacion de Producto");

        int id = ConsolaUtils.leerEntero("Ingrese ID del producto a modificar: ");
        try {
            Optional<Producto> opt = controller.obtenerProductoPorId(id);
            if (opt.isEmpty()) {
                ConsolaUtils.imprimirError("No se encontro ningun producto con ID: " + id);
                return;
            }

            Producto actual = opt.get();
            ConsolaUtils.imprimirInfo("Modificando: " + actual.getNombreProducto() + " (Stock actual: " + actual.getCantidadStock() + ")");

            String nombre = ConsolaUtils.leerTextoOpcional("Nombre del producto", actual.getNombreProducto());
            String desc = ConsolaUtils.leerTextoOpcional("Descripcion", actual.getDescripcion() != null ? actual.getDescripcion() : "");
            String fab = ConsolaUtils.leerTextoOpcional("Fabricante", actual.getFabricante() != null ? actual.getFabricante() : "");
            int stockMin = ConsolaUtils.leerEntero("Nuevo stock minimo [" + actual.getStockMinimo() + "]: ");
            double pVenta = ConsolaUtils.leerDecimal("Nuevo precio de venta [" + actual.getPrecioVenta() + "]: ");
            boolean requiereReceta = ConsolaUtils.leerBooleano("Requiere receta? [" + (actual.isRequiereReceta() ? "S" : "N") + "]");

            actual.setNombreProducto(nombre);
            actual.setDescripcion(desc);
            actual.setFabricante(fab);
            actual.setStockMinimo(stockMin);
            actual.setPrecioVenta(BigDecimal.valueOf(pVenta));
            actual.setRequiereReceta(requiereReceta);

            boolean exito = controller.actualizarProducto(actual);
            if (exito) {
                ConsolaUtils.imprimirExito("Producto ID " + id + " actualizado exitosamente.");
            } else {
                ConsolaUtils.imprimirAdvertencia("No se pudo actualizar el producto.");
            }

        } catch (ValidacionException e) {
            ConsolaUtils.imprimirAdvertencia(e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al actualizar producto: " + e.getMessage());
        }
    }

    // Recepcion de mercaderia
    private void registrarEntradaMercaderia() {
        ConsolaUtils.imprimirSubtitulo("Recepcion de Mercaderia (Entrada / Nuevo Lote)");

        int id = ConsolaUtils.leerEntero("ID del producto a ingresar: ");
        try {
            Optional<Producto> opt = controller.obtenerProductoPorId(id);
            if (opt.isEmpty()) {
                ConsolaUtils.imprimirError("No existe producto con ID: " + id);
                return;
            }

            Producto p = opt.get();
            ConsolaUtils.imprimirInfo("Producto: " + p.getNombreProducto() + " - Stock actual: " + p.getCantidadStock());

            int cantidad = ConsolaUtils.leerEntero("Cantidad a ingresar (+): ");
            String lote = ConsolaUtils.leerTextoOpcional("Numero de nuevo lote", p.getLote() != null ? p.getLote() : "");
            LocalDate vencimiento = ConsolaUtils.leerFechaOpcional("Fecha de vencimiento del nuevo lote");
            String motivo = ConsolaUtils.leerTextoObligatorio("Motivo / Factura de compra: ", 255);
            String usuario = ConsolaUtils.leerTextoOpcional("Nombre de usuario / encargado", "Almacen");

            boolean exito = controller.registrarEntradaMercaderia(id, cantidad, lote, vencimiento, motivo, usuario);
            if (exito) {
                ConsolaUtils.imprimirExito("Recepcion de mercaderia registrada en kardex. Nuevo stock: " + (p.getCantidadStock() + cantidad));
            } else {
                ConsolaUtils.imprimirAdvertencia("No fue posible registrar la entrada.");
            }

        } catch (ValidacionException | InventarioException e) {
            ConsolaUtils.imprimirAdvertencia(e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al registrar entrada: " + e.getMessage());
        }
    }

    // Salida o descarte
    private void registrarSalidaMercaderia() {
        ConsolaUtils.imprimirSubtitulo("Salida o Descarte de Mercaderia");

        int id = ConsolaUtils.leerEntero("ID del producto: ");
        try {
            Optional<Producto> opt = controller.obtenerProductoPorId(id);
            if (opt.isEmpty()) {
                ConsolaUtils.imprimirError("No existe producto con ID: " + id);
                return;
            }

            Producto p = opt.get();
            ConsolaUtils.imprimirInfo("Producto: " + p.getNombreProducto() + " - Stock disponible: " + p.getCantidadStock());

            int cantidad = ConsolaUtils.leerEntero("Cantidad a retirar (-): ");
            System.out.println("Tipo de salida: [1] Salida comun / Venta directa | [2] Descarte por vencimiento o merma");
            int tipoOpcion = ConsolaUtils.leerEnteroEnRango("Seleccione tipo: ", 1, 2);
            TipoMovimientoEnum tipo = (tipoOpcion == 1) ? TipoMovimientoEnum.SALIDA : TipoMovimientoEnum.VENCIMIENTO;

            String motivo = ConsolaUtils.leerTextoObligatorio("Justificacion / Motivo de salida: ", 255);
            String usuario = ConsolaUtils.leerTextoOpcional("Responsable", "Almacen");

            boolean exito = controller.registrarSalidaODescarte(id, cantidad, tipo, motivo, usuario);
            if (exito) {
                ConsolaUtils.imprimirExito("Salida registrada en kardex exitosamente.");
            } else {
                ConsolaUtils.imprimirAdvertencia("No fue posible registrar la salida.");
            }

        } catch (ValidacionException | InventarioException e) {
            ConsolaUtils.imprimirAdvertencia(e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al registrar salida: " + e.getMessage());
        }
    }

    // Ajuste fisico
    private void registrarAjusteFisico() {
        ConsolaUtils.imprimirSubtitulo("Ajuste Manual de Inventario Fisico");

        int id = ConsolaUtils.leerEntero("ID del producto a auditar: ");
        try {
            Optional<Producto> opt = controller.obtenerProductoPorId(id);
            if (opt.isEmpty()) {
                ConsolaUtils.imprimirError("No existe producto con ID: " + id);
                return;
            }

            Producto p = opt.get();
            ConsolaUtils.imprimirInfo("Producto: " + p.getNombreProducto() + " - Stock en sistema: " + p.getCantidadStock());

            int conteoFisico = ConsolaUtils.leerEntero("Cantidad fisica real encontrada en estanteria: ");
            String motivo = ConsolaUtils.leerTextoObligatorio("Justificacion del desajuste: ", 255);
            String usuario = ConsolaUtils.leerTextoOpcional("Auditor", "Auditoria");

            boolean exito = controller.registrarAjusteFisico(id, conteoFisico, motivo, usuario);
            if (exito) {
                ConsolaUtils.imprimirExito("Ajuste de inventario aplicado con exito.");
            } else {
                ConsolaUtils.imprimirAdvertencia("No se pudo aplicar el ajuste.");
            }

        } catch (ValidacionException | InventarioException e) {
            ConsolaUtils.imprimirAdvertencia(e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al registrar ajuste: " + e.getMessage());
        }
    }

    // Reporte stock minimo
    private void reporteBajoStockMinimo() {
        ConsolaUtils.imprimirSubtitulo("Reporte Critico: Articulos Bajo Stock de Seguridad");

        try {
            List<Producto> lista = controller.listarBajoStockMinimo();
            if (lista.isEmpty()) {
                ConsolaUtils.imprimirExito("Excelente: No hay articulos con stock por debajo del minimo requerido.");
                return;
            }

            ConsolaUtils.imprimirAdvertencia("Se detectaron " + lista.size() + " articulos en nivel critico de stock:");
            ConsolaUtils.mostrarTablaPaginada(lista, 8, this::renderizarTablaProductos);

        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al generar reporte de stock: " + e.getMessage());
        }
    }

    // Reporte proximos a vencer
    private void reporteProximosAVencer() {
        ConsolaUtils.imprimirSubtitulo("Reporte Preventivo: Medicamentos Proximos a Caducar");

        int dias = ConsolaUtils.leerEnteroEnRango("Ventana de tiempo a consultar en dias [15-180]: ", 15, 180);
        try {
            List<Producto> lista = controller.listarProximosAVencer(dias);
            if (lista.isEmpty()) {
                ConsolaUtils.imprimirExito("No se registran medicamentos proximos a caducar en los siguientes " + dias + " dias.");
                return;
            }

            ConsolaUtils.imprimirAdvertencia("Medicamentos que vencen en menos de " + dias + " dias: " + lista.size());
            ConsolaUtils.mostrarTablaPaginada(lista, 8, this::renderizarTablaProductos);

        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al consultar vencimientos: " + e.getMessage());
        }
    }

    // Reporte vencidos
    private void reporteVencidos() {
        ConsolaUtils.imprimirSubtitulo("Reporte de Medicamentos ya Vencidos");

        try {
            List<Producto> lista = controller.listarVencidos();
            if (lista.isEmpty()) {
                ConsolaUtils.imprimirExito("No hay medicamentos vencidos en el almacen.");
                return;
            }

            ConsolaUtils.imprimirError("ALERTA: Se encontraron " + lista.size() + " articulos vencidos para retiro inmediato:");
            ConsolaUtils.mostrarTablaPaginada(lista, 8, this::renderizarTablaProductos);

        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al consultar medicamentos vencidos: " + e.getMessage());
        }
    }

    // Kardex
    private void consultarKardexProducto() {
        ConsolaUtils.imprimirSubtitulo("Historial de Kardex de un Producto");

        int id = ConsolaUtils.leerEntero("ID del producto a auditar: ");
        try {
            List<MovimientoInventario> movs = controller.listarMovimientosPorProducto(id);
            if (movs.isEmpty()) {
                ConsolaUtils.imprimirInfo("No hay movimientos registrados para este producto.");
                return;
            }

            String formatoEncabezado = "| %-4s | %-16s | %-12s | %-5s | %-6s | %-6s | %-20s |%n";
            String formatoFila = "| %-4d | %-16s | %-12s | %-5d | %-6d | %-6d | %-20s |%n";
            String separador = "+------+------------------+--------------+-------+--------+--------+----------------------+";

            System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);
            System.out.printf(ConsolaUtils.COLOR_TITULO + formatoEncabezado + ConsolaUtils.ANSI_RESET,
                    "ID", "FECHA/HORA", "MOVIMIENTO", "CANT", "PREVIO", "NUEVO", "MOTIVO");
            System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);

            for (MovimientoInventario m : movs) {
                String fStr = m.getFechaMovimiento() != null ? m.getFechaMovimiento().format(FORMATO_FECHA_HORA) : "--";
                String motivo = truncar(m.getMotivo() != null ? m.getMotivo() : "-", 20);
                System.out.printf(formatoFila, m.getId(), fStr, m.getTipoMovimiento().getDescripcion(),
                        m.getCantidad(), m.getStockAnterior(), m.getStockNuevo(), motivo);
            }
            System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);

        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al consultar kardex: " + e.getMessage());
        }
    }

    // ==========================================
    // SUBMODULO DE PROVEEDORES
    // ==========================================

    private void mostrarMenuProveedores() {
        boolean enProveedores = true;
        while (enProveedores) {
            try {
                ConsolaUtils.imprimirSubtitulo("SUBMODULO DE GESTION DE PROVEEDORES");
                System.out.println(ConsolaUtils.COLOR_INFO + "1. Registrar nuevo proveedor" + ConsolaUtils.ANSI_RESET);
                System.out.println(ConsolaUtils.COLOR_INFO + "2. Listar todos los proveedores" + ConsolaUtils.ANSI_RESET);
                System.out.println(ConsolaUtils.COLOR_INFO + "3. Actualizar informacion de proveedor" + ConsolaUtils.ANSI_RESET);
                System.out.println(ConsolaUtils.COLOR_INFO + "4. Desactivar proveedor (baja logica)" + ConsolaUtils.ANSI_RESET);
                System.out.println(ConsolaUtils.COLOR_BORDE + "0. Regresar al menu de inventario" + ConsolaUtils.ANSI_RESET);
                System.out.println(ConsolaUtils.COLOR_BORDE + "--------------------------------------------------------" + ConsolaUtils.ANSI_RESET);

                int opcion = ConsolaUtils.leerEnteroEnRango("Seleccione una opcion [0-4]: ", 0, 4);
                switch (opcion) {
                    case 1 -> registrarProveedor();
                    case 2 -> listarProveedores();
                    case 3 -> actualizarProveedor();
                    case 4 -> desactivarProveedor();
                    case 0 -> enProveedores = false;
                    default -> ConsolaUtils.imprimirError("Opcion invalida.");
                }

                if (enProveedores) {
                    ConsolaUtils.pausar();
                }
            } catch (OperacionCanceladaException e) {
                ConsolaUtils.imprimirAdvertencia(e.getMessage() + " Regresando al menu de inventario...");
                enProveedores = false;
            }
        }
    }

    private void registrarProveedor() {
        ConsolaUtils.imprimirSubtitulo("Registrar Nuevo Proveedor Farmaceutico");
        try {
            String empresa = ConsolaUtils.leerTextoObligatorio("Nombre / Razon Social de la empresa: ", 255);
            String contacto = ConsolaUtils.leerTextoOpcional("Persona o agente de contacto", "");
            String tel = ConsolaUtils.leerTextoOpcional("Telefono", "");
            String email = ConsolaUtils.leerTextoOpcional("Correo electronico", "");
            String dir = ConsolaUtils.leerTextoOpcional("Direccion fisica", "");
            String web = ConsolaUtils.leerTextoOpcional("Sitio web", "");

            Proveedor p = new Proveedor(empresa, contacto, tel, email, dir, web);
            Proveedor guardado = controller.registrarProveedor(p);
            ConsolaUtils.imprimirExito("Proveedor registrado con ID: " + guardado.getId());

        } catch (ValidacionException e) {
            ConsolaUtils.imprimirAdvertencia(e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al registrar proveedor: " + e.getMessage());
        }
    }

    private void listarProveedores() {
        ConsolaUtils.imprimirSubtitulo("Listado de Proveedores Registrados");
        List<Proveedor> lista = controller.listarProveedores();
        if (lista.isEmpty()) {
            ConsolaUtils.imprimirInfo("No hay proveedores registrados.");
            return;
        }

        String formatoEncabezado = "| %-4s | %-24s | %-18s | %-14s | %-22s | %-6s |%n";
        String formatoFila = "| %-4d | %-24s | %-18s | %-14s | %-22s | %-6s |%n";
        String separador = "+------+--------------------------+--------------------+----------------+------------------------+--------+";

        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);
        System.out.printf(ConsolaUtils.COLOR_TITULO + formatoEncabezado + ConsolaUtils.ANSI_RESET,
                "ID", "EMPRESA", "CONTACTO", "TELEFONO", "EMAIL", "ESTADO");
        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);

        for (Proveedor p : lista) {
            String estado = p.isActivo() ? "ACTIVO" : "INACT";
            System.out.printf(formatoFila, p.getId(), truncar(p.getNombreEmpresa(), 24),
                    truncar(p.getContacto() != null ? p.getContacto() : "-", 18),
                    truncar(p.getTelefono() != null ? p.getTelefono() : "-", 14),
                    truncar(p.getEmail() != null ? p.getEmail() : "-", 22),
                    estado);
        }
        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);
    }

    private void actualizarProveedor() {
        ConsolaUtils.imprimirSubtitulo("Actualizar Proveedor");
        int id = ConsolaUtils.leerEntero("ID del proveedor a actualizar: ");
        try {
            Optional<Proveedor> opt = controller.obtenerProveedorPorId(id);
            if (opt.isEmpty()) {
                ConsolaUtils.imprimirError("No existe proveedor con ID: " + id);
                return;
            }

            Proveedor p = opt.get();
            ConsolaUtils.imprimirInfo("Modificando: " + p.getNombreEmpresa());

            String empresa = ConsolaUtils.leerTextoOpcional("Nombre empresa", p.getNombreEmpresa());
            String contacto = ConsolaUtils.leerTextoOpcional("Contacto", p.getContacto() != null ? p.getContacto() : "");
            String tel = ConsolaUtils.leerTextoOpcional("Telefono", p.getTelefono() != null ? p.getTelefono() : "");
            String email = ConsolaUtils.leerTextoOpcional("Email", p.getEmail() != null ? p.getEmail() : "");
            String dir = ConsolaUtils.leerTextoOpcional("Direccion", p.getDireccion() != null ? p.getDireccion() : "");

            p.setNombreEmpresa(empresa);
            p.setContacto(contacto);
            p.setTelefono(tel);
            p.setEmail(email);
            p.setDireccion(dir);

            boolean exito = controller.actualizarProveedor(p);
            if (exito) {
                ConsolaUtils.imprimirExito("Proveedor ID " + id + " actualizado correctamente.");
            } else {
                ConsolaUtils.imprimirAdvertencia("No se pudo actualizar el proveedor.");
            }

        } catch (ValidacionException e) {
            ConsolaUtils.imprimirAdvertencia(e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al actualizar proveedor: " + e.getMessage());
        }
    }

    private void desactivarProveedor() {
        ConsolaUtils.imprimirSubtitulo("Desactivar Proveedor (Baja Logica)");
        int id = ConsolaUtils.leerEntero("ID del proveedor a dar de baja: ");
        try {
            boolean exito = controller.eliminarProveedor(id);
            if (exito) {
                ConsolaUtils.imprimirExito("Proveedor ID " + id + " desactivado con exito.");
            } else {
                ConsolaUtils.imprimirAdvertencia("No fue posible desactivar el proveedor.");
            }
        } catch (ValidacionException e) {
            ConsolaUtils.imprimirAdvertencia(e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al desactivar proveedor: " + e.getMessage());
        }
    }

    private String truncar(String texto, int max) {
        if (texto == null) return "-";
        return texto.length() <= max ? texto : texto.substring(0, max - 3) + "...";
    }
}
