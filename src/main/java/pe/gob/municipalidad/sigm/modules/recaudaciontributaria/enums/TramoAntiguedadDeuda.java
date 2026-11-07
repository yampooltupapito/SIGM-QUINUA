package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tramos administrativos de antigüedad de deuda en días de morosidad a una fecha de corte.
 */
@Getter
@RequiredArgsConstructor
public enum TramoAntiguedadDeuda {
    NO_VENCIDA("Corriente (No Vencida)", "0 días", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    DE_01_A_30_DIAS("1 a 30 días", "1 - 30 días", "bg-sky-500/10 text-sky-400 border-sky-500/20"),
    DE_31_A_60_DIAS("31 a 60 días", "31 - 60 días", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    DE_61_A_90_DIAS("61 a 90 días", "61 - 90 días", "bg-orange-500/10 text-orange-400 border-orange-500/20"),
    MAS_DE_90_DIAS("Mayor a 90 días", "> 90 días", "bg-rose-500/10 text-rose-400 border-rose-500/20");

    private final String etiqueta;
    private final String rango;
    private final String badgeClass;

    public static TramoAntiguedadDeuda calcularTramo(long diasMorosidad) {
        if (diasMorosidad <= 0) {
            return NO_VENCIDA;
        } else if (diasMorosidad <= 30) {
            return DE_01_A_30_DIAS;
        } else if (diasMorosidad <= 60) {
            return DE_31_A_60_DIAS;
        } else if (diasMorosidad <= 90) {
            return DE_61_A_90_DIAS;
        } else {
            return MAS_DE_90_DIAS;
        }
    }
}
