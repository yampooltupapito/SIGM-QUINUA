package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de movimientos en la cuenta corriente tributaria.
 */
@Getter
@RequiredArgsConstructor
public enum TipoMovimientoCuentaCorriente {
    CARGO_OBLIGACION("Cargo por Determinación de Obligación", NaturalezaMovimiento.DEBITO),
    AJUSTE_AUTORIZADO("Ajuste Contable Autorizado", NaturalezaMovimiento.DEBITO),
    REVERSO_AJUSTE("Reverso de Ajuste", NaturalezaMovimiento.CREDITO),
    ANULACION_AUTORIZADA("Anulación Formal de Obligación", NaturalezaMovimiento.CREDITO),
    APLICACION_PAGO("Aplicación de Pago Recaudado", NaturalezaMovimiento.CREDITO),
    REVERSO_APLICACION_PAGO("Reverso de Pago Recaudado", NaturalezaMovimiento.DEBITO),
    APLICACION_CREDITO("Aplicación de Crédito / Saldo a Favor", NaturalezaMovimiento.CREDITO),
    REVERSO_CREDITO("Reverso de Crédito / Compensación", NaturalezaMovimiento.DEBITO);

    private final String descripcion;
    private final NaturalezaMovimiento naturalezaPorDefecto;
}
