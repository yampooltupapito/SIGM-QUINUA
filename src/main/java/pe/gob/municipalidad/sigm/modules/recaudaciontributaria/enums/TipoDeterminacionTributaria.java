package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipo de determinación tributaria que da origen a la obligación.
 */
@Getter
@RequiredArgsConstructor
public enum TipoDeterminacionTributaria {
    IMPUESTO_PREDIAL("Impuesto Predial Anual", "IMP_PREDIAL"),
    ARBITRIOS_MUNICIPALES("Arbitrios Municipales", "ARBITRIOS"),
    DERECHO_EMISION("Derecho de Emisión Mecanizada", "DERECHO_EMISION"),
    OTROS("Otras Determinaciones Autorizadas", "OTROS");

    private final String descripcion;
    private final String codigoConceptoDefault;
}
