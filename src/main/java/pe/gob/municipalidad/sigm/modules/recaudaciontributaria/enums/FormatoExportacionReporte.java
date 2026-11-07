package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Formatos admitidos para la exportación de reportes tributarios institucionales.
 */
@Getter
@RequiredArgsConstructor
public enum FormatoExportacionReporte {
    PDF("Documento PDF", "application/pdf", ".pdf"),
    EXCEL("Libro Excel XLSX", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
    CSV("Archivo CSV", "text/csv; charset=UTF-8", ".csv");

    private final String descripcion;
    private final String mediaType;
    private final String extension;
}

