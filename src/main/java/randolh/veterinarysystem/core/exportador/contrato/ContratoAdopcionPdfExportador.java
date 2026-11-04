package randolh.veterinarysystem.core.exportador.contrato;

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

// Estrategia de exportacion de contratos de adopcion a documento PDF formal utilizando OpenPDF
public class ContratoAdopcionPdfExportador implements ExportadorDocumento<ContratoAdopcionExportData> {

    private static final String MODULO = "ContratoAdopcionPdfExportador";
    private static final String CARPETA_CONTRATOS = "exportaciones/contratos";

    public ContratoAdopcionPdfExportador() {
        new File(CARPETA_CONTRATOS).mkdirs();
    }

    @Override
    public FormatoExportacion getFormato() {
        return FormatoExportacion.PDF;
    }

    @Override
    public String exportar(ContratoAdopcionExportData datos) {
        String nombreArchivo = "contrato_adopcion_ADOP-" + datos.getAdopcionId() + ".pdf";
        File archivo = new File(CARPETA_CONTRATOS, nombreArchivo);

        Document document = new Document(PageSize.A4, 40, 40, 36, 36);
        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            PdfWriter.getInstance(document, fos);
            document.open();

            Color azulOscuro = new Color(20, 50, 100);
            Color verdeInstitucional = new Color(34, 139, 34);
            Color grisBorde = new Color(200, 205, 215);

            Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, azulOscuro);
            Font fSub = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.DARK_GRAY);
            Font fNum = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, verdeInstitucional);
            Font fSec = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, azulOscuro);
            Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.DARK_GRAY);
            Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.BLACK);
            Font fClausulaText = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);

            // 1. Encabezado institucional
            Paragraph pTit = new Paragraph("CONTRATO DE ADOPCION RESPONSABLE Y TUTELA ANIMAL", fTitulo);
            pTit.setAlignment(Element.ALIGN_CENTER);
            document.add(pTit);

            Paragraph pInst = new Paragraph(datos.getClinicaNombre() + " - NIT: " + datos.getClinicaNit() + " - Tel: " + datos.getClinicaTel(), fSub);
            pInst.setAlignment(Element.ALIGN_CENTER);
            document.add(pInst);

            Paragraph pNum = new Paragraph(String.format("CONTRATO NRO: ADOP-%05d  |  FECHA DE FORMALIZACION: %s",
                    datos.getAdopcionId(), datos.getFechaAdopcion()), fNum);
            pNum.setAlignment(Element.ALIGN_CENTER);
            document.add(pNum);
            document.add(new Paragraph(" "));

            // 2. Informacion de las Partes
            PdfPTable tablaPartes = new PdfPTable(new float[]{1f, 1f});
            tablaPartes.setWidthPercentage(100);

            PdfPCell cAdopIzq = new PdfPCell();
            cAdopIzq.setBackgroundColor(new Color(248, 250, 252));
            cAdopIzq.setBorderColor(grisBorde);
            cAdopIzq.setPadding(8);
            cAdopIzq.addElement(new Paragraph("I. ADOPTANTE / TUTOR LEGAL", fSec));
            cAdopIzq.addElement(new Paragraph("Nombre Completo: " + datos.getAdoptanteNombre(), fBold));
            cAdopIzq.addElement(new Paragraph("Documento: " + datos.getAdoptanteDoc(), fNormal));
            cAdopIzq.addElement(new Paragraph("Telefono: " + datos.getAdoptanteTel(), fNormal));
            cAdopIzq.addElement(new Paragraph("Direccion: " + (datos.getAdoptanteDir() != null ? datos.getAdoptanteDir() : "-"), fNormal));
            cAdopIzq.addElement(new Paragraph("Email: " + (datos.getAdoptanteEmail() != null ? datos.getAdoptanteEmail() : "-"), fNormal));
            tablaPartes.addCell(cAdopIzq);

            PdfPCell cMascDer = new PdfPCell();
            cMascDer.setBackgroundColor(new Color(248, 250, 252));
            cMascDer.setBorderColor(grisBorde);
            cMascDer.setPadding(8);
            cMascDer.addElement(new Paragraph("II. ANIMAL DE COMPANIA ADOPTADO", fSec));
            cMascDer.addElement(new Paragraph("Nombre: " + datos.getMascotaNombre(), fBold));
            cMascDer.addElement(new Paragraph("Especie / Raza: " + datos.getEspecie() + " - " + (datos.getRaza() != null ? datos.getRaza() : "Mestizo"), fNormal));
            cMascDer.addElement(new Paragraph("Sexo / Edad: " + (datos.getSexo() != null ? datos.getSexo() : "-") + " - " + datos.getEdad(), fNormal));
            cMascDer.addElement(new Paragraph("Temperamento: " + (datos.getTemperamento() != null ? datos.getTemperamento() : "Sociable"), fNormal));
            if (datos.getNecesidades() != null && !datos.getNecesidades().isEmpty()) {
                cMascDer.addElement(new Paragraph("Cuidados Especiales: " + datos.getNecesidades(), fNormal));
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
            document.add(new Paragraph("CUARTA. DERECHO DE SEGUIMIENTO Y VISITAS: El adoptante autoriza inspecciones periodicas o virtuales por parte de la clinica. Primer seguimiento estipulado: " + (datos.getFechaSeguimiento() != null ? datos.getFechaSeguimiento() : "A los 30 dias de la firma") + ".", fClausulaText));
            if (datos.getCondicionesEspeciales() != null && !datos.getCondicionesEspeciales().isEmpty()) {
                document.add(new Paragraph("QUINTA. CONDICIONES ESPECIALES PACTADAS: " + datos.getCondicionesEspeciales(), fClausulaText));
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // 4. Bloque de firmas
            PdfPTable tablaFirmas = new PdfPTable(new float[]{1f, 1f});
            tablaFirmas.setWidthPercentage(100);

            PdfPCell f1 = new PdfPCell();
            f1.setBorder(PdfPCell.NO_BORDER);
            f1.setHorizontalAlignment(Element.ALIGN_CENTER);
            Paragraph pLin1 = new Paragraph("_____________________________________\nFIRMA DEL ADOPTANTE / TUTOR\nC.C. " + datos.getAdoptanteDoc() + "\n" + datos.getAdoptanteNombre(), fBold);
            pLin1.setAlignment(Element.ALIGN_CENTER);
            f1.addElement(pLin1);
            tablaFirmas.addCell(f1);

            PdfPCell f2 = new PdfPCell();
            f2.setBorder(PdfPCell.NO_BORDER);
            f2.setHorizontalAlignment(Element.ALIGN_CENTER);
            Paragraph pLin2 = new Paragraph("_____________________________________\nDIRECCION MEDICA Y BIENESTAR ANIMAL\nClinica " + datos.getClinicaNombre() + "\nNIT: " + datos.getClinicaNit(), fBold);
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
}

