package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoEvidenciaSeguridad {
    FOTOGRAFIA("Fotografía Digital"),
    VIDEO("Video Grabación / Bodycam"),
    AUDIO("Audio Grabación de Llamada"),
    ACTA_ESCRITA("Acta de Ocurrencia / Intervención"),
    PARTE_INFORMATIVO("Parte Informativo Escrito"),
    OTRO("Otro Documento Sustentatorio");

    private final String descripcion;

    TipoEvidenciaSeguridad(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
