package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Medio de contacto prioritario para avisos y comunicaciones tributarias.
 */
@Getter
@RequiredArgsConstructor
public enum MedioContactoPreferido {
    CORREO_ELECTRONICO("Correo Electrónico"),
    TELEFONO("Teléfono / WhatsApp"),
    NOTIFICACION_FISICA("Notificación Física en Domicilio"),
    CASILLA_ELECTRONICA("Casilla Electrónica");

    private final String descripcion;
}
