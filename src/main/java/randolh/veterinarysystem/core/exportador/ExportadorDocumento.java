package randolh.veterinarysystem.core.exportador;

// Estrategia generica para la exportacion de documentos (Open/Closed Principle)
public interface ExportadorDocumento<T> {

    // Ejecuta la exportacion del modelo al formato especifico y retorna la ruta del archivo generado
    String exportar(T modelo);

    // Retorna el formato soportado por esta estrategia
    FormatoExportacion getFormato();
}

