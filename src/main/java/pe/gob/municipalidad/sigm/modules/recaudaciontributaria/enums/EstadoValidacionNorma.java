package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoValidacionNorma {
    REGISTRADA("Registrada sin Validación", "bg-slate-500/10 text-slate-400 border-slate-500/20"),
    EN_REVISION("En Revisión Legal", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    VALIDADA_LEGAL("Validada Jurídicamente", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    RECHAZADA("Rechazada / Inaplicable", "bg-rose-500/10 text-rose-400 border-rose-500/20");

    private final String descripcion;
    private final String badgeClass;
}
