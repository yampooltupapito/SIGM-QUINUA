package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Canales y tipos de contacto registrados para el contribuyente.
 */
@Getter
@RequiredArgsConstructor
public enum TipoContactoContribuyente {
    TELEFONO_MOVIL("Teléfono Celular"),
    TELEFONO_FIJO("Teléfono Fijo"),
    CORREO_ELECTRONICO("Correo Electrónico"),
    CASILLA_ELECTRONICA("Casilla Electrónica Municipal"),
    OTRO("Otro Canal");

    private final String descripcion;
}
