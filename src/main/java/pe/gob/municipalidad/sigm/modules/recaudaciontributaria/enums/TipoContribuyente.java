package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoContribuyente {
    PERSONA_NATURAL("Persona Natural"),
    PERSONA_JURIDICA("Persona Jurídica"),
    SUCESION_INDIVISA("Sucesión Indivisa"),
    SOCIEDAD_CONYUGAL("Sociedad Conyugal");

    private final String descripcion;
}
