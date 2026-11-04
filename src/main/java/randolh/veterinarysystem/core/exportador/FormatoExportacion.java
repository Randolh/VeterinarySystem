package randolh.veterinarysystem.core.exportador;

// Formatos soportados para la exportacion de documentos
public enum FormatoExportacion {
    PDF("pdf", "application/pdf"),
    TEXTO("txt", "text/plain");

    private final String extension;
    private final String mimeType;

    FormatoExportacion(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMimeType() {
        return mimeType;
    }
}

