package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Acciones auditables en el historial de determinaciones y cálculos del Impuesto Predial.
 */
@Getter
@RequiredArgsConstructor
public enum AccionHistorialCalculoPredial {

    CREACION_SIMULACION("Simulación Realizada"),
    GUARDADO_PRELIMINAR("Cálculo Preliminar Guardado"),
    ENVIO_A_REVISION("Enviado a Revisión Técnica"),
    REGISTRO_OBSERVACIONES("Observaciones Técnicas Registradas"),
    APROBACION_CALCULO("Aprobación Formal de Cálculo"),
    ANULACION_CALCULO("Anulación de Cálculo"),
    RECALCULO_NUEVA_VERSION("Generación de Nueva Versión por Recálculo");

    private final String descripcion;
}
