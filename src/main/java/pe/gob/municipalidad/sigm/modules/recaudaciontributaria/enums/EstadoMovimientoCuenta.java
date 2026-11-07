package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estado del asiento contable en el libro de cuenta corriente tributaria.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoMovimientoCuenta {
    REGISTRADO("Registrado", "bg-blue-500/10 text-blue-400 border-blue-500/20"),
    CONCILIADO("Conciliado", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    REVERSADO("Reversado", "bg-rose-500/10 text-rose-400 border-rose-500/20");

    private final String descripcion;
    private final String badgeClass;
}
