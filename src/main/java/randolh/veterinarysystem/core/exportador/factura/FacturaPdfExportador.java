package randolh.veterinarysystem.core.exportador.factura;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import randolh.veterinarysystem.core.exportador.ExportadorDocumento;
import randolh.veterinarysystem.core.exportador.FormatoExportacion;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

// Estrategia de exportacion de facturas a documento PDF formal utilizando OpenPDF
public class FacturaPdfExportador implements ExportadorDocumento<FacturaExportData> {

    private static final String MODULO = "FacturaPdfExportador";
    private static final String CARPETA_FACTURAS = "exportaciones/facturas";

    public FacturaPdfExportador() {
        new File(CARPETA_FACTURAS).mkdirs();
    }

    @Override
    public FormatoExportacion getFormato() {
        return FormatoExportacion.PDF;
    }

    @Override
    public String exportar(FacturaExportData datos) {
        String nombreArchivo = "factura_" + datos.getNumeroFactura() + ".pdf";
        File archivo = new File(CARPETA_FACTURAS, nombreArchivo);

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            PdfWriter.getInstance(document, fos);
            document.open();

            // Paleta de colores institucional
            Color azulPrimario = new Color(26, 64, 128);
            Color azulClaro = new Color(235, 242, 250);
            Color grisBorde = new Color(210, 215, 225);
            Color grisFila = new Color(248, 249, 250);
            Color verdeExito = new Color(34, 139, 34);

            // Tipografias OpenPDF
            Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, Color.WHITE);
            Font fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(220, 230, 245));
            Font fuenteFacturaTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, azulPrimario);
            Font fuenteFacturaNum = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(180, 40, 40));
            Font fuenteMetadatos = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.DARK_GRAY);
            Font fuenteSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, azulPrimario);
            Font fuenteCabTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.WHITE);
            Font fuenteCelda = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.DARK_GRAY);
            Font fuenteCeldaBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.BLACK);
            Font fuenteTotalLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, azulPrimario);
            Font fuenteTotalMonto = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, azulPrimario);
            Font fuentePie = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

            // 1. Encabezado institucional y caja de factura
            PdfPTable tablaEncabezado = new PdfPTable(new float[]{3.2f, 1.8f});
            tablaEncabezado.setWidthPercentage(100);

            PdfPCell celdaClinica = new PdfPCell();
            celdaClinica.setBackgroundColor(azulPrimario);
            celdaClinica.setPadding(12);
            celdaClinica.setBorder(PdfPCell.NO_BORDER);

            celdaClinica.addElement(new Paragraph(datos.getClinicaNombre(), fuenteTitulo));
            celdaClinica.addElement(new Paragraph("NIT: " + datos.getClinicaNit() + " | Tel: " + datos.getClinicaTel(), fuenteSubtitulo));
            celdaClinica.addElement(new Paragraph(datos.getClinicaDir(), fuenteSubtitulo));
            tablaEncabezado.addCell(celdaClinica);

            PdfPCell celdaFactura = new PdfPCell();
            celdaFactura.setBackgroundColor(azulClaro);
            celdaFactura.setPadding(10);
            celdaFactura.setBorderColor(azulPrimario);
            celdaFactura.setBorderWidth(1.2f);

            Paragraph pFacTitulo = new Paragraph("FACTURA ELECTRONICA", fuenteFacturaTitulo);
            pFacTitulo.setAlignment(Element.ALIGN_CENTER);
            Paragraph pFacNum = new Paragraph(datos.getNumeroFactura(), fuenteFacturaNum);
            pFacNum.setAlignment(Element.ALIGN_CENTER);
            Paragraph pFacFecha = new Paragraph("Fecha: " + datos.getFechaEmision(), fuenteMetadatos);
            pFacFecha.setAlignment(Element.ALIGN_CENTER);
            Paragraph pFacEstado = new Paragraph("Estado: " + datos.getEstado() + " | Pago: " + datos.getMetodoPago(), fuenteMetadatos);
            pFacEstado.setAlignment(Element.ALIGN_CENTER);

            celdaFactura.addElement(pFacTitulo);
            celdaFactura.addElement(pFacNum);
            celdaFactura.addElement(pFacFecha);
            celdaFactura.addElement(pFacEstado);
            tablaEncabezado.addCell(celdaFactura);

            document.add(tablaEncabezado);
            document.add(new Paragraph(" "));

            // 2. Bloque de Informacion del Cliente y Club
            PdfPTable tablaCliente = new PdfPTable(new float[]{1f, 1f});
            tablaCliente.setWidthPercentage(100);

            PdfPCell cClienteIzq = new PdfPCell();
            cClienteIzq.setBackgroundColor(new Color(250, 250, 252));
            cClienteIzq.setBorderColor(grisBorde);
            cClienteIzq.setPadding(8);
            cClienteIzq.addElement(new Paragraph("DATOS DEL TITULAR / DUENO", fuenteSeccion));
            cClienteIzq.addElement(new Paragraph("Nombre: " + datos.getClienteNombre(), fuenteCelda));
            cClienteIzq.addElement(new Paragraph("Documento: " + datos.getClienteDoc(), fuenteCelda));
            cClienteIzq.addElement(new Paragraph("Email: " + (datos.getClienteEmail() != null ? datos.getClienteEmail() : "-"), fuenteCelda));
            tablaCliente.addCell(cClienteIzq);

            PdfPCell cClienteDer = new PdfPCell();
            cClienteDer.setBackgroundColor(new Color(250, 250, 252));
            cClienteDer.setBorderColor(grisBorde);
            cClienteDer.setPadding(8);
            cClienteDer.addElement(new Paragraph("DETALLES DE ATENCION", fuenteSeccion));
            cClienteDer.addElement(new Paragraph("Telefono: " + (datos.getClienteTel() != null ? datos.getClienteTel() : "-"), fuenteCelda));
            cClienteDer.addElement(new Paragraph("Metodo de Pago: " + datos.getMetodoPago(), fuenteCelda));
            if (datos.getNivelClub() != null && !datos.getNivelClub().equalsIgnoreCase("Sin Club")) {
                Paragraph pClub = new Paragraph("Club Mascotas: Nivel " + datos.getNivelClub() + " (+" + datos.getPuntosGanados() + " pts acumulados)", fuenteCeldaBold);
                pClub.getFont().setColor(verdeExito);
                cClienteDer.addElement(pClub);
            } else {
                cClienteDer.addElement(new Paragraph("Club Mascotas: Cliente General (Sin Club)", fuenteCelda));
            }
            tablaCliente.addCell(cClienteDer);

            document.add(tablaCliente);
            document.add(new Paragraph(" "));

            // 3. Tabla de Items / Servicios / Medicamentos
            PdfPTable tablaItems = new PdfPTable(new float[]{1.1f, 3.8f, 0.9f, 1.4f, 1.4f});
            tablaItems.setWidthPercentage(100);

            String[] encabezados = {"TIPO", "DESCRIPCION / CONCEPTO", "CANT", "PRECIO U.", "SUBTOTAL"};
            for (String enc : encabezados) {
                PdfPCell cEnc = new PdfPCell(new Phrase(enc, fuenteCabTabla));
                cEnc.setBackgroundColor(azulPrimario);
                cEnc.setPadding(6);
                cEnc.setHorizontalAlignment(enc.equals("TIPO") || enc.startsWith("DESC") ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
                tablaItems.addCell(cEnc);
            }

            boolean filaPar = false;
            for (String[] it : datos.getItems()) {
                String tipo = it.length > 0 ? it[0] : "Item";
                String desc = it.length > 1 ? it[1] : "-";
                String cant = it.length > 2 ? it[2] : "1";
                String pUnit = it.length > 3 ? it[3] : "0.00";
                String sub = it.length > 4 ? it[4] : "0.00";

                Color bgFila = filaPar ? grisFila : Color.WHITE;
                filaPar = !filaPar;

                agregarCeldaItem(tablaItems, tipo, fuenteCelda, bgFila, grisBorde, Element.ALIGN_LEFT);
                agregarCeldaItem(tablaItems, desc, fuenteCelda, bgFila, grisBorde, Element.ALIGN_LEFT);
                agregarCeldaItem(tablaItems, cant, fuenteCelda, bgFila, grisBorde, Element.ALIGN_RIGHT);
                agregarCeldaItem(tablaItems, "$ " + pUnit, fuenteCelda, bgFila, grisBorde, Element.ALIGN_RIGHT);
                agregarCeldaItem(tablaItems, "$ " + sub, fuenteCeldaBold, bgFila, grisBorde, Element.ALIGN_RIGHT);
            }
            document.add(tablaItems);

            // 4. Bloque de Totales y Liquidacion
            PdfPTable tablaTotales = new PdfPTable(new float[]{3.4f, 1.6f});
            tablaTotales.setWidthPercentage(100);

            PdfPCell cTerminos = new PdfPCell();
            cTerminos.setBorder(PdfPCell.NO_BORDER);
            cTerminos.setPadding(8);
            Paragraph pTerm = new Paragraph("TERMINOS Y CONDICIONES:\n" +
                    "- Documento equivalente a factura de venta expedido por Clinica Veterinaria.\n" +
                    "- Medicamentos con sello de garantia dentro de los 5 dias de expedicion.\n" +
                    "- Puntos del Club sujetos al reglamento de fidelizacion vigente.", fuentePie);
            cTerminos.addElement(pTerm);
            tablaTotales.addCell(cTerminos);

            PdfPTable subTablaTot = new PdfPTable(new float[]{1.8f, 1.4f});
            subTablaTot.setWidthPercentage(100);

            agregarFilaTotal(subTablaTot, "Subtotal Bruto:", String.format(Locale.US, "$ %.2f", datos.getSubtotal()), fuenteCelda, fuenteCeldaBold);
            if (datos.getDescuento() > 0) {
                agregarFilaTotal(subTablaTot, "Descuento (" + datos.getNivelClub() + "):", String.format(Locale.US, "-$ %.2f", datos.getDescuento()), fuenteCelda, fuenteCeldaBold);
            }
            agregarFilaTotal(subTablaTot, "Impuesto IVA (19%):", String.format(Locale.US, "$ %.2f", datos.getImpuesto()), fuenteCelda, fuenteCeldaBold);
            agregarFilaTotal(subTablaTot, "TOTAL A PAGAR:", String.format(Locale.US, "$ %.2f", datos.getTotal()), fuenteTotalLabel, fuenteTotalMonto);

            PdfPCell cTotalesWrapper = new PdfPCell(subTablaTot);
            cTotalesWrapper.setBorder(PdfPCell.NO_BORDER);
            cTotalesWrapper.setPadding(4);
            tablaTotales.addCell(cTotalesWrapper);

            document.add(tablaTotales);

            // 5. Pie de pagina
            document.add(new Paragraph(" "));
            Paragraph pAgradecimiento = new Paragraph("GRACIAS POR SU PREFERENCIA Y POR CONFIAR EN NUESTROS PROFESIONALES VETERINARIOS", fuenteFacturaTitulo);
            pAgradecimiento.setAlignment(Element.ALIGN_CENTER);
            document.add(pAgradecimiento);

            document.close();
            LoggerManager.info(MODULO, "Factura PDF formal generada exitosamente con OpenPDF: " + archivo.getAbsolutePath());
            return archivo.getAbsolutePath();

        } catch (Exception e) {
            LoggerManager.error(MODULO, "Error al generar factura PDF formal con OpenPDF", e);
            return null;
        }
    }

    private void agregarCeldaItem(PdfPTable tabla, String texto, Font fuente, Color bg, Color borde, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto, fuente));
        c.setBackgroundColor(bg);
        c.setBorderColor(borde);
        c.setPadding(5);
        c.setHorizontalAlignment(alineacion);
        tabla.addCell(c);
    }

    private void agregarFilaTotal(PdfPTable subTabla, String label, String valor, Font fLabel, Font fValor) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, fLabel));
        c1.setBorder(PdfPCell.NO_BORDER);
        c1.setPadding(3);
        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        subTabla.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(valor, fValor));
        c2.setBorder(PdfPCell.NO_BORDER);
        c2.setPadding(3);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        subTabla.addCell(c2);
    }
}

