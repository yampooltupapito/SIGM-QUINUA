package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Acciones registradas en el historial de auditoría de una obligación tributaria.
 */
@Getter
@RequiredArgsConstructor
public enum AccionHistorialObligacion {
    GENERACION("Generación Inicial de Obligación"),
    VALIDACION("Validación Administrativa Formal"),
    SUSPENSION("Suspensión por Reclamo / Procedimiento"),
    RECTIFICACION("Rectificación por Nueva Determinación"),
    ANULACION("Anulación Formal de Obligación"),
    CARGO_CUOTA("Generación / Desglose de Cuotas"),
    AJUSTE("Ajuste Contable Autorizado");

    private final String descripcion;
}
