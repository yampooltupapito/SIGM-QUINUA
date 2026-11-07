package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Modalidad o tipo de procedimiento en el que se ejecuta el cálculo del Impuesto Predial.
 */
@Getter
@RequiredArgsConstructor
public enum TipoCalculoPredial {

    SIMULACION("Simulación Informativa", "Simulación técnica sin generación de obligaciones vinculantes"),
    DETERMINACION_INDIVIDUAL("Determinación Individual", "Liquidación y emisión individual ordinaria por contribuyente"),
    DETERMINACION_ANUAL("Determinación Masiva Anual", "Emisión general anual para el ejercicio fiscal"),
    FISCALIZACION("Fiscalización Posterior", "Determinación de oficio por fiscalización tributaria o inspección catastral"),
    RECTIFICATORIA("Declaración Rectificatoria", "Liquidación resultante de actualización o rectificación jurada");

    private final String nombre;
    private final String descripcion;
}
