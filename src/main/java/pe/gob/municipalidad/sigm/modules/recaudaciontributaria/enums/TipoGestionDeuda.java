package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de actuación y gestión administrativa autorizada sobre el seguimiento de deudas.
 */
@Getter
@RequiredArgsConstructor
public enum TipoGestionDeuda {
    REVISION_INTERNA("Revisión Interna de Obligación"),
    VALIDACION_INFORMACION("Validación de Información Predial/Tributaria"),
    CONTACTO_ADMINISTRATIVO("Contacto Administrativo con Contribuyente"),
    ATENCION_PRESENCIAL("Atención Presencial en Ventanilla"),
    OBSERVACION("Registro de Observación o Incidencia"),
    ACTUALIZACION_SEGUIMIENTO("Actualización de Estado de Seguimiento"),
    DERIVACION_INTERNA("Derivación a Oficina Competente");

    private final String descripcion;
}
