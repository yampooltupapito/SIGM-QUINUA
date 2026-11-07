package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de relación administrativa entre el contribuyente y el predio municipal.
 */
@Getter
@RequiredArgsConstructor
public enum TipoRelacionContribuyente {
    TITULAR_DECLARADO("Titular Declarado"),
    COPROPIETARIO_DECLARADO("Copropietario Declarado"),
    POSEEDOR_DECLARADO("Poseedor Declarado"),
    RESPONSABLE_TRIBUTARIO("Responsable Tributario"),
    PROPIETARIO_UNICO("Propietario Único"),
    USUFRUCTUARIO("Usufructuario Registrado"),
    ARRENDATARIO("Arrendatario / Responsable Solidario");

    private final String descripcion;

    public String getNombre() {
        return descripcion;
    }
}
