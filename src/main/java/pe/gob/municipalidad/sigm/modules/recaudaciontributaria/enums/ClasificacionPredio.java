package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Clasificación físico-catastral del predio en el distrito de Quinua.
 */
@Getter
@RequiredArgsConstructor
public enum ClasificacionPredio {
    URBANO("Predio Urbano"),
    RURAL("Predio Rural"),
    CASA_HABITACION("Casa Habitación / Residencial"),
    COMERCIAL("Predio Comercial"),
    INDUSTRIAL("Predio Industrial / Taller Artesanal"),
    TERRENO_SIN_CONSTRUIR("Terreno sin Construir / Eriazo Urbano"),
    RUSTICO_AGRICOLA("Rústico Agrícola / Cultivo"),
    RUSTICO_PASTIZAL("Rústico Pastizal / Ganadero"),
    RUSTICO_ERIAZO("Rústico Eriazo"),
    INSTITUCIONAL("Uso Institucional / Público"),
    OTRO("Otro Tipo de Predio");

    private final String descripcion;

    public String getNombre() {
        return descripcion;
    }
}
