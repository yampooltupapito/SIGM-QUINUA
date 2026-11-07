package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Fuente técnica y nivel de precisión de las coordenadas geográficas del predio.
 */
@Getter
@RequiredArgsConstructor
public enum FuenteCoordenadas {
    LEVANTAMIENTO_GPS("Levantamiento GPS en Campo / Estación Total"),
    GPS_NAVEGADOR_DE_CAMPO("Levantamiento GPS en Campo / Estación Total"),
    MAPA_DIGITAL("Digitalización sobre Ortofoto / Mapa Satelital"),
    DECLARADA("Coordenada Declarada por el Contribuyente"),
    NO_DISPONIBLE("Sin Coordenadas Registradas");

    private final String descripcion;

    public String getNombre() {
        return descripcion;
    }
}
