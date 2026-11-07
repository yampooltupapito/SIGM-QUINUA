package pe.gob.municipalidad.sigm.modules.mercados.enums;

import lombok.Getter;

@Getter
public enum EstadoVersionPlano {
    BORRADOR("Borrador en Edición"),
    EN_REVISION("En Revisión Técnica"),
    PUBLICADA("Publicada / Vigente"),
    ARCHIVADA("Histórica / Archivada");

    private final String label;

    EstadoVersionPlano(String label) {
        this.label = label;
    }
}
