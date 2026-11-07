package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Resultado individual de evaluación de una determinación al generar obligaciones.
 */
@Getter
@RequiredArgsConstructor
public enum ResultadoGeneracionItem {
    EXITO("Generada Exitosamente", "text-emerald-400"),
    DUPLICADO("Omitida: Obligación Previa Existente", "text-amber-400"),
    NO_ELEGIBLE("No Elegible: Determinación No Aprobada", "text-slate-400"),
    ERROR("Error en Validación o Datos Faltantes", "text-rose-400");

    private final String descripcion;
    private final String styleClass;
}
