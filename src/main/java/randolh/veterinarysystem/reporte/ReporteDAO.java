package randolh.veterinarysystem.reporte;

import randolh.veterinarysystem.core.config.ConexionBD;
import randolh.veterinarysystem.core.exception.BaseDatosException;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Acceso a datos para consultas analiticas y vistas de inteligencia de negocio
public class ReporteDAO {

    private static final String MODULO = "ReporteDAO";

    // Consulta la vista o agrupacion de servicios y productos mas solicitados
    public List<ServicioSolicitadoDTO> obtenerServiciosMasSolicitados() {
        List<ServicioSolicitadoDTO> lista = new ArrayList<>();
        String sql = "SELECT concepto, tipo_item, total_solicitudes, cantidad_total, ingresos_totales " +
                     "FROM v_servicios_mas_solicitados";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new ServicioSolicitadoDTO(
                        rs.getString("concepto"),
                        rs.getString("tipo_item"),
                        rs.getInt("total_solicitudes"),
                        rs.getInt("cantidad_total"),
                        rs.getDouble("ingresos_totales")
                ));
            }
        } catch (SQLException e) {
            LoggerManager.warning(MODULO, "Vista v_servicios_mas_solicitados no disponible, ejecutando consulta directa: " + e.getMessage());
            return consultarServiciosDirecto();
        }
        return lista;
    }

    // Consulta la vista o calculo de desempeno y productividad de veterinarios
    public List<DesempenoVeterinarioDTO> obtenerDesempenoVeterinarios() {
        List<DesempenoVeterinarioDTO> lista = new ArrayList<>();
        String sql = "SELECT veterinario_id, nombre_completo, licencia_profesional, especialidad, " +
                     "total_consultas, total_procedimientos, total_atenciones, ingresos_generados " +
                     "FROM v_desempeno_veterinarios";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new DesempenoVeterinarioDTO(
                        rs.getInt("veterinario_id"),
                        rs.getString("nombre_completo"),
                        rs.getString("licencia_profesional"),
                        rs.getString("especialidad"),
                        rs.getInt("total_consultas"),
                        rs.getInt("total_procedimientos"),
                        rs.getInt("total_atenciones"),
                        rs.getDouble("ingresos_generados")
                ));
            }
        } catch (SQLException e) {
            LoggerManager.warning(MODULO, "Vista v_desempeno_veterinarios no disponible, ejecutando consulta directa: " + e.getMessage());
            return consultarDesempenoDirecto();
        }
        return lista;
    }

    // Consulta la vista de alertas de inventario bajo y caducidad proxima
    public List<AlertaInventarioCriticaDTO> obtenerAlertasInventarioCriticas() {
        List<AlertaInventarioCriticaDTO> lista = new ArrayList<>();
        String sql = "SELECT producto_id, codigo_barras, nombre_producto, categoria, lote, " +
                     "cantidad_stock, stock_minimo, fecha_vencimiento, dias_para_vencer, estado_alerta " +
                     "FROM v_alertas_inventario_criticas";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new AlertaInventarioCriticaDTO(
                        rs.getInt("producto_id"),
                        rs.getString("codigo_barras"),
                        rs.getString("nombre_producto"),
                        rs.getString("categoria"),
                        rs.getString("lote"),
                        rs.getInt("cantidad_stock"),
                        rs.getInt("stock_minimo"),
                        rs.getDate("fecha_vencimiento") != null ? rs.getDate("fecha_vencimiento").toLocalDate() : null,
                        rs.getInt("dias_para_vencer"),
                        rs.getString("estado_alerta")
                ));
            }
        } catch (SQLException e) {
            LoggerManager.warning(MODULO, "Vista v_alertas_inventario_criticas no disponible, ejecutando consulta directa: " + e.getMessage());
            return consultarAlertasInventarioDirecto();
        }
        return lista;
    }

    // Consulta la vista de resumen mensual de facturacion y totales
    public List<ResumenFacturacionMensualDTO> obtenerResumenFacturacionMensual() {
        List<ResumenFacturacionMensualDTO> lista = new ArrayList<>();
        String sql = "SELECT anio, mes, facturas_emitidas, facturas_pagadas, facturas_pendientes, " +
                     "facturas_anuladas, subtotal_neto, total_descuentos, total_impuestos, total_facturado " +
                     "FROM v_resumen_facturacion_mensual";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new ResumenFacturacionMensualDTO(
                        rs.getInt("anio"),
                        rs.getInt("mes"),
                        rs.getInt("facturas_emitidas"),
                        rs.getInt("facturas_pagadas"),
                        rs.getInt("facturas_pendientes"),
                        rs.getInt("facturas_anuladas"),
                        rs.getDouble("subtotal_neto"),
                        rs.getDouble("total_descuentos"),
                        rs.getDouble("total_impuestos"),
                        rs.getDouble("total_facturado")
                ));
            }
        } catch (SQLException e) {
            LoggerManager.warning(MODULO, "Vista v_resumen_facturacion_mensual no disponible, ejecutando consulta directa: " + e.getMessage());
            return consultarFacturacionMensualDirecto();
        }
        return lista;
    }

    // Consultas de contingencia directa en tablas base si las vistas no estuviesen creadas
    private List<ServicioSolicitadoDTO> consultarServiciosDirecto() {
        List<ServicioSolicitadoDTO> lista = new ArrayList<>();
        String sql = "SELECT fd.descripcion AS concepto, fd.tipo_item, COUNT(fd.id) AS total_solicitudes, " +
                     "SUM(fd.cantidad) AS cantidad_total, SUM(fd.subtotal) AS ingresos_totales " +
                     "FROM factura_detalles fd " +
                     "JOIN facturas f ON fd.factura_id = f.id " +
                     "WHERE f.estado <> 'Anulada' " +
                     "GROUP BY fd.descripcion, fd.tipo_item";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new ServicioSolicitadoDTO(
                        rs.getString("concepto"),
                        rs.getString("tipo_item"),
                        rs.getInt("total_solicitudes"),
                        rs.getInt("cantidad_total"),
                        rs.getDouble("ingresos_totales")
                ));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error en consulta directa de servicios", e);
        }
        return lista;
    }

    private List<DesempenoVeterinarioDTO> consultarDesempenoDirecto() {
        List<DesempenoVeterinarioDTO> lista = new ArrayList<>();
        String sql = "SELECT v.id AS veterinario_id, v.nombre_completo, v.licencia_profesional, v.especialidad, " +
                     "COUNT(DISTINCT c.id) AS total_consultas, COUNT(DISTINCT p.id) AS total_procedimientos, " +
                     "(COUNT(DISTINCT c.id) + COUNT(DISTINCT p.id)) AS total_atenciones, " +
                     "COALESCE(SUM(c.costo_consulta), 0.00) + COALESCE(SUM(p.costo_base), 0.00) AS ingresos_generados " +
                     "FROM veterinarios v " +
                     "LEFT JOIN consultas_medicas c ON v.id = c.veterinario_id " +
                     "LEFT JOIN procedimientos_especiales p ON v.id = p.veterinario_id AND p.estado = 'Finalizado' " +
                     "WHERE v.activo = TRUE " +
                     "GROUP BY v.id, v.nombre_completo, v.licencia_profesional, v.especialidad";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new DesempenoVeterinarioDTO(
                        rs.getInt("veterinario_id"),
                        rs.getString("nombre_completo"),
                        rs.getString("licencia_profesional"),
                        rs.getString("especialidad"),
                        rs.getInt("total_consultas"),
                        rs.getInt("total_procedimientos"),
                        rs.getInt("total_atenciones"),
                        rs.getDouble("ingresos_generados")
                ));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error en consulta directa de desempeno veterinario", e);
        }
        return lista;
    }

    private List<AlertaInventarioCriticaDTO> consultarAlertasInventarioDirecto() {
        List<AlertaInventarioCriticaDTO> lista = new ArrayList<>();
        String sql = "SELECT i.id AS producto_id, i.codigo_barras, i.nombre_producto, pt.nombre AS categoria, " +
                     "i.lote, i.cantidad_stock, i.stock_minimo, i.fecha_vencimiento, " +
                     "DATEDIFF(i.fecha_vencimiento, CURDATE()) AS dias_para_vencer, " +
                     "CASE WHEN i.fecha_vencimiento <= CURDATE() THEN 'VENCIDO' " +
                     "WHEN i.cantidad_stock = 0 THEN 'SIN_STOCK' " +
                     "WHEN i.cantidad_stock <= i.stock_minimo THEN 'STOCK_MINIMO' " +
                     "WHEN DATEDIFF(i.fecha_vencimiento, CURDATE()) <= 30 THEN 'POR_VENCER' " +
                     "ELSE 'NORMAL' END AS estado_alerta " +
                     "FROM inventario i " +
                     "JOIN producto_tipos pt ON i.tipo_id = pt.id " +
                     "WHERE i.activo = TRUE " +
                     "AND (i.cantidad_stock <= i.stock_minimo OR DATEDIFF(i.fecha_vencimiento, CURDATE()) <= 30)";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new AlertaInventarioCriticaDTO(
                        rs.getInt("producto_id"),
                        rs.getString("codigo_barras"),
                        rs.getString("nombre_producto"),
                        rs.getString("categoria"),
                        rs.getString("lote"),
                        rs.getInt("cantidad_stock"),
                        rs.getInt("stock_minimo"),
                        rs.getDate("fecha_vencimiento") != null ? rs.getDate("fecha_vencimiento").toLocalDate() : null,
                        rs.getInt("dias_para_vencer"),
                        rs.getString("estado_alerta")
                ));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error en consulta directa de inventario critico", e);
        }
        return lista;
    }

    private List<ResumenFacturacionMensualDTO> consultarFacturacionMensualDirecto() {
        List<ResumenFacturacionMensualDTO> lista = new ArrayList<>();
        String sql = "SELECT YEAR(fecha_emision) AS anio, MONTH(fecha_emision) AS mes, " +
                     "COUNT(id) AS facturas_emitidas, " +
                     "SUM(IF(estado = 'Pagada', 1, 0)) AS facturas_pagadas, " +
                     "SUM(IF(estado = 'Pendiente', 1, 0)) AS facturas_pendientes, " +
                     "SUM(IF(estado = 'Anulada', 1, 0)) AS facturas_anuladas, " +
                     "SUM(IF(estado <> 'Anulada', subtotal, 0.00)) AS subtotal_neto, " +
                     "SUM(IF(estado <> 'Anulada', descuento, 0.00)) AS total_descuentos, " +
                     "SUM(IF(estado <> 'Anulada', impuesto, 0.00)) AS total_impuestos, " +
                     "SUM(IF(estado <> 'Anulada', total, 0.00)) AS total_facturado " +
                     "FROM facturas " +
                     "GROUP BY YEAR(fecha_emision), MONTH(fecha_emision)";

        Connection conn = ConexionBD.getInstancia().getConexion();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new ResumenFacturacionMensualDTO(
                        rs.getInt("anio"),
                        rs.getInt("mes"),
                        rs.getInt("facturas_emitidas"),
                        rs.getInt("facturas_pagadas"),
                        rs.getInt("facturas_pendientes"),
                        rs.getInt("facturas_anuladas"),
                        rs.getDouble("subtotal_neto"),
                        rs.getDouble("total_descuentos"),
                        rs.getDouble("total_impuestos"),
                        rs.getDouble("total_facturado")
                ));
            }
        } catch (SQLException e) {
            LoggerManager.error(MODULO, "Error en consulta directa de facturacion mensual", e);
        }
        return lista;
    }
}
