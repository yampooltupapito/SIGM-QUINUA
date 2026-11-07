package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoCajaMunicipal {
    ACTIVA("Activa para Operaciones", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    INACTIVA("Inactiva / Fuera de Servicio", "bg-slate-500/10 text-slate-400 border-slate-500/20"),
    SUSPENDIDA("Suspendida Temporalmente", "bg-amber-500/10 text-amber-400 border-amber-500/20");

    private final String descripcion;
    private final String badgeClass;
}
