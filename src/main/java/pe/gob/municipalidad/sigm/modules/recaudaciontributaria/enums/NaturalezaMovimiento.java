package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Naturaleza contable del movimiento en la cuenta corriente tributaria.
 * DÉBITO: Incrementa la deuda del contribuyente (cargo).
 * CRÉDITO: Disminuye la deuda del contribuyente (abono/anulación/pago).
 */
@Getter
@RequiredArgsConstructor
public enum NaturalezaMovimiento {
    DEBITO("Débito (Cargo / Deuda)", "text-rose-400 font-semibold"),
    CREDITO("Crédito (Abono / Descargo)", "text-emerald-400 font-semibold");

    private final String descripcion;
    private final String styleClass;
}
