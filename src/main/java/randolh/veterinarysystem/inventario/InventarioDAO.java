package randolh.veterinarysystem.inventario;

import randolh.veterinarysystem.core.config.ConexionBD;
import randolh.veterinarysystem.core.exception.BaseDatosException;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Acceso a datos para catalogo de farmacia, proveedores y kardex de inventario
public class InventarioDAO {

    private static final String MODULO = "InventarioDAO";

    private static final String SELECT_PRODUCTO_BASE =
            "SELECT i.id, i.codigo_barras, i.nombre_producto, i.tipo_id AS producto_tipo_id, i.descripcion, i.fabricante, " +
            "i.proveedor_id, i.lote, i.cantidad_stock, i.stock_minimo, i.unidad_medida, " +
            "i.fecha_vencimiento, i.precio_compra, i.precio_venta, i.requiere_receta, " +
            "i.activo, i.fecha_registro, " +
            "pt.nombre AS tipo_nombre, pr.nombre_empresa AS proveedor_nombre " +
            "FROM inventario i " +
            "INNER JOIN producto_tipos pt ON i.tipo_id = pt.id " +
            "LEFT JOIN proveedores pr ON i.proveedor_id = pr.id ";

    // ==========================================
    // OPERACIONES DE PROVEEDORES
    // ==========================================

    // Inserta un nuevo proveedor y obtiene el ID autoincremental
    public Proveedor insertarProveedor(Proveedor p) {
        String rucNit = "NIT-" + (System.currentTimeMillis() % 1000000000);
        String sql = "INSERT INTO proveedores (nombre_empresa, ruc_nit, contacto_nombre, telefono, email, direccion, sitio_web, activo, fecha_registro) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, p.getNombreEmpresa());
            stmt.setString(2, rucNit);
            stmt.setString(3, p.getContacto());
            stmt.setString(4, p.getTelefono());
            stmt.setString(5, p.getEmail());
            stmt.setString(6, p.getDireccion());
            stmt.setString(7, p.getSitioWeb());
            stmt.setBoolean(8, p.isActivo());
            stmt.setTimestamp(9, Timestamp.valueOf(p.getFechaRegistro()));

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }
            LoggerManager.info(MODULO, "Proveedor registrado con ID: " + p.getId());
            return p;

        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al insertar proveedor: " + e.getMessage(), e);
            throw new BaseDatosException("Error al registrar proveedor: " + e.getMessage(), e);
        }
    }

    // Actualiza datos de un proveedor
    public boolean actualizarProveedor(Proveedor p) {
        String sql = "UPDATE proveedores SET nombre_empresa = ?, contacto_nombre = ?, telefono = ?, " +
                "email = ?, direccion = ?, sitio_web = ?, activo = ? WHERE id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getNombreEmpresa());
            stmt.setString(2, p.getContacto());
            stmt.setString(3, p.getTelefono());
            stmt.setString(4, p.getEmail());
            stmt.setString(5, p.getDireccion());
            stmt.setString(6, p.getSitioWeb());
            stmt.setBoolean(7, p.isActivo());
            stmt.setInt(8, p.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al actualizar proveedor ID: " + p.getId(), e);
            throw new BaseDatosException("Error al actualizar proveedor: " + e.getMessage(), e);
        }
    }

    // Desactiva logicamente un proveedor
    public boolean eliminarLogicoProveedor(int id) {
        String sql = "UPDATE proveedores SET activo = FALSE WHERE id = ?";
        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al desactivar proveedor ID: " + id, e);
            throw new BaseDatosException("Error al desactivar proveedor: " + e.getMessage(), e);
        }
    }

    // Busca proveedor por ID
    public Optional<Proveedor> buscarProveedorPorId(int id) {
        String sql = "SELECT id, nombre_empresa, contacto_nombre AS contacto, telefono, email, direccion, sitio_web, activo, fecha_registro " +
                "FROM proveedores WHERE id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearProveedor(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar proveedor por ID: " + id, e);
            throw new BaseDatosException("Error al consultar proveedor: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Busca proveedor por nombre de empresa
    public Optional<Proveedor> buscarProveedorPorNombre(String nombre) {
        String sql = "SELECT id, nombre_empresa, contacto_nombre AS contacto, telefono, email, direccion, sitio_web, activo, fecha_registro " +
                "FROM proveedores WHERE LOWER(nombre_empresa) = LOWER(?)";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearProveedor(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar proveedor por nombre: " + nombre, e);
            throw new BaseDatosException("Error al consultar proveedor: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Lista todos los proveedores
    public List<Proveedor> listarProveedores() {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT id, nombre_empresa, contacto_nombre AS contacto, telefono, email, direccion, sitio_web, activo, fecha_registro " +
                "FROM proveedores ORDER BY nombre_empresa ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearProveedor(rs));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar proveedores", e);
            throw new BaseDatosException("Error al consultar proveedores: " + e.getMessage(), e);
        }
        return lista;
    }

    // ==========================================
    // OPERACIONES DE TIPOS DE PRODUCTO
    // ==========================================

    // Lista todos los tipos de producto
    public List<ProductoTipo> listarTiposProducto() {
        List<ProductoTipo> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion FROM producto_tipos ORDER BY id ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new ProductoTipo(rs.getInt("id"), rs.getString("nombre"), rs.getString("descripcion")));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar tipos de producto", e);
            throw new BaseDatosException("Error al consultar tipos de producto: " + e.getMessage(), e);
        }
        return lista;
    }

    // Busca tipo por ID
    public Optional<ProductoTipo> buscarTipoPorId(int id) {
        String sql = "SELECT id, nombre, descripcion FROM producto_tipos WHERE id = ?";
        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new ProductoTipo(rs.getInt("id"), rs.getString("nombre"), rs.getString("descripcion")));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar tipo de producto ID: " + id, e);
            throw new BaseDatosException("Error al consultar tipo de producto: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // ==========================================
    // OPERACIONES DE PRODUCTOS E INVENTARIO
    // ==========================================

    // Inserta un producto y obtiene el ID autoincremental
    public Producto insertarProducto(Producto p) {
        String codigoBarras = "BAR-" + (System.currentTimeMillis() % 1000000000);
        String sql = "INSERT INTO inventario (codigo_barras, nombre_producto, tipo_id, descripcion, fabricante, " +
                "proveedor_id, lote, cantidad_stock, stock_minimo, unidad_medida, fecha_vencimiento, " +
                "precio_compra, precio_venta, requiere_receta, activo, fecha_registro) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, codigoBarras);
            stmt.setString(2, p.getNombreProducto());
            stmt.setInt(3, p.getProductoTipoId());
            stmt.setString(4, p.getDescripcion());
            stmt.setString(5, p.getFabricante());

            if (p.getProveedorId() != null) {
                stmt.setInt(6, p.getProveedorId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.setString(7, p.getLote());
            stmt.setInt(8, p.getCantidadStock());
            stmt.setInt(9, p.getStockMinimo());
            stmt.setString(10, p.getUnidadMedida());

            if (p.getFechaVencimiento() != null) {
                stmt.setDate(11, Date.valueOf(p.getFechaVencimiento()));
            } else {
                stmt.setNull(11, Types.DATE);
            }

            stmt.setBigDecimal(12, p.getPrecioCompra());
            stmt.setBigDecimal(13, p.getPrecioVenta());
            stmt.setBoolean(14, p.isRequiereReceta());
            stmt.setBoolean(15, p.isActivo());
            stmt.setTimestamp(16, Timestamp.valueOf(p.getFechaRegistro()));

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }
            LoggerManager.info(MODULO, "Producto registrado con ID: " + p.getId());
            return p;

        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al insertar producto: " + e.getMessage(), e);
            throw new BaseDatosException("Error al registrar producto en inventario: " + e.getMessage(), e);
        }
    }

    // Actualiza datos generales del producto
    public boolean actualizarProducto(Producto p) {
        String sql = "UPDATE inventario SET nombre_producto = ?, tipo_id = ?, descripcion = ?, " +
                "fabricante = ?, proveedor_id = ?, lote = ?, stock_minimo = ?, unidad_medida = ?, " +
                "fecha_vencimiento = ?, precio_compra = ?, precio_venta = ?, requiere_receta = ?, activo = ? " +
                "WHERE id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getNombreProducto());
            stmt.setInt(2, p.getProductoTipoId());
            stmt.setString(3, p.getDescripcion());
            stmt.setString(4, p.getFabricante());

            if (p.getProveedorId() != null) {
                stmt.setInt(5, p.getProveedorId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setString(6, p.getLote());
            stmt.setInt(7, p.getStockMinimo());
            stmt.setString(8, p.getUnidadMedida());

            if (p.getFechaVencimiento() != null) {
                stmt.setDate(9, Date.valueOf(p.getFechaVencimiento()));
            } else {
                stmt.setNull(9, Types.DATE);
            }

            stmt.setBigDecimal(10, p.getPrecioCompra());
            stmt.setBigDecimal(11, p.getPrecioVenta());
            stmt.setBoolean(12, p.isRequiereReceta());
            stmt.setBoolean(13, p.isActivo());
            stmt.setInt(14, p.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al actualizar producto ID: " + p.getId(), e);
            throw new BaseDatosException("Error al actualizar producto: " + e.getMessage(), e);
        }
    }

    // Actualiza cantidad fisica en stock
    public boolean actualizarStock(int productoId, int nuevoStock, Connection conn) throws SQLException {
        String sql = "UPDATE inventario SET cantidad_stock = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nuevoStock);
            stmt.setInt(2, productoId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean actualizarStock(int productoId, int nuevoStock) {
        Connection conn = ConexionBD.getInstancia().getConexion();
        try {
            return actualizarStock(productoId, nuevoStock, conn);
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al actualizar stock de producto ID: " + productoId, e);
            throw new BaseDatosException("Error al actualizar stock: " + e.getMessage(), e);
        }
    }

    // Actualiza lote y fecha de vencimiento al recibir nuevo lote
    public boolean actualizarLoteYVencimiento(int productoId, String nuevoLote, LocalDate nuevoVencimiento, Connection conn) throws SQLException {
        String sql = "UPDATE inventario SET lote = ?, fecha_vencimiento = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nuevoLote);
            if (nuevoVencimiento != null) {
                stmt.setDate(2, Date.valueOf(nuevoVencimiento));
            } else {
                stmt.setNull(2, Types.DATE);
            }
            stmt.setInt(3, productoId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Desactivacion logica de producto
    public boolean eliminarLogicoProducto(int id) {
        String sql = "UPDATE inventario SET activo = FALSE WHERE id = ?";
        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al desactivar producto ID: " + id, e);
            throw new BaseDatosException("Error al desactivar producto: " + e.getMessage(), e);
        }
    }

    // Busca producto por ID
    public Optional<Producto> buscarProductoPorId(int id) {
        String sql = SELECT_PRODUCTO_BASE + "WHERE i.id = ?";
        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearProducto(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar producto por ID: " + id, e);
            throw new BaseDatosException("Error al consultar producto: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Busca productos por coincidencia de nombre
    public List<Producto> buscarProductoPorNombre(String termino) {
        List<Producto> lista = new ArrayList<>();
        String sql = SELECT_PRODUCTO_BASE + "WHERE LOWER(i.nombre_producto) LIKE LOWER(?) ORDER BY i.nombre_producto ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + termino.trim() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProducto(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al buscar productos por nombre: " + termino, e);
            throw new BaseDatosException("Error al buscar productos: " + e.getMessage(), e);
        }
        return lista;
    }

    // Lista todos los articulos de inventario
    public List<Producto> listarTodos() {
        List<Producto> lista = new ArrayList<>();
        String sql = SELECT_PRODUCTO_BASE + "ORDER BY i.nombre_producto ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar catalogo general", e);
            throw new BaseDatosException("Error al consultar catalogo: " + e.getMessage(), e);
        }
        return lista;
    }

    // Filtra productos por categoria
    public List<Producto> listarPorTipo(int tipoId) {
        List<Producto> lista = new ArrayList<>();
        String sql = SELECT_PRODUCTO_BASE + "WHERE i.tipo_id = ? ORDER BY i.nombre_producto ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tipoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProducto(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar productos por tipo: " + tipoId, e);
            throw new BaseDatosException("Error al consultar productos por categoria: " + e.getMessage(), e);
        }
        return lista;
    }

    // Lista productos en estado critico (stock <= stock_minimo)
    public List<Producto> listarBajoStockMinimo() {
        List<Producto> lista = new ArrayList<>();
        String sql = SELECT_PRODUCTO_BASE + "WHERE i.activo = TRUE AND i.cantidad_stock <= i.stock_minimo ORDER BY i.cantidad_stock ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar productos con bajo stock", e);
            throw new BaseDatosException("Error al consultar reporte de stock minimo: " + e.getMessage(), e);
        }
        return lista;
    }

    // Lista productos proximos a caducar en los siguientes N dias
    public List<Producto> listarProximosAVencer(int dias) {
        List<Producto> lista = new ArrayList<>();
        String sql = SELECT_PRODUCTO_BASE +
                "WHERE i.activo = TRUE AND i.fecha_vencimiento IS NOT NULL " +
                "  AND i.fecha_vencimiento >= CURDATE() " +
                "  AND i.fecha_vencimiento <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                "ORDER BY i.fecha_vencimiento ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dias);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProducto(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar productos proximos a vencer en " + dias + " dias", e);
            throw new BaseDatosException("Error al consultar vencimientos proximos: " + e.getMessage(), e);
        }
        return lista;
    }

    // Lista productos ya vencidos a la fecha
    public List<Producto> listarVencidos() {
        List<Producto> lista = new ArrayList<>();
        String sql = SELECT_PRODUCTO_BASE +
                "WHERE i.activo = TRUE AND i.fecha_vencimiento IS NOT NULL " +
                "  AND i.fecha_vencimiento < CURDATE() " +
                "ORDER BY i.fecha_vencimiento ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar productos vencidos", e);
            throw new BaseDatosException("Error al consultar productos vencidos: " + e.getMessage(), e);
        }
        return lista;
    }

    // ==========================================
    // OPERACIONES DE KARDEX Y MOVIMIENTOS
    // ==========================================

    // Registra un movimiento dentro de una conexion transaccional
    public MovimientoInventario registrarMovimiento(MovimientoInventario m, Connection conn) throws SQLException {
        String sql = "INSERT INTO inventario_movimientos (producto_id, tipo_movimiento, cantidad, " +
                "stock_anterior, stock_nuevo, motivo, referencia_consulta_id, referencia_procedimiento_id, " +
                "usuario, fecha_hora) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, m.getProductoId());
            stmt.setString(2, m.getTipoMovimiento().getValorDb());
            stmt.setInt(3, m.getCantidad());
            stmt.setInt(4, m.getStockAnterior());
            stmt.setInt(5, m.getStockNuevo());
            stmt.setString(6, m.getMotivo());

            if (m.getReferenciaConsultaId() != null) {
                stmt.setInt(7, m.getReferenciaConsultaId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            if (m.getReferenciaProcedimientoId() != null) {
                stmt.setInt(8, m.getReferenciaProcedimientoId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            stmt.setString(9, m.getUsuario());
            stmt.setTimestamp(10, Timestamp.valueOf(m.getFechaMovimiento()));

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    m.setId(rs.getInt(1));
                }
            }
            return m;
        }
    }

    public MovimientoInventario registrarMovimiento(MovimientoInventario m) {
        Connection conn = ConexionBD.getInstancia().getConexion();
        try {
            return registrarMovimiento(m, conn);
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al registrar movimiento de kardex", e);
            throw new BaseDatosException("Error al registrar movimiento: " + e.getMessage(), e);
        }
    }

    // Lista el historial de movimientos de un producto
    public List<MovimientoInventario> listarMovimientosPorProducto(int productoId) {
        List<MovimientoInventario> lista = new ArrayList<>();
        String sql = "SELECT m.id, m.producto_id, m.tipo_movimiento, m.cantidad, m.stock_anterior, " +
                "m.stock_nuevo, m.motivo, m.referencia_consulta_id, m.referencia_procedimiento_id, " +
                "m.usuario, m.fecha_hora AS fecha_movimiento, i.nombre_producto " +
                "FROM inventario_movimientos m " +
                "INNER JOIN inventario i ON m.producto_id = i.id " +
                "WHERE m.producto_id = ? " +
                "ORDER BY m.fecha_hora DESC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearMovimiento(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar movimientos de producto ID: " + productoId, e);
            throw new BaseDatosException("Error al consultar kardex: " + e.getMessage(), e);
        }
        return lista;
    }

    // ==========================================
    // MAPEO DE RESULTSETS A ENTIDADES
    // ==========================================

    private Proveedor mapearProveedor(ResultSet rs) throws SQLException {
        Timestamp reg = rs.getTimestamp("fecha_registro");
        return new Proveedor(
                rs.getInt("id"),
                rs.getString("nombre_empresa"),
                rs.getString("contacto"),
                rs.getString("telefono"),
                rs.getString("email"),
                rs.getString("direccion"),
                rs.getString("sitio_web"),
                rs.getBoolean("activo"),
                reg != null ? reg.toLocalDateTime() : null
        );
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Date fVenc = rs.getDate("fecha_vencimiento");
        Timestamp fReg = rs.getTimestamp("fecha_registro");
        int provId = rs.getInt("proveedor_id");
        Integer provIdObj = rs.wasNull() ? null : provId;

        Producto p = new Producto(
                rs.getInt("id"),
                rs.getString("nombre_producto"),
                rs.getInt("producto_tipo_id"),
                rs.getString("descripcion"),
                rs.getString("fabricante"),
                provIdObj,
                rs.getString("lote"),
                rs.getInt("cantidad_stock"),
                rs.getInt("stock_minimo"),
                rs.getString("unidad_medida"),
                fVenc != null ? fVenc.toLocalDate() : null,
                rs.getBigDecimal("precio_compra"),
                rs.getBigDecimal("precio_venta"),
                rs.getBoolean("requiere_receta"),
                rs.getBoolean("activo"),
                fReg != null ? fReg.toLocalDateTime() : null
        );

        p.setTipoNombre(rs.getString("tipo_nombre"));
        p.setProveedorNombre(rs.getString("proveedor_nombre"));
        return p;
    }

    private MovimientoInventario mapearMovimiento(ResultSet rs) throws SQLException {
        Timestamp fMov = rs.getTimestamp("fecha_movimiento");
        int refCons = rs.getInt("referencia_consulta_id");
        Integer refConsObj = rs.wasNull() ? null : refCons;
        int refProc = rs.getInt("referencia_procedimiento_id");
        Integer refProcObj = rs.wasNull() ? null : refProc;

        MovimientoInventario m = new MovimientoInventario(
                rs.getInt("id"),
                rs.getInt("producto_id"),
                TipoMovimientoEnum.desdeString(rs.getString("tipo_movimiento")),
                rs.getInt("cantidad"),
                rs.getInt("stock_anterior"),
                rs.getInt("stock_nuevo"),
                rs.getString("motivo"),
                refConsObj,
                refProcObj,
                rs.getString("usuario"),
                fMov != null ? fMov.toLocalDateTime() : null
        );
        m.setNombreProducto(rs.getString("nombre_producto"));
        return m;
    }
}
