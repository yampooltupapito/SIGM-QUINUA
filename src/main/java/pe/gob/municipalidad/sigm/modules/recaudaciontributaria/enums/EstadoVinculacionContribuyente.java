package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados del procedimiento de vinculación de cuenta tributaria ciudadana.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoVinculacionContribuyente {
    PENDIENTE("Pendiente de Revisión", "bg-amber-100 text-amber-800 border-amber-200"),
    EN_VERIFICACION("En Cotejo Documental", "bg-blue-100 text-blue-800 border-blue-200"),
    OBSERVADA("Observada por Subsanar", "bg-orange-100 text-orange-800 border-orange-200"),
    APROBADA("Aprobada y Habilitada", "bg-emerald-100 text-emerald-800 border-emerald-200"),
    RECHAZADA("Rechazada / Denegada", "bg-rose-100 text-rose-800 border-rose-200"),
    REVOCADA("Revocada / Caducada", "bg-slate-100 text-slate-700 border-slate-200");

    private final String etiqueta;
    private final String badgeClass;
}

