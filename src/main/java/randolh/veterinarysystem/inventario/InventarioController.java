package randolh.veterinarysystem.inventario;

import randolh.veterinarysystem.core.config.TransaccionManager;
import randolh.veterinarysystem.core.exception.InventarioException;
import randolh.veterinarysystem.core.exception.ValidacionException;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

// Controlador de logica de negocio para almacen, proveedores y movimientos de kardex
public class InventarioController {

    private static final String MODULO = "InventarioController";
    private static final Pattern PATRON_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PATRON_TELEFONO = Pattern.compile("^[0-9+()\\-\\s]{7,20}$");

    private final InventarioDAO inventarioDAO;

    public InventarioController() {
        this.inventarioDAO = new InventarioDAO();
    }

    public InventarioController(InventarioDAO inventarioDAO) {
        this.inventarioDAO = inventarioDAO;
    }

    // ==========================================
    // GESTION DE PROVEEDORES
    // ==========================================

    // Registra nuevo proveedor validando unicidad de nombre y formato de contacto
    public Proveedor registrarProveedor(Proveedor p) {
        validarProveedor(p);

        Optional<Proveedor> existente = inventarioDAO.buscarProveedorPorNombre(p.getNombreEmpresa());
        if (existente.isPresent()) {
            throw new ValidacionException("Ya existe un proveedor registrado con el nombre: " + p.getNombreEmpresa());
        }

        LoggerManager.info(MODULO, "Registrando proveedor: " + p.getNombreEmpresa());
        return inventarioDAO.insertarProveedor(p);
    }

    // Actualiza datos de un proveedor
    public boolean actualizarProveedor(Proveedor p) {
        if (p.getId() == null || p.getId() <= 0) {
            throw new ValidacionException("El ID del proveedor es obligatorio para actualizar.");
        }
        validarProveedor(p);

        Optional<Proveedor> existente = inventarioDAO.buscarProveedorPorNombre(p.getNombreEmpresa());
        if (existente.isPresent() && !existente.get().getId().equals(p.getId())) {
            throw new ValidacionException("Ya existe otro proveedor registrado con el nombre: " + p.getNombreEmpresa());
        }

        LoggerManager.info(MODULO, "Actualizando proveedor ID: " + p.getId());
        return inventarioDAO.actualizarProveedor(p);
    }

    // Desactiva proveedor
    public boolean eliminarProveedor(int id) {
        if (id <= 0) {
            throw new ValidacionException("El ID del proveedor debe ser mayor a 0.");
        }
        return inventarioDAO.eliminarLogicoProveedor(id);
    }

    // Busca proveedor por ID
    public Optional<Proveedor> obtenerProveedorPorId(int id) {
        if (id <= 0) {
            throw new ValidacionException("El ID del proveedor debe ser mayor a 0.");
        }
        return inventarioDAO.buscarProveedorPorId(id);
    }

    // Lista proveedores
    public List<Proveedor> listarProveedores() {
        return inventarioDAO.listarProveedores();
    }

    // ==========================================
    // GESTION DE PRODUCTOS
    // ==========================================

    // Registra nuevo producto en inventario validando caducidad y stock
    public Producto registrarProducto(Producto p) {
        validarProducto(p);

        if (p.getFechaVencimiento() != null && p.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw new ValidacionException("No es posible registrar un producto cuya fecha de vencimiento ya expiro (" + p.getFechaVencimiento() + ").");
        }

        if (p.getProveedorId() != null) {
            Optional<Proveedor> prov = inventarioDAO.buscarProveedorPorId(p.getProveedorId());
            if (prov.isEmpty() || !prov.get().isActivo()) {
                throw new ValidacionException("El proveedor especificado no existe o se encuentra inactivo.");
            }
        }

        Optional<ProductoTipo> tipo = inventarioDAO.buscarTipoPorId(p.getProductoTipoId());
        if (tipo.isEmpty()) {
            throw new ValidacionException("La categoria/tipo de producto seleccionada (ID: " + p.getProductoTipoId() + ") no existe.");
        }

        LoggerManager.info(MODULO, "Registrando articulo en inventario: " + p.getNombreProducto());
        Producto guardado = inventarioDAO.insertarProducto(p);

        // Registro de kardex si ingresa con stock inicial
        if (guardado.getCantidadStock() > 0) {
            MovimientoInventario inicial = new MovimientoInventario(
                    guardado.getId(),
                    TipoMovimientoEnum.ENTRADA,
                    guardado.getCantidadStock(),
                    0,
                    guardado.getCantidadStock(),
                    "Registro de inventario inicial",
                    "Sistema"
            );
            inventarioDAO.registrarMovimiento(inicial);
        }

        return guardado;
    }

    // Actualiza datos de un articulo
    public boolean actualizarProducto(Producto p) {
        if (p.getId() == null || p.getId() <= 0) {
            throw new ValidacionException("El ID del producto es obligatorio para actualizar.");
        }
        validarProducto(p);

        if (p.getProveedorId() != null) {
            Optional<Proveedor> prov = inventarioDAO.buscarProveedorPorId(p.getProveedorId());
            if (prov.isEmpty() || !prov.get().isActivo()) {
                throw new ValidacionException("El proveedor especificado es invalido o esta inactivo.");
            }
        }

        LoggerManager.info(MODULO, "Actualizando producto ID: " + p.getId());
        return inventarioDAO.actualizarProducto(p);
    }

    // Desactivacion logica de producto
    public boolean eliminarProducto(int id) {
        if (id <= 0) {
            throw new ValidacionException("El ID del producto debe ser mayor a 0.");
        }
        return inventarioDAO.eliminarLogicoProducto(id);
    }

    // ==========================================
    // OPERACIONES DE KARDEX Y CONTROL FISICO
    // ==========================================

    // Registra entrada de mercaderia (recepcion de compra o nuevo lote) de forma transaccional
    public boolean registrarEntradaMercaderia(int productoId, int cantidad, String nuevoLote,
                                             LocalDate nuevoVencimiento, String motivo, String usuario) {
        if (cantidad <= 0) {
            throw new ValidacionException("La cantidad a ingresar debe ser mayor a 0.");
        }
        if (nuevoVencimiento != null && nuevoVencimiento.isBefore(LocalDate.now())) {
            throw new ValidacionException("No es posible ingresar mercaderia con fecha de caducidad expirada.");
        }

        return TransaccionManager.ejecutarEnTransaccion(conn -> {
            Producto p = inventarioDAO.buscarProductoPorId(productoId)
                    .orElseThrow(() -> new ValidacionException("Producto no encontrado con ID: " + productoId));

            int stockAnterior = p.getCantidadStock();
            int stockNuevo = stockAnterior + cantidad;

            // Actualizacion de stock fisico
            inventarioDAO.actualizarStock(productoId, stockNuevo, conn);

            // Actualizacion de lote y vencimiento si se especifican
            if (nuevoLote != null && !nuevoLote.trim().isEmpty()) {
                inventarioDAO.actualizarLoteYVencimiento(productoId, nuevoLote.trim(), nuevoVencimiento, conn);
            }

            // Registro en kardex
            MovimientoInventario mov = new MovimientoInventario(
                    null,
                    productoId,
                    TipoMovimientoEnum.ENTRADA,
                    cantidad,
                    stockAnterior,
                    stockNuevo,
                    motivo != null && !motivo.trim().isEmpty() ? motivo.trim() : "Entrada por recepcion de mercaderia",
                    null,
                    null,
                    usuario != null ? usuario : "Almacen",
                    null
            );
            inventarioDAO.registrarMovimiento(mov, conn);

            LoggerManager.info(MODULO, String.format("Entrada registrada para producto ID %d: +%d unidades. Stock actual: %d",
                    productoId, cantidad, stockNuevo));
            return true;
        });
    }

    // Registra salida o descarte de mercaderia verificando disponibilidad
    public boolean registrarSalidaODescarte(int productoId, int cantidad, TipoMovimientoEnum tipo,
                                           String motivo, String usuario) {
        if (cantidad <= 0) {
            throw new ValidacionException("La cantidad debe ser mayor a 0.");
        }
        if (tipo != TipoMovimientoEnum.SALIDA && tipo != TipoMovimientoEnum.VENCIMIENTO) {
            throw new ValidacionException("Tipo de movimiento invalido para una salida de almacen.");
        }

        return TransaccionManager.ejecutarEnTransaccion(conn -> {
            Producto p = inventarioDAO.buscarProductoPorId(productoId)
                    .orElseThrow(() -> new ValidacionException("Producto no encontrado con ID: " + productoId));

            int stockAnterior = p.getCantidadStock();
            if (cantidad > stockAnterior) {
                throw new InventarioException(String.format(
                        "Stock insuficiente para '%s'. Solicitado: %d, Disponible en almacen: %d",
                        p.getNombreProducto(), cantidad, stockAnterior));
            }

            int stockNuevo = stockAnterior - cantidad;
            inventarioDAO.actualizarStock(productoId, stockNuevo, conn);

            MovimientoInventario mov = new MovimientoInventario(
                    null,
                    productoId,
                    tipo,
                    cantidad,
                    stockAnterior,
                    stockNuevo,
                    motivo != null && !motivo.trim().isEmpty() ? motivo.trim() : "Salida registrada de almacen",
                    null,
                    null,
                    usuario != null ? usuario : "Almacen",
                    null
            );
            inventarioDAO.registrarMovimiento(mov, conn);

            LoggerManager.info(MODULO, String.format("Salida registrada para producto ID %d: -%d unidades (%s). Stock: %d",
                    productoId, cantidad, tipo.getDescripcion(), stockNuevo));
            return true;
        });
    }

    // Realiza un ajuste manual de inventario segun conteo fisico
    public boolean registrarAjusteFisico(int productoId, int nuevoStockFisico, String motivo, String usuario) {
        if (nuevoStockFisico < 0) {
            throw new ValidacionException("El stock fisico no puede ser un numero negativo.");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new ValidacionException("Debe especificar la justificacion o motivo del ajuste de inventario.");
        }

        return TransaccionManager.ejecutarEnTransaccion(conn -> {
            Producto p = inventarioDAO.buscarProductoPorId(productoId)
                    .orElseThrow(() -> new ValidacionException("Producto no encontrado con ID: " + productoId));

            int stockAnterior = p.getCantidadStock();
            if (stockAnterior == nuevoStockFisico) {
                LoggerManager.info(MODULO, "Ajuste omitido: el stock fisico coincide con el sistema (" + nuevoStockFisico + ").");
                return true;
            }

            int diferencia = Math.abs(nuevoStockFisico - stockAnterior);
            inventarioDAO.actualizarStock(productoId, nuevoStockFisico, conn);

            MovimientoInventario mov = new MovimientoInventario(
                    null,
                    productoId,
                    TipoMovimientoEnum.AJUSTE,
                    diferencia,
                    stockAnterior,
                    nuevoStockFisico,
                    motivo.trim(),
                    null,
                    null,
                    usuario != null ? usuario : "Auditoria",
                    null
            );
            inventarioDAO.registrarMovimiento(mov, conn);

            LoggerManager.info(MODULO, String.format("Ajuste fisico aplicado para producto ID %d: %d -> %d unidades.",
                    productoId, stockAnterior, nuevoStockFisico));
            return true;
        });
    }

    // Valida que un producto se encuentre apto para su dispensacion medica
    public Producto validarDisponibilidadParaUso(int productoId, int cantidadRequerida) {
        Producto p = inventarioDAO.buscarProductoPorId(productoId)
                .orElseThrow(() -> new ValidacionException("Producto no encontrado con ID: " + productoId));

        if (!p.isActivo()) {
            throw new InventarioException("El producto '" + p.getNombreProducto() + "' esta inactivo en el catalogo.");
        }
        if (p.isVencido()) {
            throw new InventarioException(String.format(
                    "El producto '%s' (Lote: %s) se encuentra VENCIDO desde el %s y no puede ser administrado a pacientes.",
                    p.getNombreProducto(), p.getLote(), p.getFechaVencimiento()));
        }
        if (p.getCantidadStock() < cantidadRequerida) {
            throw new InventarioException(String.format(
                    "Stock insuficiente para '%s'. Solicitado: %d, Disponible: %d",
                    p.getNombreProducto(), cantidadRequerida, p.getCantidadStock()));
        }

        return p;
    }

    // ==========================================
    // CONSULTAS
    // ==========================================

    public Optional<Producto> obtenerProductoPorId(int id) {
        if (id <= 0) {
            throw new ValidacionException("El ID del producto debe ser mayor a 0.");
        }
        return inventarioDAO.buscarProductoPorId(id);
    }

    public List<Producto> buscarProductosPorNombre(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return listarTodos();
        }
        return inventarioDAO.buscarProductoPorNombre(termino);
    }

    public List<Producto> listarTodos() {
        return inventarioDAO.listarTodos();
    }

    public List<Producto> listarPorTipo(int tipoId) {
        return inventarioDAO.listarPorTipo(tipoId);
    }

    public List<Producto> listarBajoStockMinimo() {
        return inventarioDAO.listarBajoStockMinimo();
    }

    public List<Producto> listarProximosAVencer(int dias) {
        if (dias <= 0) {
            dias = 30;
        }
        return inventarioDAO.listarProximosAVencer(dias);
    }

    public List<Producto> listarVencidos() {
        return inventarioDAO.listarVencidos();
    }

    public List<MovimientoInventario> listarMovimientosPorProducto(int productoId) {
        if (productoId <= 0) {
            throw new ValidacionException("El ID del producto debe ser mayor a 0.");
        }
        return inventarioDAO.listarMovimientosPorProducto(productoId);
    }

    public List<ProductoTipo> listarTiposProducto() {
        return inventarioDAO.listarTiposProducto();
    }

    // ==========================================
    // VALIDACIONES INTERNAS
    // ==========================================

    private void validarProveedor(Proveedor p) {
        if (p == null) {
            throw new ValidacionException("Los datos del proveedor son obligatorios.");
        }
        if (p.getNombreEmpresa() == null || p.getNombreEmpresa().trim().isEmpty()) {
            throw new ValidacionException("El nombre o razon social de la empresa proveedora es obligatorio.");
        }
        if (p.getNombreEmpresa().trim().length() > 255) {
            throw new ValidacionException("El nombre de la empresa no puede superar los 255 caracteres.");
        }
        if (p.getEmail() != null && !p.getEmail().trim().isEmpty()) {
            if (!PATRON_EMAIL.matcher(p.getEmail().trim()).matches()) {
                throw new ValidacionException("El formato del correo electronico del proveedor no es valido.");
            }
        }
        if (p.getTelefono() != null && !p.getTelefono().trim().isEmpty()) {
            if (!PATRON_TELEFONO.matcher(p.getTelefono().trim()).matches()) {
                throw new ValidacionException("El telefono del proveedor contiene caracteres no permitidos.");
            }
        }
    }

    private void validarProducto(Producto p) {
        if (p == null) {
            throw new ValidacionException("Los datos del producto son obligatorios.");
        }
        if (p.getNombreProducto() == null || p.getNombreProducto().trim().isEmpty()) {
            throw new ValidacionException("El nombre del producto o medicamento es obligatorio.");
        }
        if (p.getNombreProducto().trim().length() > 255) {
            throw new ValidacionException("El nombre del producto no puede exceder 255 caracteres.");
        }
        if (p.getProductoTipoId() <= 0) {
            throw new ValidacionException("Debe seleccionar una categoria o tipo de producto valido.");
        }
        if (p.getCantidadStock() < 0) {
            throw new ValidacionException("La cantidad en stock no puede ser negativa.");
        }
        if (p.getStockMinimo() < 0) {
            throw new ValidacionException("El stock de seguridad minimo no puede ser negativo.");
        }
        if (p.getPrecioVenta() == null || p.getPrecioVenta().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionException("El precio de venta debe ser mayor o igual a 0.");
        }
        if (p.getPrecioCompra() != null && p.getPrecioCompra().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionException("El precio de compra no puede ser negativo.");
        }
    }
}
