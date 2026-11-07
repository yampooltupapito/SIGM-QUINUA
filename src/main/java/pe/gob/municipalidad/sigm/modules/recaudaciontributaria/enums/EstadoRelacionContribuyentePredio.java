package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoRelacionContribuyentePredio {
    ACTIVO("Vigente", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    HISTORICO("Histórico / Transferido", "bg-slate-500/10 text-slate-400 border-slate-500/20"),
    REVOCADO("Revocado", "bg-rose-500/10 text-rose-400 border-rose-500/20"),
    EN_DISPUTA("En Disputa Legal", "bg-amber-500/10 text-amber-400 border-amber-500/20");

    private final String descripcion;
    private final String badgeClass;
}
