package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoConceptoTributario {
    IMPUESTO_PREDIAL("Impuesto Predial", "Impuesto a los predios urbanos y rústicos"),
    ARBITRIOS_MUNICIPALES("Arbitrios Municipales", "Tasas por servicios públicos municipales"),
    DERECHO_EMISION("Derecho de Emisión Mecanizada", "Derecho por actualización y cuponera"),
    TASA_ADMINISTRATIVA("Tasa Administrativa", "Tasas por licencias o trámites tributarios"),
    OTRO("Otro Concepto Tributario", "Otros conceptos expresamente autorizados por ley");

    private final String descripcion;
    private final String detalle;

    public String getNombre() {
        return descripcion;
    }
}
