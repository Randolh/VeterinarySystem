package randolh.veterinarysystem.facturacion;

import randolh.veterinarysystem.core.config.ConexionBD;
import randolh.veterinarysystem.core.config.TransaccionManager;
import randolh.veterinarysystem.core.exception.BaseDatosException;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Acceso a datos para facturacion, detalles de items, transacciones de puntos y catalogo de servicios
public class FacturacionDAO {

    private static final String MODULO = "FacturacionDAO";

    // Informacion del club de fidelizacion asociada a un dueno
    public static class InfoClubDueno {
        private final int clubId;
        private final String nivel;
        private final int puntosAcumulados;
        private final int puntosDisponibles;
        private final boolean activo;

        public InfoClubDueno(int clubId, String nivel, int puntosAcumulados, int puntosDisponibles, boolean activo) {
            this.clubId = clubId;
            this.nivel = nivel;
            this.puntosAcumulados = puntosAcumulados;
            this.puntosDisponibles = puntosDisponibles;
            this.activo = activo;
        }

        public int getClubId() { return clubId; }
        public String getNivel() { return nivel; }
        public int getPuntosAcumulados() { return puntosAcumulados; }
        public int getPuntosDisponibles() { return puntosDisponibles; }
        public boolean isActivo() { return activo; }
    }

    // Registro liviano del catalogo de servicios clinicos
    public static class ServicioCatalogo {
        private final int id;
        private final String nombre;
        private final String categoria;
        private final BigDecimal precioBase;

        public ServicioCatalogo(int id, String nombre, String categoria, BigDecimal precioBase) {
            this.id = id;
            this.nombre = nombre;
            this.categoria = categoria;
            this.precioBase = precioBase;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getCategoria() { return categoria; }
        public BigDecimal getPrecioBase() { return precioBase; }

        @Override
        public String toString() {
            return String.format("[%d] %s (%s) - $%.2f", id, nombre, categoria != null ? categoria : "General", precioBase);
        }
    }

    // Genera el proximo numero correlativo de factura con formato FAC-YYYY-XXXX
    public String generarSiguienteNumeroFactura() {
        int anioActual = LocalDateTime.now().getYear();
        String prefijo = "FAC-" + anioActual + "-";
        String sql = "SELECT numero_factura FROM facturas WHERE numero_factura LIKE ? ORDER BY id DESC LIMIT 1";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, prefijo + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String ultimoNumero = rs.getString("numero_factura");
                    String[] partes = ultimoNumero.split("-");
                    if (partes.length >= 3) {
                        try {
                            int consecutivo = Integer.parseInt(partes[2]) + 1;
                            return String.format("%s%04d", prefijo, consecutivo);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al consultar consecutivo de factura: " + e.getMessage(), e);
        }
        return prefijo + "0001";
    }

    // Consulta la membresia activa del club para un cliente
    public Optional<InfoClubDueno> obtenerInfoClubDueno(int duenoId) {
        String sql = "SELECT id, nivel, puntos_acumulados, puntos_disponibles, activo " +
                     "FROM club_mascotas WHERE dueno_id = ? AND activo = TRUE";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, duenoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    InfoClubDueno info = new InfoClubDueno(
                            rs.getInt("id"),
                            rs.getString("nivel"),
                            rs.getInt("puntos_acumulados"),
                            rs.getInt("puntos_disponibles"),
                            rs.getBoolean("activo")
                    );
                    return Optional.of(info);
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al consultar membresia del club para dueno ID " + duenoId, e);
        }
        return Optional.empty();
    }

    // Inserta la factura, sus lineas de detalle y acumula puntos de fidelizacion de manera transaccional ACID
    public Factura insertarFacturaTransaccional(Factura factura, int puntosGanados) {
        return TransaccionManager.ejecutarEnTransaccion(conn -> {
            // 1. Insertar cabecera de factura
            String sqlFactura = "INSERT INTO facturas (numero_factura, dueno_id, fecha_emision, subtotal, " +
                    "descuento, impuesto, total, metodo_pago, estado, observaciones) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmtFac = conn.prepareStatement(sqlFactura, Statement.RETURN_GENERATED_KEYS)) {
                stmtFac.setString(1, factura.getNumeroFactura());
                stmtFac.setInt(2, factura.getDuenoId());
                stmtFac.setTimestamp(3, Timestamp.valueOf(factura.getFechaEmision()));
                stmtFac.setBigDecimal(4, factura.getSubtotal());
                stmtFac.setBigDecimal(5, factura.getDescuento());
                stmtFac.setBigDecimal(6, factura.getImpuesto());
                stmtFac.setBigDecimal(7, factura.getTotal());
                stmtFac.setString(8, factura.getMetodoPago().getValor());
                stmtFac.setString(9, factura.getEstado().getValor());
                if (factura.getObservaciones() != null && !factura.getObservaciones().isEmpty()) {
                    stmtFac.setString(10, factura.getObservaciones());
                } else {
                    stmtFac.setNull(10, Types.VARCHAR);
                }

                int filas = stmtFac.executeUpdate();
                if (filas == 0) {
                    throw new BaseDatosException("No fue posible registrar la cabecera de la factura.");
                }

                try (ResultSet rsClaves = stmtFac.getGeneratedKeys()) {
                    if (rsClaves.next()) {
                        factura.setId(rsClaves.getInt(1));
                    }
                }
            }

            // 2. Insertar cada renglon o item facturado
            String sqlDetalle = "INSERT INTO factura_detalles (factura_id, tipo_item, producto_id, servicio_id, " +
                    "referencia_id, descripcion, cantidad, precio_unitario, subtotal) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            String sqlDescontarStock = "UPDATE inventario SET cantidad_stock = cantidad_stock - ? " +
                    "WHERE id = ? AND cantidad_stock >= ?";

            try (PreparedStatement stmtDet = conn.prepareStatement(sqlDetalle, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement stmtStock = conn.prepareStatement(sqlDescontarStock)) {

                for (ItemFactura it : factura.getItems()) {
                    it.setFacturaId(factura.getId());
                    stmtDet.setInt(1, factura.getId());
                    stmtDet.setString(2, it.getTipoItem().getValor());

                    if (it.getTipoItem() == TipoItemEnum.PRODUCTO) {
                        stmtDet.setInt(3, it.getProductoId());
                        stmtDet.setNull(4, Types.INTEGER);
                        stmtDet.setInt(5, it.getProductoId());

                        // Deduccion inmediata de stock para productos fisicos
                        stmtStock.setInt(1, it.getCantidad());
                        stmtStock.setInt(2, it.getProductoId());
                        stmtStock.setInt(3, it.getCantidad());
                        int stockAfectado = stmtStock.executeUpdate();
                        if (stockAfectado == 0) {
                            LoggerManager.warning(MODULO, "Stock insuficiente o no disponible para producto ID: " + it.getProductoId());
                        }
                    } else {
                        stmtDet.setNull(3, Types.INTEGER);
                        int srvId = (it.getServicioId() != null && it.getServicioId() > 0) ? it.getServicioId() : 1;
                        stmtDet.setInt(4, srvId);
                        stmtDet.setInt(5, srvId);
                    }

                    stmtDet.setString(6, it.getNombreItem());
                    stmtDet.setInt(7, it.getCantidad());
                    stmtDet.setBigDecimal(8, it.getPrecioUnitario());
                    stmtDet.setBigDecimal(9, it.getSubtotal());

                    stmtDet.executeUpdate();
                    try (ResultSet rsDetId = stmtDet.getGeneratedKeys()) {
                        if (rsDetId.next()) {
                            it.setId(rsDetId.getInt(1));
                        }
                    }
                }
            }

            // 3. Registrar acumulacion de puntos en el club si el dueno es miembro activo
            if (puntosGanados > 0) {
                String sqlBuscarClub = "SELECT id, puntos_acumulados, puntos_disponibles FROM club_mascotas " +
                        "WHERE dueno_id = ? AND activo = TRUE FOR UPDATE";

                try (PreparedStatement stmtClub = conn.prepareStatement(sqlBuscarClub)) {
                    stmtClub.setInt(1, factura.getDuenoId());
                    try (ResultSet rsClub = stmtClub.executeQuery()) {
                        if (rsClub.next()) {
                            int clubId = rsClub.getInt("id");
                            int puntosDisponiblesActual = rsClub.getInt("puntos_disponibles");
                            int saldoNuevo = puntosDisponiblesActual + puntosGanados;

                            // Actualizar saldo de puntos en club_mascotas
                            String sqlUpdClub = "UPDATE club_mascotas SET puntos_acumulados = puntos_acumulados + ?, " +
                                    "puntos_disponibles = puntos_disponibles + ? WHERE id = ?";
                            try (PreparedStatement stmtUpd = conn.prepareStatement(sqlUpdClub)) {
                                stmtUpd.setInt(1, puntosGanados);
                                stmtUpd.setInt(2, puntosGanados);
                                stmtUpd.setInt(3, clubId);
                                stmtUpd.executeUpdate();
                            }

                            // Registrar transaccion de puntos para auditoria
                            String sqlTrans = "INSERT INTO transacciones_puntos (club_mascotas_id, factura_id, puntos, " +
                                    "tipo, fecha, descripcion, saldo_anterior, saldo_nuevo) " +
                                    "VALUES (?, ?, ?, 'Ganados', NOW(), ?, ?, ?)";
                            try (PreparedStatement stmtTr = conn.prepareStatement(sqlTrans)) {
                                stmtTr.setInt(1, clubId);
                                stmtTr.setInt(2, factura.getId());
                                stmtTr.setInt(3, puntosGanados);
                                stmtTr.setString(4, "Puntos obtenidos por Factura " + factura.getNumeroFactura());
                                stmtTr.setInt(5, puntosDisponiblesActual);
                                stmtTr.setInt(6, saldoNuevo);
                                stmtTr.executeUpdate();
                            }

                            factura.setPuntosGanados(puntosGanados);
                            LoggerManager.info(MODULO, "Puntos club registrados: +" + puntosGanados + " pts para club ID " + clubId);
                        }
                    }
                }
            }

            LoggerManager.info(MODULO, "Factura " + factura.getNumeroFactura() + " insertada exitosamente con ID " + factura.getId());
            return factura;
        });
    }

    // Busca una factura completa por su ID primario
    public Optional<Factura> buscarPorId(int id) {
        String sql = "SELECT f.id, f.numero_factura, f.dueno_id, f.fecha_emision, f.subtotal, f.descuento, " +
                "f.impuesto, f.total, f.metodo_pago, f.estado, f.observaciones, " +
                "d.nombre_completo AS dueno_nombre, d.numero_documento AS dueno_documento, " +
                "d.email AS dueno_email, d.telefono AS dueno_telefono, d.direccion AS dueno_direccion, " +
                "cm.nivel AS club_nivel " +
                "FROM facturas f " +
                "INNER JOIN duenos d ON f.dueno_id = d.id " +
                "LEFT JOIN club_mascotas cm ON cm.dueno_id = d.id AND cm.activo = TRUE " +
                "WHERE f.id = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Factura f = mapearFactura(rs);
                    f.setItems(cargarDetallesFactura(f.getId()));
                    return Optional.of(f);
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al consultar factura ID " + id, e);
            throw new BaseDatosException("Error al consultar factura: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Busca una factura por su correlativo unico
    public Optional<Factura> buscarPorNumero(String numeroFactura) {
        String sql = "SELECT f.id, f.numero_factura, f.dueno_id, f.fecha_emision, f.subtotal, f.descuento, " +
                "f.impuesto, f.total, f.metodo_pago, f.estado, f.observaciones, " +
                "d.nombre_completo AS dueno_nombre, d.numero_documento AS dueno_documento, " +
                "d.email AS dueno_email, d.telefono AS dueno_telefono, d.direccion AS dueno_direccion, " +
                "cm.nivel AS club_nivel " +
                "FROM facturas f " +
                "INNER JOIN duenos d ON f.dueno_id = d.id " +
                "LEFT JOIN club_mascotas cm ON cm.dueno_id = d.id AND cm.activo = TRUE " +
                "WHERE f.numero_factura = ?";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, numeroFactura);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Factura f = mapearFactura(rs);
                    f.setItems(cargarDetallesFactura(f.getId()));
                    return Optional.of(f);
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al consultar factura por numero " + numeroFactura, e);
            throw new BaseDatosException("Error al consultar factura: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Lista todas las facturas emitidas ordenadas descendentemente
    public List<Factura> listarTodas() {
        List<Factura> lista = new ArrayList<>();
        String sql = "SELECT f.id, f.numero_factura, f.dueno_id, f.fecha_emision, f.subtotal, f.descuento, " +
                "f.impuesto, f.total, f.metodo_pago, f.estado, f.observaciones, " +
                "d.nombre_completo AS dueno_nombre, d.numero_documento AS dueno_documento, " +
                "d.email AS dueno_email, d.telefono AS dueno_telefono, d.direccion AS dueno_direccion, " +
                "cm.nivel AS club_nivel " +
                "FROM facturas f " +
                "INNER JOIN duenos d ON f.dueno_id = d.id " +
                "LEFT JOIN club_mascotas cm ON cm.dueno_id = d.id AND cm.activo = TRUE " +
                "ORDER BY f.id DESC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearFactura(rs));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar facturas", e);
            throw new BaseDatosException("Error al listar facturas: " + e.getMessage(), e);
        }
        return lista;
    }

    // Lista facturas correspondientes a un dueno especifico
    public List<Factura> listarPorDueno(int duenoId) {
        List<Factura> lista = new ArrayList<>();
        String sql = "SELECT f.id, f.numero_factura, f.dueno_id, f.fecha_emision, f.subtotal, f.descuento, " +
                "f.impuesto, f.total, f.metodo_pago, f.estado, f.observaciones, " +
                "d.nombre_completo AS dueno_nombre, d.numero_documento AS dueno_documento, " +
                "d.email AS dueno_email, d.telefono AS dueno_telefono, d.direccion AS dueno_direccion, " +
                "cm.nivel AS club_nivel " +
                "FROM facturas f " +
                "INNER JOIN duenos d ON f.dueno_id = d.id " +
                "LEFT JOIN club_mascotas cm ON cm.dueno_id = d.id AND cm.activo = TRUE " +
                "WHERE f.dueno_id = ? " +
                "ORDER BY f.id DESC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, duenoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFactura(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al listar facturas de dueno ID " + duenoId, e);
            throw new BaseDatosException("Error al listar facturas por dueno: " + e.getMessage(), e);
        }
        return lista;
    }

    // Anula una factura emitida cambiando su estado
    public boolean anularFactura(int id) {
        String sql = "UPDATE facturas SET estado = 'Anulada' WHERE id = ?";
        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al anular factura ID " + id, e);
            throw new BaseDatosException("Error al anular factura: " + e.getMessage(), e);
        }
    }

    // Consulta el catalogo de servicios clinicos vigentes
    public List<ServicioCatalogo> listarCatalogoServicios() {
        List<ServicioCatalogo> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, categoria, precio_base FROM servicios WHERE activo = TRUE ORDER BY nombre ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new ServicioCatalogo(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("categoria"),
                        rs.getBigDecimal("precio_base")
                ));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al consultar catalogo de servicios", e);
        }
        return lista;
    }

    // Carga las lineas de detalle de una factura
    public List<ItemFactura> cargarDetallesFactura(int facturaId) {
        List<ItemFactura> items = new ArrayList<>();
        String sql = "SELECT fd.id, fd.factura_id, fd.tipo_item, fd.producto_id, fd.servicio_id, " +
                "fd.descripcion, fd.cantidad, fd.precio_unitario, fd.subtotal, " +
                "i.nombre_producto, s.nombre AS servicio_nombre " +
                "FROM factura_detalles fd " +
                "LEFT JOIN inventario i ON fd.producto_id = i.id " +
                "LEFT JOIN servicios s ON fd.servicio_id = s.id " +
                "WHERE fd.factura_id = ? " +
                "ORDER BY fd.id ASC";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, facturaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TipoItemEnum tipo = TipoItemEnum.desdeTexto(rs.getString("tipo_item"));
                    Integer prodId = rs.getObject("producto_id") != null ? rs.getInt("producto_id") : null;
                    Integer servId = rs.getObject("servicio_id") != null ? rs.getInt("servicio_id") : null;
                    String desc = rs.getString("descripcion");

                    ItemFactura item = new ItemFactura(
                            rs.getInt("id"),
                            rs.getInt("factura_id"),
                            tipo,
                            prodId,
                            servId,
                            desc,
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario"),
                            rs.getBigDecimal("subtotal")
                    );

                    String nombre = desc;
                    if (tipo == TipoItemEnum.PRODUCTO && rs.getString("nombre_producto") != null) {
                        nombre = rs.getString("nombre_producto");
                    } else if (tipo == TipoItemEnum.SERVICIO && rs.getString("servicio_nombre") != null) {
                        nombre = rs.getString("servicio_nombre");
                    }
                    item.setNombreItem(nombre);
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error al cargar detalles de factura ID " + facturaId, e);
        }
        return items;
    }

    // Mapea la fila de la cabecera de factura
    private Factura mapearFactura(ResultSet rs) throws SQLException {
        Factura f = new Factura(
                rs.getInt("id"),
                rs.getInt("dueno_id"),
                rs.getString("numero_factura"),
                rs.getTimestamp("fecha_emision").toLocalDateTime(),
                rs.getBigDecimal("subtotal"),
                rs.getBigDecimal("impuesto"),
                rs.getBigDecimal("descuento"),
                rs.getBigDecimal("total"),
                MetodoPagoEnum.desdeTexto(rs.getString("metodo_pago")),
                EstadoFacturaEnum.desdeTexto(rs.getString("estado")),
                rs.getString("observaciones")
        );

        f.setNombreDueno(rs.getString("dueno_nombre"));
        f.setDocumentoDueno(rs.getString("dueno_documento"));
        f.setEmailDueno(rs.getString("dueno_email"));
        f.setTelefonoDueno(rs.getString("dueno_telefono"));
        f.setDireccionDueno(rs.getString("dueno_direccion"));

        String nivel = rs.getString("club_nivel");
        f.setNivelClub(nivel != null ? nivel : "Sin Club");
        return f;
    }
}
