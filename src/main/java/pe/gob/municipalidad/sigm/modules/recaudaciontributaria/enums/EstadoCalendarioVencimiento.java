package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados de cuotas del calendario tributario municipal.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoCalendarioVencimiento {
    PROGRAMADO("Programado", "bg-slate-500/10 text-slate-400 border-slate-500/20"),
    VIGENTE("Vigente / En Cobranza", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    VENCIDO("Vencido", "bg-rose-500/10 text-rose-400 border-rose-500/20"),
    PRORROGADO("Prorrogado por Decreto", "bg-blue-500/10 text-blue-400 border-blue-500/20");

    private final String descripcion;
    private final String badgeClass;

    public String getNombre() {
        return descripcion;
    }
}
