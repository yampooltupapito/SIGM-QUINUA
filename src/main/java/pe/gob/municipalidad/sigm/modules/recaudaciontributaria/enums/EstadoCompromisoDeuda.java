package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estado del compromiso informativo voluntario de pago asumido por el contribuyente.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoCompromisoDeuda {
    PENDIENTE("Pendiente de Vencimiento", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    CUMPLIDO("Cumplido (Pago Registrado)", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    INCUMPLIDO("Incumplido (Plazo Vencido)", "bg-rose-500/10 text-rose-400 border-rose-500/20"),
    CANCELADO("Cancelado Administrativamente", "bg-slate-500/10 text-slate-400 border-slate-500/20");

    private final String descripcion;
    private final String badgeClass;
}
