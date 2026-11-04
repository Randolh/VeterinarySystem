package randolh.veterinarysystem.core.exportador.factura;

import randolh.veterinarysystem.core.exportador.ExportadorDocumento;
import randolh.veterinarysystem.core.exportador.FormatoExportacion;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Estrategia de exportacion de facturas a texto plano estructurado
public class FacturaTextoExportador implements ExportadorDocumento<FacturaExportData> {

    private static final String MODULO = "FacturaTextoExportador";
    private static final String CARPETA_FACTURAS = "exportaciones/facturas";

    public FacturaTextoExportador() {
        new File(CARPETA_FACTURAS).mkdirs();
    }

    @Override
    public FormatoExportacion getFormato() {
        return FormatoExportacion.TEXTO;
    }

    @Override
    public String exportar(FacturaExportData datos) {
        String nombreArchivo = "factura_" + datos.getNumeroFactura() + ".txt";
        File archivo = new File(CARPETA_FACTURAS, nombreArchivo);

        try (FileWriter writer = new FileWriter(archivo, StandardCharsets.UTF_8)) {
            writer.write("================================================================================\n");
            writer.write(centrarTexto(datos.getClinicaNombre(), 80) + "\n");
            writer.write(centrarTexto("NIT: " + datos.getClinicaNit() + " | Tel: " + datos.getClinicaTel(), 80) + "\n");
            writer.write(centrarTexto(datos.getClinicaDir(), 80) + "\n");
            writer.write("================================================================================\n");
            writer.write(String.format(" FACTURA ELECTRONICA DE VENTA: %-25s FECHA: %s\n",
                    datos.getNumeroFactura(), datos.getFechaEmision()));
            writer.write(String.format(" ESTADO: %-37s METODO PAGO: %s\n",
                    datos.getEstado(), datos.getMetodoPago()));
            writer.write("--------------------------------------------------------------------------------\n");
            writer.write(" DATOS DEL CLIENTE / DUENO:\n");
            writer.write(String.format(" Nombre Completo: %-35s Documento: %s\n",
                    datos.getClienteNombre(), datos.getClienteDoc()));
            writer.write(String.format(" Correo:          %-35s Telefono:  %s\n",
                    datos.getClienteEmail() != null ? datos.getClienteEmail() : "-",
                    datos.getClienteTel() != null ? datos.getClienteTel() : "-"));

            if (datos.getNivelClub() != null && !datos.getNivelClub().equalsIgnoreCase("Sin Club")) {
                writer.write(String.format(" Membresia Club:  %-35s Puntos Ganados: +%d pts\n",
                        datos.getNivelClub(), datos.getPuntosGanados()));
            }
            writer.write("--------------------------------------------------------------------------------\n");
            writer.write(String.format(" %-4s | %-10s | %-32s | %-5s | %-10s | %-10s\n",
                    "ITEM", "TIPO", "DESCRIPCION", "CANT", "PRECIO U.", "SUBTOTAL"));
            writer.write("--------------------------------------------------------------------------------\n");

            int idx = 1;
            for (String[] it : datos.getItems()) {
                String tipo = it.length > 0 ? it[0] : "Item";
                String desc = it.length > 1 ? truncar(it[1], 32) : "-";
                String cant = it.length > 2 ? it[2] : "1";
                String pUnit = it.length > 3 ? it[3] : "0.00";
                String sub = it.length > 4 ? it[4] : "0.00";

                writer.write(String.format(" %-4d | %-10s | %-32s | %-5s | %-10s | %-10s\n",
                        idx++, tipo, desc, cant, pUnit, sub));
            }

            writer.write("--------------------------------------------------------------------------------\n");
            writer.write(String.format("%65s: $ %10.2f\n", "SUBTOTAL BRUTO", datos.getSubtotal()));
            if (datos.getDescuento() > 0) {
                writer.write(String.format("%65s: -$ %9.2f\n",
                        "DESCUENTO CLUB (" + datos.getNivelClub() + ")", datos.getDescuento()));
            }
            writer.write(String.format("%65s: $ %10.2f\n", "IMPUESTO (IVA)", datos.getImpuesto()));
            writer.write(String.format("%65s: $ %10.2f\n", "TOTAL NETO A PAGAR", datos.getTotal()));
            writer.write("================================================================================\n");
            writer.write(centrarTexto("GRACIAS POR CONFIAR EN NUESTROS SERVICIOS VETERINARIOS", 80) + "\n");
            writer.write(centrarTexto("Este documento es representacion digital de factura contable.", 80) + "\n");
            writer.write("================================================================================\n");

            LoggerManager.info(MODULO, "Factura en texto plano generada en: " + archivo.getAbsolutePath());
            return archivo.getAbsolutePath();

        } catch (IOException e) {
            LoggerManager.error(MODULO, "Error al generar factura de texto plano", e);
            return null;
        }
    }

    private String centrarTexto(String texto, int ancho) {
        if (texto == null) return "";
        if (texto.length() >= ancho) return texto.substring(0, ancho);
        int espacios = (ancho - texto.length()) / 2;
        return " ".repeat(espacios) + texto;
    }

    private String truncar(String texto, int longitud) {
        if (texto == null) return "";
        return texto.length() > longitud ? texto.substring(0, longitud - 3) + "..." : texto;
    }
}

