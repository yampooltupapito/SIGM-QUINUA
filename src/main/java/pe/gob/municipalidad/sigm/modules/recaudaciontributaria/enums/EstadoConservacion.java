package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estado de conservación técnica y física de las construcciones.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoConservacion {
    MUY_BUENO("Muy Bueno", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    BUENO("Bueno", "bg-blue-500/10 text-blue-400 border-blue-500/20"),
    REGULAR("Regular", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    MALO("Malo", "bg-orange-500/10 text-orange-400 border-orange-500/20"),
    RUINOSO("Muy Malo / Ruinoso", "bg-rose-500/10 text-rose-400 border-rose-500/20");

    private final String descripcion;
    private final String badgeClass;

    public String getNombre() {
        return descripcion;
    }
}
