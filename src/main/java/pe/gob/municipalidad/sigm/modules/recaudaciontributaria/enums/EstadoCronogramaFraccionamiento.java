package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados del cronograma de fraccionamiento tributario.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoCronogramaFraccionamiento {
    SIMULADO("Simulado (Referencial)", "bg-slate-500/10 text-slate-400 border-slate-500/20"),
    VIGENTE("Vigente en Convenio", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    MODIFICADO("Modificado / Reprogramado", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    CANCELADO("Cancelado", "bg-rose-500/10 text-rose-400 border-rose-500/20");

    private final String descripcion;
    private final String badgeClass;
}
