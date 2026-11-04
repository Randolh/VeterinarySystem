package randolh.veterinarysystem.core.exportador.reporte;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Estrategia de exportacion de reportes tabulares a documento PDF formal apaisado utilizando OpenPDF
public class ReportePdfExportador implements ExportadorDocumento<ReporteExportData> {

    private static final String MODULO = "ReportePdfExportador";
    private static final String CARPETA_REPORTES = "exportaciones/reportes";

    public ReportePdfExportador() {
        new File(CARPETA_REPORTES).mkdirs();
    }

    @Override
    public FormatoExportacion getFormato() {
        return FormatoExportacion.PDF;
    }

    @Override
    public String exportar(ReporteExportData datos) {
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

            Paragraph pTit = new Paragraph(datos.getTitulo(), fTitulo);
            pTit.setAlignment(Element.ALIGN_CENTER);
            document.add(pTit);
            document.add(new Paragraph(" "));

            List<String> cabeceras = datos.getCabeceras();
            if (cabeceras != null && !cabeceras.isEmpty()) {
                PdfPTable tabla = new PdfPTable(cabeceras.size());
                tabla.setWidthPercentage(100);

                for (String c : cabeceras) {
                    PdfPCell cell = new PdfPCell(new Phrase(c, fCab));
                    cell.setBackgroundColor(azulPrimario);
                    cell.setPadding(6);
                    tabla.addCell(cell);
                }

                List<List<String>> filas = datos.getFilas();
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
}

