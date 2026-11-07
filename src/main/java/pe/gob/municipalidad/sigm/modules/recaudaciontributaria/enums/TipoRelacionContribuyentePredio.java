package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoRelacionContribuyentePredio {
    PROPIETARIO_UNICO("Propietario Único (100%)"),
    TITULAR_DECLARADO("Titular Declarado"),
    COPROPIETARIO("Copropietario / Condómino"),
    COPROPIETARIO_DECLARADO("Copropietario Declarado"),
    POSEEDOR("Poseedor / Ocupante Legítimo"),
    POSEEDOR_DECLARADO("Poseedor Declarado"),
    USUFRUCTUARIO("Usufructuario Registrado"),
    CONCESIONARIO("Concesionario Municipal / Estatal"),
    ARRENDATARIO("Arrendatario / Responsable Solidario"),
    RESPONSABLE_TRIBUTARIO("Responsable Tributario");

    private final String descripcion;

    public String getNombre() {
        return descripcion;
    }
}
