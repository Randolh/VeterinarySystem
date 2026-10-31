package randolh.veterinarysystem.core.utils;

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

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

// Exportador dual de comprobantes y reportes a formato texto plano y documento PDF formal OpenPDF
public class PdfExportador {

    private static final String MODULO = "PdfExportador";
    private static final String CARPETA_FACTURAS = "exportaciones/facturas";
    private static final String CARPETA_REPORTES = "exportaciones/reportes";
    private static final String CARPETA_CONTRATOS = "exportaciones/contratos";

    static {
        new File(CARPETA_FACTURAS).mkdirs();
        new File(CARPETA_REPORTES).mkdirs();
        new File(CARPETA_CONTRATOS).mkdirs();
    }

    // Exporta comprobante a archivo de texto plano estructurado
    public static String exportarFacturaTexto(
            String clinicaNombre, String clinicaNit, String clinicaTel, String clinicaDir,
            String numeroFactura, String fechaEmision, String clienteNombre, String clienteDoc,
            String clienteEmail, String clienteTel, String nivelClub, int puntosGanados,
            List<String[]> items, double subtotal, double descuento, double impuesto,
            double total, String metodoPago, String estado) {

        String nombreArchivo = "factura_" + numeroFactura + ".txt";
        File archivo = new File(CARPETA_FACTURAS, nombreArchivo);

        try (FileWriter writer = new FileWriter(archivo, StandardCharsets.UTF_8)) {
            writer.write("================================================================================\n");
            writer.write(centrarTexto(clinicaNombre, 80) + "\n");
            writer.write(centrarTexto("NIT: " + clinicaNit + " | Tel: " + clinicaTel, 80) + "\n");
            writer.write(centrarTexto(clinicaDir, 80) + "\n");
            writer.write("================================================================================\n");
            writer.write(String.format(" FACTURA ELECTRONICA DE VENTA: %-25s FECHA: %s\n", numeroFactura, fechaEmision));
            writer.write(String.format(" ESTADO: %-37s METODO PAGO: %s\n", estado, metodoPago));
            writer.write("--------------------------------------------------------------------------------\n");
            writer.write(" DATOS DEL CLIENTE / DUENO:\n");
            writer.write(String.format(" Nombre Completo: %-35s Documento: %s\n", clienteNombre, clienteDoc));
            writer.write(String.format(" Correo:          %-35s Telefono:  %s\n", clienteEmail, clienteTel));
            if (nivelClub != null && !nivelClub.equalsIgnoreCase("Sin Club")) {
                writer.write(String.format(" Membresia Club:  %-35s Puntos Ganados: +%d pts\n", nivelClub, puntosGanados));
            }
            writer.write("--------------------------------------------------------------------------------\n");
            writer.write(String.format(" %-4s | %-10s | %-32s | %-5s | %-10s | %-10s\n",
                    "ITEM", "TIPO", "DESCRIPCION", "CANT", "PRECIO U.", "SUBTOTAL"));
            writer.write("--------------------------------------------------------------------------------\n");

            int idx = 1;
            for (String[] it : items) {
                String tipo = it.length > 0 ? it[0] : "Item";
                String desc = it.length > 1 ? truncar(it[1], 32) : "-";
                String cant = it.length > 2 ? it[2] : "1";
                String pUnit = it.length > 3 ? it[3] : "0.00";
                String sub = it.length > 4 ? it[4] : "0.00";

                writer.write(String.format(" %-4d | %-10s | %-32s | %-5s | %-10s | %-10s\n",
                        idx++, tipo, desc, cant, pUnit, sub));
            }

            writer.write("--------------------------------------------------------------------------------\n");
            writer.write(String.format("%65s: $ %10.2f\n", "SUBTOTAL BRUTO", subtotal));
            if (descuento > 0) {
                writer.write(String.format("%65s: -$ %9.2f\n", "DESCUENTO CLUB (" + nivelClub + ")", descuento));
            }
            writer.write(String.format("%65s: $ %10.2f\n", "IMPUESTO (IVA)", impuesto));
            writer.write(String.format("%65s: $ %10.2f\n", "TOTAL NETO A PAGAR", total));
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

    // Exporta comprobante a documento PDF formal utilizando la libreria OpenPDF
    public static String exportarFacturaPdf(
            String clinicaNombre, String clinicaNit, String clinicaTel, String clinicaDir,
            String numeroFactura, String fechaEmision, String clienteNombre, String clienteDoc,
            String clienteEmail, String clienteTel, String nivelClub, int puntosGanados,
            List<String[]> items, double subtotal, double descuento, double impuesto,
            double total, String metodoPago, String estado) {

        String nombreArchivo = "factura_" + numeroFactura + ".pdf";
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

            Paragraph pTitulo = new Paragraph(clinicaNombre, fuenteTitulo);
            Paragraph pNit = new Paragraph("NIT: " + clinicaNit + " | Tel: " + clinicaTel, fuenteSubtitulo);
            Paragraph pDir = new Paragraph(clinicaDir, fuenteSubtitulo);
            celdaClinica.addElement(pTitulo);
            celdaClinica.addElement(pNit);
            celdaClinica.addElement(pDir);
            tablaEncabezado.addCell(celdaClinica);

            PdfPCell celdaFactura = new PdfPCell();
            celdaFactura.setBackgroundColor(azulClaro);
            celdaFactura.setPadding(10);
            celdaFactura.setBorderColor(azulPrimario);
            celdaFactura.setBorderWidth(1.2f);

            Paragraph pFacTitulo = new Paragraph("FACTURA ELECTRONICA", fuenteFacturaTitulo);
            pFacTitulo.setAlignment(Element.ALIGN_CENTER);
            Paragraph pFacNum = new Paragraph(numeroFactura, fuenteFacturaNum);
            pFacNum.setAlignment(Element.ALIGN_CENTER);
            Paragraph pFacFecha = new Paragraph("Fecha: " + fechaEmision, fuenteMetadatos);
            pFacFecha.setAlignment(Element.ALIGN_CENTER);
            Paragraph pFacEstado = new Paragraph("Estado: " + estado + " | Pago: " + metodoPago, fuenteMetadatos);
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
            cClienteIzq.addElement(new Paragraph("Nombre: " + clienteNombre, fuenteCelda));
            cClienteIzq.addElement(new Paragraph("Documento: " + clienteDoc, fuenteCelda));
            cClienteIzq.addElement(new Paragraph("Email: " + (clienteEmail != null ? clienteEmail : "-"), fuenteCelda));
            tablaCliente.addCell(cClienteIzq);

            PdfPCell cClienteDer = new PdfPCell();
            cClienteDer.setBackgroundColor(new Color(250, 250, 252));
            cClienteDer.setBorderColor(grisBorde);
            cClienteDer.setPadding(8);
            cClienteDer.addElement(new Paragraph("DETALLES DE ATENCION", fuenteSeccion));
            cClienteDer.addElement(new Paragraph("Telefono: " + (clienteTel != null ? clienteTel : "-"), fuenteCelda));
            cClienteDer.addElement(new Paragraph("Metodo de Pago: " + metodoPago, fuenteCelda));
            if (nivelClub != null && !nivelClub.equalsIgnoreCase("Sin Club")) {
                Paragraph pClub = new Paragraph("Club Mascotas: Nivel " + nivelClub + " (+" + puntosGanados + " pts acumulados)", fuenteCeldaBold);
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
            for (String[] it : items) {
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

            agregarFilaTotal(subTablaTot, "Subtotal Bruto:", String.format(Locale.US, "$ %.2f", subtotal), fuenteCelda, fuenteCeldaBold);
            if (descuento > 0) {
                agregarFilaTotal(subTablaTot, "Descuento (" + nivelClub + "):", String.format(Locale.US, "-$ %.2f", descuento), fuenteCelda, fuenteCeldaBold);
            }
            agregarFilaTotal(subTablaTot, "Impuesto IVA (19%):", String.format(Locale.US, "$ %.2f", impuesto), fuenteCelda, fuenteCeldaBold);
            agregarFilaTotal(subTablaTot, "TOTAL A PAGAR:", String.format(Locale.US, "$ %.2f", total), fuenteTotalLabel, fuenteTotalMonto);

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

    // Exporta reporte generico tabular a documento PDF formal utilizando OpenPDF
    public static String exportarReporteGenerico(String titulo, List<String> cabeceras, List<List<String>> filas) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nombreArchivo = "reporte_" + timestamp + ".pdf";
        File archivo = new File(CARPETA_REPORTES, nombreArchivo);

        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            PdfWriter.getInstance(document, fos);
            document.open();

            Color azulPrimario = new Color(26, 64, 128);
            Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, azulPrimario);
            Font fCab = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font fCelda = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.DARK_GRAY);

            Paragraph pTit = new Paragraph(titulo, fTitulo);
            pTit.setAlignment(Element.ALIGN_CENTER);
            document.add(pTit);
            document.add(new Paragraph(" "));

            if (cabeceras != null && !cabeceras.isEmpty()) {
                PdfPTable tabla = new PdfPTable(cabeceras.size());
                tabla.setWidthPercentage(100);

                for (String c : cabeceras) {
                    PdfPCell cell = new PdfPCell(new Phrase(c, fCab));
                    cell.setBackgroundColor(azulPrimario);
                    cell.setPadding(6);
                    tabla.addCell(cell);
                }

                if (filas != null) {
                    boolean par = false;
                    for (List<String> fila : filas) {
                        Color bg = par ? new Color(248, 249, 250) : Color.WHITE;
                        par = !par;
                        for (String val : fila) {
                            PdfPCell cell = new PdfPCell(new Phrase(val != null ? val : "", fCelda));
                            cell.setBackgroundColor(bg);
                            cell.setPadding(5);
                            tabla.addCell(cell);
                        }
                    }
                }
                document.add(tabla);
            }

            document.close();
            LoggerManager.info(MODULO, "Reporte PDF formal generado en: " + archivo.getAbsolutePath());
            return archivo.getAbsolutePath();

        } catch (Exception e) {
            LoggerManager.error(MODULO, "Error al generar reporte PDF con OpenPDF", e);
            return null;
        }
    }

    // Exporta contrato legal de adopcion responsable a archivo de texto plano
    public static String exportarContratoAdopcionTexto(
            String clinicaNombre, String clinicaNit, String clinicaTel, String clinicaDir,
            int adopcionId, String fechaAdopcion, String adoptanteNombre, String adoptanteDoc,
            String adoptanteTel, String adoptanteDir, String adoptanteEmail,
            String mascotaNombre, String especie, String raza, String sexo, String edad,
            String historia, String temperamento, String necesidades, String condicionesEspeciales,
            String fechaSeguimiento) {

        String nombreArchivo = "contrato_adopcion_ADOP-" + adopcionId + ".txt";
        File archivo = new File(CARPETA_CONTRATOS, nombreArchivo);

        try (FileWriter writer = new FileWriter(archivo, StandardCharsets.UTF_8)) {
            writer.write("================================================================================\n");
            writer.write(centrarTexto(clinicaNombre, 80) + "\n");
            writer.write(centrarTexto("NIT: " + clinicaNit + " | Tel: " + clinicaTel, 80) + "\n");
            writer.write(centrarTexto(clinicaDir, 80) + "\n");
            writer.write("================================================================================\n");
            writer.write(centrarTexto("CONTRATO LEGAL DE ADOPCION RESPONSABLE Y TUTELA ANIMAL", 80) + "\n");
            writer.write(String.format(" CONTRATO NRO: ADOP-%05d                            FECHA: %s\n", adopcionId, fechaAdopcion));
            writer.write("--------------------------------------------------------------------------------\n");
            writer.write(" I. DATOS DEL ADOPTANTE / TUTOR LEGAL:\n");
            writer.write(String.format(" Nombre Completo:     %-32s Documento: %s\n", adoptanteNombre, adoptanteDoc));
            writer.write(String.format(" Direccion Domicilio: %-32s Telefono:  %s\n", adoptanteDir != null ? adoptanteDir : "-", adoptanteTel));
            writer.write(String.format(" Correo Electronico:  %s\n", adoptanteEmail != null ? adoptanteEmail : "-"));
            writer.write("--------------------------------------------------------------------------------\n");
            writer.write(" II. DATOS DEL ANIMAL ADOPTADO:\n");
            writer.write(String.format(" Nombre:       %-35s Especie: %s\n", mascotaNombre, especie));
            writer.write(String.format(" Raza:         %-35s Sexo:    %s\n", raza != null ? raza : "Mestizo", sexo != null ? sexo : "-"));
            writer.write(String.format(" Edad Estim.:  %-35s Temperamento: %s\n", edad, temperamento != null ? temperamento : "-"));
            if (historia != null && !historia.isEmpty()) {
                writer.write(" Antecedentes: " + historia + "\n");
            }
            if (necesidades != null && !necesidades.isEmpty()) {
                writer.write(" Cuidados Esp: " + necesidades + "\n");
            }
            writer.write("--------------------------------------------------------------------------------\n");
            writer.write(" III. CLAUSULAS DE COMPROMISO Y BIENESTAR ANIMAL:\n");
            writer.write(" 1. BIENESTAR Y SALUD: El adoptante se compromete a proveer alimentacion balanceada,\n" +
                         "    atencion veterinaria preventiva, cuadro de vacunas y desparasitacion al dia.\n");
            writer.write(" 2. TENENCIA RESPONSABLE: Queda terminantemente prohibido mantener al animal atado,\n" +
                         "    en azoteas o a la intemperie. El animal debe convivir como miembro del hogar.\n");
            writer.write(" 3. NO REPRODUCCION NI COMERCIO: Queda prohibida la reproduccion comercial o cruza.\n" +
                         "    El animal debe ser esterilizado segun protocolo clinico veterinario.\n");
            writer.write(" 4. PROHIBICION DE ABANDONO: Bajo ninguna circunstancia se abandonara o cedera a\n" +
                         "    terceros. En caso de fuerza mayor, debera restituirse a la institucion.\n");
            writer.write(" 5. SEGUIMIENTO: El adoptante autoriza visitas periodicas del comite veterinario.\n" +
                         "    Fecha programada de primer seguimiento post-adopcion: " + (fechaSeguimiento != null ? fechaSeguimiento : "En 30 dias") + "\n");
            if (condicionesEspeciales != null && !condicionesEspeciales.isEmpty()) {
                writer.write(" 6. CONDICIONES PARTICULARES ACORDADAS: " + condicionesEspeciales + "\n");
            }
            writer.write("--------------------------------------------------------------------------------\n\n\n");
            writer.write("   ___________________________                 ___________________________\n");
            writer.write("        FIRMA DEL ADOPTANTE                         DIRECCION MEDICA\n");
            writer.write("   Doc: " + adoptanteDoc + "                Clinica " + clinicaNombre + "\n");
            writer.write("================================================================================\n");

            LoggerManager.info(MODULO, "Contrato de adopcion TXT generado en: " + archivo.getAbsolutePath());
            return archivo.getAbsolutePath();

        } catch (IOException e) {
            LoggerManager.error(MODULO, "Error al generar contrato de adopcion de texto", e);
            return null;
        }
    }

    // Exporta contrato legal de adopcion responsable a documento PDF formal utilizando OpenPDF
    public static String exportarContratoAdopcionPdf(
            String clinicaNombre, String clinicaNit, String clinicaTel, String clinicaDir,
            int adopcionId, String fechaAdopcion, String adoptanteNombre, String adoptanteDoc,
            String adoptanteTel, String adoptanteDir, String adoptanteEmail,
            String mascotaNombre, String especie, String raza, String sexo, String edad,
            String historia, String temperamento, String necesidades, String condicionesEspeciales,
            String fechaSeguimiento) {

        String nombreArchivo = "contrato_adopcion_ADOP-" + adopcionId + ".pdf";
        File archivo = new File(CARPETA_CONTRATOS, nombreArchivo);

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            PdfWriter.getInstance(document, fos);
            document.open();

            Color azulPrimario = new Color(26, 64, 128);
            Color verdeClaro = new Color(240, 248, 240);
            Color grisBorde = new Color(205, 210, 220);

            Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.WHITE);
            Font fSub = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, new Color(220, 230, 245));
            Font fEncDoc = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, azulPrimario);
            Font fSec = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, azulPrimario);
            Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.DARK_GRAY);
            Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.BLACK);
            Font fClausulaText = FontFactory.getFont(FontFactory.HELVETICA, 8.0f, Color.DARK_GRAY);

            // 1. Cabecera institucional
            PdfPTable tablaEnc = new PdfPTable(new float[]{3.2f, 1.8f});
            tablaEnc.setWidthPercentage(100);

            PdfPCell cClinica = new PdfPCell();
            cClinica.setBackgroundColor(azulPrimario);
            cClinica.setPadding(10);
            cClinica.setBorder(PdfPCell.NO_BORDER);
            cClinica.addElement(new Paragraph(clinicaNombre, fTitulo));
            cClinica.addElement(new Paragraph("NIT: " + clinicaNit + " | Tel: " + clinicaTel, fSub));
            cClinica.addElement(new Paragraph(clinicaDir, fSub));
            tablaEnc.addCell(cClinica);

            PdfPCell cAdop = new PdfPCell();
            cAdop.setBackgroundColor(verdeClaro);
            cAdop.setPadding(10);
            cAdop.setBorderColor(new Color(46, 139, 87));
            cAdop.setBorderWidth(1.2f);
            Paragraph pT = new Paragraph("CONTRATO DE ADOPCION", fEncDoc);
            pT.setAlignment(Element.ALIGN_CENTER);
            Paragraph pN = new Paragraph(String.format("NRO: ADOP-%05d", adopcionId), fBold);
            pN.setAlignment(Element.ALIGN_CENTER);
            Paragraph pF = new Paragraph("Fecha: " + fechaAdopcion, fNormal);
            pF.setAlignment(Element.ALIGN_CENTER);
            cAdop.addElement(pT);
            cAdop.addElement(pN);
            cAdop.addElement(pF);
            tablaEnc.addCell(cAdop);

            document.add(tablaEnc);
            document.add(new Paragraph(" "));

            // 2. Tablas de datos de Adoptante y Mascota
            PdfPTable tablaPartes = new PdfPTable(new float[]{1f, 1f});
            tablaPartes.setWidthPercentage(100);

            PdfPCell cAdopIzq = new PdfPCell();
            cAdopIzq.setBackgroundColor(new Color(250, 250, 252));
            cAdopIzq.setBorderColor(grisBorde);
            cAdopIzq.setPadding(8);
            cAdopIzq.addElement(new Paragraph("DATOS DEL ADOPTANTE / TUTOR", fSec));
            cAdopIzq.addElement(new Paragraph("Nombre: " + adoptanteNombre, fNormal));
            cAdopIzq.addElement(new Paragraph("Documento: " + adoptanteDoc, fNormal));
            cAdopIzq.addElement(new Paragraph("Telefono: " + (adoptanteTel != null ? adoptanteTel : "-"), fNormal));
            cAdopIzq.addElement(new Paragraph("Direccion: " + (adoptanteDir != null ? adoptanteDir : "-"), fNormal));
            cAdopIzq.addElement(new Paragraph("Email: " + (adoptanteEmail != null ? adoptanteEmail : "-"), fNormal));
            tablaPartes.addCell(cAdopIzq);

            PdfPCell cMascDer = new PdfPCell();
            cMascDer.setBackgroundColor(new Color(250, 250, 252));
            cMascDer.setBorderColor(grisBorde);
            cMascDer.setPadding(8);
            cMascDer.addElement(new Paragraph("DATOS DEL ANIMAL BENEFICIARIO", fSec));
            cMascDer.addElement(new Paragraph("Nombre: " + mascotaNombre, fNormal));
            cMascDer.addElement(new Paragraph("Especie / Raza: " + especie + " - " + (raza != null ? raza : "Mestizo"), fNormal));
            cMascDer.addElement(new Paragraph("Sexo / Edad: " + (sexo != null ? sexo : "-") + " - " + edad, fNormal));
            cMascDer.addElement(new Paragraph("Temperamento: " + (temperamento != null ? temperamento : "Sociable"), fNormal));
            if (necesidades != null && !necesidades.isEmpty()) {
                cMascDer.addElement(new Paragraph("Cuidados Especiales: " + necesidades, fNormal));
            }
            tablaPartes.addCell(cMascDer);

            document.add(tablaPartes);
            document.add(new Paragraph(" "));

            // 3. Clausulas Legales de Adopcion Responsable
            Paragraph pClausulasTit = new Paragraph("CLAUSULAS Y COMPROMISOS LEGALES DE BIENESTAR ANIMAL:", fSec);
            document.add(pClausulasTit);

            document.add(new Paragraph("PRIMERA. CONDICIONES DE VIDA Y BIENESTAR: El adoptante se compromete a brindar atencion digna, alimentacion adecuada, refugio protegido y atencion medica veterinaria preventiva periodica.", fClausulaText));
            document.add(new Paragraph("SEGUNDA. PROHIBICION DE MALTRATO O NEGLIGENCIA: Queda terminantemente prohibido mantener al animal atado, en azoteas o a la intemperie. Cualquier signo de negligencia o maltrato dara lugar a la rescision inmediata del presente contrato.", fClausulaText));
            document.add(new Paragraph("TERCERA. OBLIGACION DE ESTERILIZACION: Queda expresamente prohibida la reproduccion con fines comerciales o de lucro. El tutor se compromete a cumplir con la esterilizacion quirurgica.", fClausulaText));
            document.add(new Paragraph("CUARTA. DERECHO DE SEGUIMIENTO Y VISITAS: El adoptante autoriza inspecciones periodicas o virtuales por parte de la clinica. Primer seguimiento estipulado: " + (fechaSeguimiento != null ? fechaSeguimiento : "A los 30 dias de la firma") + ".", fClausulaText));
            if (condicionesEspeciales != null && !condicionesEspeciales.isEmpty()) {
                document.add(new Paragraph("QUINTA. CONDICIONES ESPECIALES PACTADAS: " + condicionesEspeciales, fClausulaText));
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // 4. Bloque de firmas
            PdfPTable tablaFirmas = new PdfPTable(new float[]{1f, 1f});
            tablaFirmas.setWidthPercentage(100);

            PdfPCell f1 = new PdfPCell();
            f1.setBorder(PdfPCell.NO_BORDER);
            f1.setHorizontalAlignment(Element.ALIGN_CENTER);
            Paragraph pLin1 = new Paragraph("_____________________________________\nFIRMA DEL ADOPTANTE / TUTOR\nC.C. " + adoptanteDoc + "\n" + adoptanteNombre, fBold);
            pLin1.setAlignment(Element.ALIGN_CENTER);
            f1.addElement(pLin1);
            tablaFirmas.addCell(f1);

            PdfPCell f2 = new PdfPCell();
            f2.setBorder(PdfPCell.NO_BORDER);
            f2.setHorizontalAlignment(Element.ALIGN_CENTER);
            Paragraph pLin2 = new Paragraph("_____________________________________\nDIRECCION MEDICA Y BIENESTAR ANIMAL\nClinica " + clinicaNombre + "\nNIT: " + clinicaNit, fBold);
            pLin2.setAlignment(Element.ALIGN_CENTER);
            f2.addElement(pLin2);
            tablaFirmas.addCell(f2);

            document.add(tablaFirmas);

            document.close();
            LoggerManager.info(MODULO, "Contrato de adopcion PDF generado en: " + archivo.getAbsolutePath());
            return archivo.getAbsolutePath();

        } catch (Exception e) {
            LoggerManager.error(MODULO, "Error al generar contrato de adopcion PDF con OpenPDF", e);
            return null;
        }
    }

    private static void agregarCeldaItem(PdfPTable tabla, String texto, Font fuente, Color bg, Color borde, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto, fuente));
        c.setBackgroundColor(bg);
        c.setBorderColor(borde);
        c.setPadding(5);
        c.setHorizontalAlignment(alineacion);
        tabla.addCell(c);
    }

    private static void agregarFilaTotal(PdfPTable subTabla, String label, String valor, Font fLabel, Font fValor) {
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

    private static String centrarTexto(String texto, int ancho) {
        if (texto == null) return "";
        if (texto.length() >= ancho) return texto.substring(0, ancho);
        int espacios = (ancho - texto.length()) / 2;
        return " ".repeat(espacios) + texto;
    }

    private static String truncar(String texto, int longitud) {
        if (texto == null) return "";
        return texto.length() > longitud ? texto.substring(0, longitud - 3) + "..." : texto;
    }
}
