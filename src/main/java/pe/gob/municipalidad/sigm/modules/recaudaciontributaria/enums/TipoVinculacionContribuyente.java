package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de relación jurídica de vinculación entre una cuenta ciudadana y un contribuyente tributario.
 */
@Getter
@RequiredArgsConstructor
public enum TipoVinculacionContribuyente {
    TITULAR_VERIFICADO("Titular Verificado", "El usuario es la misma persona natural inscrita como contribuyente"),
    REPRESENTANTE_ACREDITADO("Representante Legal Acreditado", "Representante legal formalmente inscrito en SUNAT y acreditado ante la municipalidad"),
    APODERADO_ACREDITADO("Apoderado con Poder Registrado", "Apoderado facultado mediante poder notarial o administrativo con facultades tributarias"),
    OTRA_RELACION_AUTORIZADA("Otra Relación Autorizada", "Relación especial autorizada administrativamente (sucesión indivisa, curador, etc.)");

    private final String nombre;
    private final String descripcion;

    public String getEtiqueta() {
        return nombre;
    }
}

