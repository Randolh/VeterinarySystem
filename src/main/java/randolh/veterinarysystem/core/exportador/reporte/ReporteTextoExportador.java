package randolh.veterinarysystem.core.exportador.reporte;

import randolh.veterinarysystem.core.exportador.ExportadorDocumento;
import randolh.veterinarysystem.core.exportador.FormatoExportacion;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Estrategia de exportacion de reportes tabulares a texto plano estructurado
public class ReporteTextoExportador implements ExportadorDocumento<ReporteExportData> {

    private static final String MODULO = "ReporteTextoExportador";
    private static final String CARPETA_REPORTES = "exportaciones/reportes";

    public ReporteTextoExportador() {
        new File(CARPETA_REPORTES).mkdirs();
    }

    @Override
    public FormatoExportacion getFormato() {
        return FormatoExportacion.TEXTO;
    }

    @Override
    public String exportar(ReporteExportData datos) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nombreArchivo = "reporte_" + timestamp + ".txt";
        File archivo = new File(CARPETA_REPORTES, nombreArchivo);

        try (FileWriter writer = new FileWriter(archivo, StandardCharsets.UTF_8)) {
            writer.write("================================================================================\n");
            writer.write(datos.getTitulo() + "\n");
            writer.write("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            writer.write("================================================================================\n\n");

            List<String> cabeceras = datos.getCabeceras();
            List<List<String>> filas = datos.getFilas();

            if (cabeceras != null && !cabeceras.isEmpty()) {
                // Calcular ancho optimo por columna
                List<Integer> anchos = new ArrayList<>();
                for (int i = 0; i < cabeceras.size(); i++) {
                    int max = cabeceras.get(i).length();
                    if (filas != null) {
                        for (List<String> fila : filas) {
                            if (i < fila.size() && fila.get(i) != null) {
                                max = Math.max(max, fila.get(i).length());
                            }
                        }
                    }
                    anchos.add(Math.min(max + 2, 40));
                }

                // Escribir cabecera
                StringBuilder sbCab = new StringBuilder();
                StringBuilder sbDiv = new StringBuilder();
                for (int i = 0; i < cabeceras.size(); i++) {
                    String formato = "%-" + anchos.get(i) + "s ";
                    sbCab.append(String.format(formato, cabeceras.get(i)));
                    sbDiv.append("-".repeat(anchos.get(i))).append(" ");
                }
                writer.write(sbCab.toString().trim() + "\n");
                writer.write(sbDiv.toString().trim() + "\n");

                // Escribir filas
                if (filas != null) {
                    for (List<String> fila : filas) {
                        StringBuilder sbFila = new StringBuilder();
                        for (int i = 0; i < cabeceras.size(); i++) {
                            String val = (i < fila.size() && fila.get(i) != null) ? fila.get(i) : "-";
                            String formato = "%-" + anchos.get(i) + "s ";
                            sbFila.append(String.format(formato, val));
                        }
                        writer.write(sbFila.toString().trim() + "\n");
                    }
                }
            }

            LoggerManager.info(MODULO, "Reporte TXT generado en: " + archivo.getAbsolutePath());
            return archivo.getAbsolutePath();

        } catch (IOException e) {
            LoggerManager.error(MODULO, "Error al generar reporte en texto plano", e);
            return null;
        }
    }
}

