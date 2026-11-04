package randolh.veterinarysystem.core.exportador.reporte;

import java.util.Collections;
import java.util.List;

// Modelo inmutable que encapsula los datos requeridos para exportar un reporte tabular
public class ReporteExportData {

    private final String titulo;
    private final List<String> cabeceras;
    private final List<List<String>> filas;

    public ReporteExportData(String titulo, List<String> cabeceras, List<List<String>> filas) {
        this.titulo = titulo != null ? titulo : "REPORTE";
        this.cabeceras = cabeceras != null ? List.copyOf(cabeceras) : Collections.emptyList();
        this.filas = filas != null ? List.copyOf(filas) : Collections.emptyList();
    }

    public String getTitulo() { return titulo; }
    public List<String> getCabeceras() { return cabeceras; }
    public List<List<String>> getFilas() { return filas; }
}

