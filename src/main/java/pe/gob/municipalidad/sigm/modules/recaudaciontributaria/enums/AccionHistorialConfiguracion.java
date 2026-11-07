package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Acciones auditadas en el ciclo de vida de configuraciones tributarias.
 */
@Getter
@RequiredArgsConstructor
public enum AccionHistorialConfiguracion {
    CREACION("Creación de Borrador"),
    MODIFICACION_BORRADOR("Modificación de Parámetros"),
    ENVIO_REVISION("Envío a Revisión"),
    APROBACION("Aprobación Formal"),
    ACTIVACION("Activación y Puesta en Vigencia"),
    DESACTIVACION("Desactivación / Cese de Vigencia"),
    NUEVA_VERSION("Generación de Nueva Versión");

    private final String descripcion;

    public String getNombre() {
        return descripcion;
    }
}
