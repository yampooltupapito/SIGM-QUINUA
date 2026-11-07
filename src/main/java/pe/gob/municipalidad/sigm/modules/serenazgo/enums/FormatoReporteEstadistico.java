package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Formato de exportación de reportes analíticos de Serenazgo.
 */
@Getter
@RequiredArgsConstructor
public enum FormatoReporteEstadistico {

    PDF("Documento Portátil (PDF)", "application/pdf", ".pdf"),
    EXCEL("Libro de Cálculo (XLSX)", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx");

    private final String descripcion;
    private final String mimeType;
    private final String extension;
}
