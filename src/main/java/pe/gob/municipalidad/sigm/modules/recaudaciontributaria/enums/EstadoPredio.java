package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados administrativos del predio tributario municipal.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoPredio {
    ACTIVO("Activo en Padrón", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    OBSERVADO("Observado / En Fiscalización", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    EN_REVISION("En Revisión Documentaria", "bg-blue-500/10 text-blue-400 border-blue-500/20"),
    EN_LITIGIO("En Litigio / Disputa Judicial", "bg-purple-500/10 text-purple-400 border-purple-500/20"),
    DEMOLIDO("Demolido / Sin Construcción", "bg-rose-500/10 text-rose-400 border-rose-500/20"),
    INACTIVO("Inactivo / Desincorporado", "bg-slate-500/10 text-slate-400 border-slate-500/20");

    private final String descripcion;
    private final String badgeClass;

    public String getNombre() {
        return descripcion;
    }
}
