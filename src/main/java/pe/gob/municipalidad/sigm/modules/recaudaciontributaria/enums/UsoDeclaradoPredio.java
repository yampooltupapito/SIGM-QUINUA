package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Destino y uso declarado del predio municipal.
 */
@Getter
@RequiredArgsConstructor
public enum UsoDeclaradoPredio {
    VIVIENDA("Vivienda Familiar"),
    COMERCIO("Actividad Comercial"),
    MIXTO("Uso Mixto (Vivienda y Comercio)"),
    AGROPECUARIO("Actividad Agrícola / Pecuaria"),
    AGRICOLA("Actividad Agrícola"),
    ARTESANAL("Taller Artesanal / Alfarería"),
    EDUCATIVO_CULTURAL("Educativo o Cultural"),
    SALUD("Servicios de Salud"),
    INSTITUCIONAL("Sede Institucional o Religiosa"),
    GOBIERNO("Sede Gubernamental / Municipal"),
    CULTO_EDUCACION("Culto / Educación"),
    DESOCUPADO("Predio Desocupado / Baldío"),
    OTRO("Otro Uso Declarado");

    private final String descripcion;

    public String getNombre() {
        return descripcion;
    }
}
