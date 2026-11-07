package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados del ciclo de vida de la determinación y cálculo del Impuesto Predial Municipal.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoCalculoPredial {

    SIMULACION("Simulación", "Simulación informativa sin efectos tributarios ni determinación de deuda", "bg-indigo-500/20 text-indigo-400 border-indigo-500/30"),
    PRELIMINAR("Preliminar", "Cálculo técnico preliminar registrado en borrador", "bg-slate-500/20 text-slate-300 border-slate-500/30"),
    EN_REVISION("En Revisión", "Elevado para revisión técnica y fiscalización municipal", "bg-amber-500/20 text-amber-400 border-amber-500/30"),
    OBSERVADO("Observado", "Devuelto con observaciones técnicas o discrepancias catastrales", "bg-rose-500/20 text-rose-400 border-rose-500/30"),
    APROBADO("Aprobado", "Determinación formalmente aprobada y autorizada para emisión", "bg-emerald-500/20 text-emerald-400 border-emerald-500/30"),
    ANULADO("Anulado", "Cálculo revocado o anulado formalmente con justificación", "bg-red-950/60 text-red-400 border-red-800/40");

    private final String nombre;
    private final String descripcion;
    private final String badgeClass;

    public boolean esModificable() {
        return this == SIMULACION || this == PRELIMINAR || this == OBSERVADO;
    }

    public boolean esDefinitivo() {
        return this == APROBADO;
    }
}
