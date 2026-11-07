package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Categorías de domicilio tributario para el contribuyente.
 */
@Getter
@RequiredArgsConstructor
public enum TipoDomicilioContribuyente {
    FISCAL("Domicilio Fiscal Registrado"),
    DECLARADO("Domicilio Declarado"),
    NOTIFICACION_LEGAL("Domicilio Procesal / Notificación"),
    REAL("Domicilio Real");

    private final String descripcion;
}
