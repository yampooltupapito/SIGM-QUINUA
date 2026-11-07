package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoConciliacionCaja {
    PENDIENTE("Pendiente de Revisión", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    EN_REVISION("En Revisión por Tesorería", "bg-blue-500/10 text-blue-400 border-blue-500/20"),
    CONCILIADA("Conciliada y Aprobada", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    CON_DIFERENCIAS("Conciliada con Diferencias", "bg-purple-500/10 text-purple-400 border-purple-500/20"),
    OBSERVADA("Observada por Discrepancias", "bg-rose-500/10 text-rose-400 border-rose-500/20");

    private final String descripcion;
    private final String badgeClass;
}
