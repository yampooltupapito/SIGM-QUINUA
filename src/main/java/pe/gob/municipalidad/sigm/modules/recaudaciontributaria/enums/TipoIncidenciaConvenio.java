package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de incidencias y actuaciones registradas en la bitácora de convenios de fraccionamiento.
 */
@Getter
@RequiredArgsConstructor
public enum TipoIncidenciaConvenio {
    CUOTA_VENCIDA("Cuota Vencida"),
    INCUMPLIMIENTO_PAGO("Incumplimiento de Cronograma"),
    SUSPENSION_SOLICITADA("Suspensión Administrativa Solicitada"),
    REPROGRAMACION_AUTORIZADA("Reprogramación Autorizada"),
    REVISION_ADMINISTRATIVA("Revisión de Cumplimiento"),
    RESOLUCION_PERDIDA("Resolución de Pérdida de Beneficio");

    private final String descripcion;
}
