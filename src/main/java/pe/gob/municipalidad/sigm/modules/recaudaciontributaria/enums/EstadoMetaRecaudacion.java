package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados del ciclo de vida de una meta de recaudación tributaria.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoMetaRecaudacion {
    BORRADOR("Borrador", "bg-slate-100 text-slate-800 border-slate-300"),
    VIGENTE("Vigente y Aprobada", "bg-emerald-100 text-emerald-800 border-emerald-300"),
    MODIFICADA("Modificada / Reajustada", "bg-blue-100 text-blue-800 border-blue-300"),
    CERRADA("Cerrada por Fin de Ejercicio", "bg-purple-100 text-purple-800 border-purple-300"),
    ANULADA("Anulada", "bg-rose-100 text-rose-800 border-rose-300");

    private final String etiqueta;
    private final String badgeClass;
}

