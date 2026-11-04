package randolh.veterinarysystem.core.exportador.carnet;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import randolh.veterinarysystem.consulta.ProcedimientoEspecial;
import randolh.veterinarysystem.core.exportador.ExportadorDocumento;
import randolh.veterinarysystem.core.exportador.FormatoExportacion;
import randolh.veterinarysystem.core.utils.LoggerManager;
import randolh.veterinarysystem.mascota.MascotaDAO.VacunaHistorialItem;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.time.format.DateTimeFormatter;

// Estrategia de exportacion del carnet de salud a documento PDF formal utilizando OpenPDF
public class CarnetSaludPdfExportador implements ExportadorDocumento<CarnetSaludExportData> {

    private static final String MODULO = "CarnetSaludPdfExportador";
    private static final String CARPETA_CARNETS = "exportaciones/carnets";

    public CarnetSaludPdfExportador() {
        new File(CARPETA_CARNETS).mkdirs();
    }

    @Override
    public FormatoExportacion getFormato() {
        return FormatoExportacion.PDF;
    }

    @Override
    public String exportar(CarnetSaludExportData datos) {
        String nombreArchivo = "carnet_mascota_" + datos.getMascotaId() + ".pdf";
        File archivo = new File(CARPETA_CARNETS, nombreArchivo);

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            PdfWriter.getInstance(document, fos);
            document.open();

            // Paleta de colores institucional
            Color azulPrimario = new Color(26, 64, 128);
            Color azulClaro = new Color(240, 245, 252);
            Color azulAcento = new Color(41, 128, 185);
            Color grisBorde = new Color(210, 215, 225);
            Color grisFila = new Color(248, 249, 250);
            Color verdeInstitucional = new Color(39, 174, 96);

            // Tipografias OpenPDF
            Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.WHITE);
            Font fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, new Color(225, 235, 250));
            Font fuenteSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, azulPrimario);
            Font fuenteCabTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.WHITE);
            Font fuenteCelda = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);
            Font fuenteCeldaBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
            Font fuentePie = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
            Font fuenteCardTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, azulPrimario);
            Font fuenteGrisItalic = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

            // 1. Encabezado institucional
            PdfPTable tablaEncabezado = new PdfPTable(1);
            tablaEncabezado.setWidthPercentage(100);

            PdfPCell celdaClinica = new PdfPCell();
            celdaClinica.setBackgroundColor(azulPrimario);
            celdaClinica.setPadding(10);
            celdaClinica.setBorder(PdfPCell.NO_BORDER);

            Paragraph pClinica = new Paragraph(datos.getClinicaNombre().toUpperCase(), fuenteTitulo);
            pClinica.setAlignment(Element.ALIGN_CENTER);
            celdaClinica.addElement(pClinica);

            Paragraph pInfo = new Paragraph(
                    "NIT: " + datos.getClinicaNit() + "  |  Telefono: " + datos.getClinicaTel() + "  |  " + datos.getClinicaDir(),
                    fuenteSubtitulo
            );
            pInfo.setAlignment(Element.ALIGN_CENTER);
            celdaClinica.addElement(pInfo);
            tablaEncabezado.addCell(celdaClinica);
            document.add(tablaEncabezado);

            // 2. Titulo del documento
            PdfPTable tablaTitulo = new PdfPTable(1);
            tablaTitulo.setWidthPercentage(100);
            tablaTitulo.setSpacingBefore(4);
            tablaTitulo.setSpacingAfter(8);

            PdfPCell celdaTituloDoc = new PdfPCell();
            celdaTituloDoc.setBackgroundColor(azulClaro);
            celdaTituloDoc.setBorderColor(azulAcento);
            celdaTituloDoc.setPadding(6);

            Paragraph pDocTitulo = new Paragraph("CARNET DE SALUD Y CONTROL CLINICO VETERINARIO", fuenteCardTitulo);
            pDocTitulo.setAlignment(Element.ALIGN_CENTER);
            celdaTituloDoc.addElement(pDocTitulo);
            tablaTitulo.addCell(celdaTituloDoc);
            document.add(tablaTitulo);

            // 3. Informacion de la Mascota y Fotografia (Disposicion en 2 columnas)
            PdfPTable tablaPacienteYFoto = new PdfPTable(new float[]{3.2f, 1.3f});
            tablaPacienteYFoto.setWidthPercentage(100);
            tablaPacienteYFoto.setSpacingAfter(8);

            // Columna Izquierda: Ficha del paciente y propietario
            PdfPCell cFicha = new PdfPCell();
            cFicha.setBackgroundColor(Color.WHITE);
            cFicha.setBorderColor(grisBorde);
            cFicha.setPadding(8);

            cFicha.addElement(new Paragraph("DATOS DEL PACIENTE", fuenteSeccion));
            cFicha.addElement(new Paragraph("ID Paciente: #" + datos.getMascotaId() + "  |  Nombre: " + datos.getMascotaNombre(), fuenteCeldaBold));
            cFicha.addElement(new Paragraph("Especie / Raza: " + datos.getEspecie() + " / " + datos.getRaza(), fuenteCelda));
            cFicha.addElement(new Paragraph("Sexo: " + datos.getSexo() + "  |  Nacimiento: " + datos.getFechaNacimiento() + " (" + datos.getEdadCalculada() + ")", fuenteCelda));
            cFicha.addElement(new Paragraph(String.format("Peso Actual: %.2f kg  |  Microchip: %s",
                    datos.getPesoActual(), (datos.getMicrochip() != null ? datos.getMicrochip() : "No implantado")), fuenteCelda));
            cFicha.addElement(new Paragraph("Tatuaje: " + (datos.getTatuaje() != null ? datos.getTatuaje() : "No registrado"), fuenteCelda));

            cFicha.addElement(new Paragraph(" ", fuenteGrisItalic));
            cFicha.addElement(new Paragraph("PROPIETARIO / TUTOR RESPONSABLE", fuenteSeccion));
            cFicha.addElement(new Paragraph("Responsable: " + datos.getDuenoNombre() + " (Cliente ID: #" + datos.getDuenoId() + ")", fuenteCeldaBold));

            cFicha.addElement(new Paragraph(" ", fuenteGrisItalic));
            cFicha.addElement(new Paragraph("ANTECEDENTES CLINICOS", fuenteSeccion));
            cFicha.addElement(new Paragraph("Alergias: " + (datos.getAlergias() != null ? datos.getAlergias() : "Ninguna reportada"), fuenteCelda));
            cFicha.addElement(new Paragraph("Condiciones: " + (datos.getCondicionesPreexistentes() != null ? datos.getCondicionesPreexistentes() : "Sin condiciones preexistentes"), fuenteCelda));

            tablaPacienteYFoto.addCell(cFicha);

            // Columna Derecha: Recuadro de la Fotografia
            PdfPCell cFoto = new PdfPCell();
            cFoto.setBackgroundColor(azulClaro);
            cFoto.setBorderColor(grisBorde);
            cFoto.setPadding(6);
            cFoto.setHorizontalAlignment(Element.ALIGN_CENTER);

            Paragraph pFotoLabel = new Paragraph("FOTOGRAFIA", fuenteSeccion);
            pFotoLabel.setAlignment(Element.ALIGN_CENTER);
            cFoto.addElement(pFotoLabel);

            Image imgFoto = cargarImagen(datos.getFotoUrl());
            if (imgFoto != null) {
                imgFoto.scaleToFit(115f, 125f);
                imgFoto.setAlignment(Element.ALIGN_CENTER);
                imgFoto.setSpacingBefore(4);
                cFoto.addElement(imgFoto);
            } else {
                Paragraph pSinFoto = new Paragraph("\n\n\n[ Sin Fotografia ]\n\n\n", fuenteGrisItalic);
                pSinFoto.setAlignment(Element.ALIGN_CENTER);
                cFoto.addElement(pSinFoto);
            }
            tablaPacienteYFoto.addCell(cFoto);
            document.add(tablaPacienteYFoto);

            DateTimeFormatter dtfFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dtfHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // 4. Historial de Vacunacion y Biologicos
            Paragraph pSecVac = new Paragraph("HISTORIAL DE VACUNACION Y BIOLOGICOS", fuenteSeccion);
            pSecVac.setSpacingAfter(4);
            document.add(pSecVac);

            PdfPTable tablaVacunas = new PdfPTable(new float[]{1.1f, 2.5f, 1.2f, 1.1f, 2.1f});
            tablaVacunas.setWidthPercentage(100);
            tablaVacunas.setSpacingAfter(10);

            String[] cabVacunas = {"Fecha Apl.", "Vacuna / Biologico", "Lote", "Prox. Dosis", "Profesional"};
            for (String h : cabVacunas) {
                PdfPCell c = new PdfPCell(new Phrase(h, fuenteCabTabla));
                c.setBackgroundColor(azulPrimario);
                c.setPadding(5);
                c.setBorderColor(grisBorde);
                tablaVacunas.addCell(c);
            }

            if (datos.getVacunas().isEmpty()) {
                PdfPCell cVacia = new PdfPCell(new Phrase("(No se registran vacunas aplicadas para esta mascota)", fuenteGrisItalic));
                cVacia.setColspan(5);
                cVacia.setPadding(7);
                cVacia.setBorderColor(grisBorde);
                cVacia.setHorizontalAlignment(Element.ALIGN_CENTER);
                tablaVacunas.addCell(cVacia);
            } else {
                boolean alternate = false;
                for (VacunaHistorialItem v : datos.getVacunas()) {
                    Color bg = alternate ? grisFila : Color.WHITE;
                    alternate = !alternate;

                    String fApl = v.getFechaAplicacion() != null ? v.getFechaAplicacion().format(dtfFecha) : "N/D";
                    String fProx = v.getProximaDosis() != null ? v.getProximaDosis().format(dtfFecha) : "N/D";

                    tablaVacunas.addCell(crearCelda(fApl, fuenteCelda, bg, grisBorde));
                    tablaVacunas.addCell(crearCelda(v.getNombreVacuna(), fuenteCeldaBold, bg, grisBorde));
                    tablaVacunas.addCell(crearCelda(v.getLote() != null ? v.getLote() : "-", fuenteCelda, bg, grisBorde));
                    tablaVacunas.addCell(crearCelda(fProx, fuenteCelda, bg, grisBorde));
                    tablaVacunas.addCell(crearCelda(v.getVeterinario() != null ? v.getVeterinario() : "-", fuenteCelda, bg, grisBorde));
                }
            }
            document.add(tablaVacunas);

            // 5. Historial de Cirugias y Procedimientos Quirurgicos
            Paragraph pSecCir = new Paragraph("HISTORIAL DE CIRUGIAS Y PROCEDIMIENTOS ESPECIALES", fuenteSeccion);
            pSecCir.setSpacingAfter(4);
            document.add(pSecCir);

            PdfPTable tablaCirugias = new PdfPTable(new float[]{1.4f, 2.6f, 1.2f, 2.0f});
            tablaCirugias.setWidthPercentage(100);
            tablaCirugias.setSpacingAfter(14);

            String[] cabCirugias = {"Fecha / Hora", "Procedimiento", "Estado", "Cirujano"};
            for (String h : cabCirugias) {
                PdfPCell c = new PdfPCell(new Phrase(h, fuenteCabTabla));
                c.setBackgroundColor(azulAcento);
                c.setPadding(5);
                c.setBorderColor(grisBorde);
                tablaCirugias.addCell(c);
            }

            if (datos.getCirugias().isEmpty()) {
                PdfPCell cVacia = new PdfPCell(new Phrase("(No se registran procedimientos quirurgicos para esta mascota)", fuenteGrisItalic));
                cVacia.setColspan(4);
                cVacia.setPadding(7);
                cVacia.setBorderColor(grisBorde);
                cVacia.setHorizontalAlignment(Element.ALIGN_CENTER);
                tablaCirugias.addCell(cVacia);
            } else {
                boolean alternate = false;
                for (ProcedimientoEspecial p : datos.getCirugias()) {
                    Color bg = alternate ? grisFila : Color.WHITE;
                    alternate = !alternate;

                    String fProc = p.getFechaHora() != null ? p.getFechaHora().format(dtfHora) : "N/D";
                    String estado = p.getEstado() != null ? p.getEstado().getValorDb() : "N/D";
                    String cirujano = p.getNombreVeterinario() != null ? p.getNombreVeterinario() : "No asignado";

                    tablaCirugias.addCell(crearCelda(fProc, fuenteCelda, bg, grisBorde));
                    tablaCirugias.addCell(crearCelda(p.getNombreProcedimiento(), fuenteCeldaBold, bg, grisBorde));
                    tablaCirugias.addCell(crearCelda(estado, fuenteCelda, bg, grisBorde));
                    tablaCirugias.addCell(crearCelda(cirujano, fuenteCelda, bg, grisBorde));

                    // Detalles adicionales del procedimiento si existen
                    boolean hasDetalle = p.getDetalleProcedimiento() != null && !p.getDetalleProcedimiento().trim().isEmpty();
                    boolean hasPost = p.getSeguimientoPostoperatorio() != null && !p.getSeguimientoPostoperatorio().trim().isEmpty();
                    boolean hasControl = p.getProximoControl() != null;

                    if (hasDetalle || hasPost || hasControl) {
                        StringBuilder sb = new StringBuilder();
                        if (hasDetalle) sb.append("Detalle: ").append(p.getDetalleProcedimiento()).append("  |  ");
                        if (hasPost) sb.append("Postoperatorio: ").append(p.getSeguimientoPostoperatorio()).append("  |  ");
                        if (hasControl) sb.append("Proximo Control: ").append(p.getProximoControl().format(dtfFecha));

                        PdfPCell cDet = new PdfPCell(new Phrase(sb.toString(), fuenteGrisItalic));
                        cDet.setColspan(4);
                        cDet.setBackgroundColor(bg);
                        cDet.setBorderColor(grisBorde);
                        cDet.setPadding(4);
                        tablaCirugias.addCell(cDet);
                    }
                }
            }
            document.add(tablaCirugias);

            // 6. Pie de pagina con fecha de emision y bloque de firma profesional
            PdfPTable tablaFirma = new PdfPTable(new float[]{1.5f, 1.5f});
            tablaFirma.setWidthPercentage(100);
            tablaFirma.setSpacingBefore(10);

            PdfPCell cFecha = new PdfPCell();
            cFecha.setBorder(PdfPCell.NO_BORDER);
            cFecha.addElement(new Paragraph("Fecha de Emision: " + datos.getFechaEmision().format(dtfFecha), fuentePie));
            cFecha.addElement(new Paragraph("Documento oficial generado por VeterinarySystem.", fuentePie));
            tablaFirma.addCell(cFecha);

            PdfPCell cFirmaBox = new PdfPCell();
            cFirmaBox.setBorder(PdfPCell.NO_BORDER);
            Paragraph pLinea = new Paragraph("_____________________________________________", fuenteCelda);
            pLinea.setAlignment(Element.ALIGN_CENTER);
            Paragraph pFirma = new Paragraph("Firma y Sello del Medico Veterinario", fuenteCeldaBold);
            pFirma.setAlignment(Element.ALIGN_CENTER);
            Paragraph pMatricula = new Paragraph("Registro Profesional / Matricula Clinica", fuentePie);
            pMatricula.setAlignment(Element.ALIGN_CENTER);

            cFirmaBox.addElement(pLinea);
            cFirmaBox.addElement(pFirma);
            cFirmaBox.addElement(pMatricula);
            tablaFirma.addCell(cFirmaBox);

            document.add(tablaFirma);

            document.close();
            return archivo.getAbsolutePath();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el documento PDF del carnet de salud: " + e.getMessage(), e);
        }
    }

    // Carga de forma segura una imagen desde URL o ruta de archivo local
    private Image cargarImagen(String fotoUrl) {
        if (fotoUrl == null || fotoUrl.trim().isEmpty()) {
            return null;
        }
        try {
            String urlLimpia = fotoUrl.trim();
            if (urlLimpia.startsWith("http://") || urlLimpia.startsWith("https://")) {
                URL url = URI.create(urlLimpia).toURL();
                URLConnection conn = url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(4000);
                try (InputStream in = conn.getInputStream()) {
                    byte[] bytes = in.readAllBytes();
                    return Image.getInstance(bytes);
                }
            } else {
                File archivo = new File(urlLimpia);
                if (archivo.exists() && archivo.isFile()) {
                    return Image.getInstance(archivo.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            LoggerManager.warning(MODULO, "No se pudo cargar la imagen de la mascota desde '" + fotoUrl + "': " + e.getMessage());
        }
        return null;
    }

    private PdfPCell crearCelda(String texto, Font fuente, Color fondo, Color borde) {
        PdfPCell c = new PdfPCell(new Phrase(texto != null ? texto : "", fuente));
        c.setBackgroundColor(fondo);
        c.setBorderColor(borde);
        c.setPadding(5);
        return c;
    }
}
