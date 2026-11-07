package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de representación legal y apoderados para personas jurídicas o naturales con poder.
 */
@Getter
@RequiredArgsConstructor
public enum TipoRepresentacionLegal {
    GERENTE_GENERAL("Gerente General"),
    APODERADO_LEGAL("Apoderado Legal"),
    PRESIDENTE_DIRECTORIO("Presidente del Directorio"),
    ADMINISTRADOR_JUDICIAL("Administrador Judicial"),
    REPRESENTANTE_SUCESION("Representante de Sucesión Indivisa"),
    OTRO("Otro Tipo de Representación");

    private final String descripcion;
}
