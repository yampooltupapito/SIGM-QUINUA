package pe.gob.municipalidad.sigm.modules.mercados.enums;

public enum FormatoExportacion {
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
    PDF("application/pdf", ".pdf");

    private final String mimeType;
    private final String extension;

    FormatoExportacion(String mimeType, String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        return extension;
    }
}
