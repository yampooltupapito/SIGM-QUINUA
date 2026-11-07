package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoSesionCaja {
    ABIERTA("Sesión Abierta (En Recaudación)", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    EN_CIERRE("En Proceso de Cierre / Arqueo", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    CERRADA("Sesión Cerrada y Cuadrada", "bg-blue-500/10 text-blue-400 border-blue-500/20"),
    OBSERVADA("Cerrada con Observaciones / Diferencias", "bg-rose-500/10 text-rose-400 border-rose-500/20");

    private final String descripcion;
    private final String badgeClass;
}
