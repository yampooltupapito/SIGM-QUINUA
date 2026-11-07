package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoPeriodoTributario {
    CONFIGURADO("Configurado", "bg-slate-500/10 text-slate-400 border-slate-500/20"),
    APERTURADO("Aperturado / Vigente", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    CERRADO("Cerrado Contablemente", "bg-blue-500/10 text-blue-400 border-blue-500/20");

    private final String descripcion;
    private final String badgeClass;
}
