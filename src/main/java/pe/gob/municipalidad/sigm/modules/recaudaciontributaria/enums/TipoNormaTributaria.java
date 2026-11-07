package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoNormaTributaria {
    ORDENANZA_MUNICIPAL("Ordenanza Municipal"),
    DECRETO_ALCALDIA("Decreto de Alcaldía"),
    LEY_NACIONAL("Ley / Decreto Legislativo"),
    RESOLUCION("Resolución de Alcaldía / Gerencial");

    private final String descripcion;
}
