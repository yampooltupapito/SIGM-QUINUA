package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoDocumentoPersonalSerenazgo {
    CONTRATO("Contrato de Trabajo / Locación"),
    CONSTANCIA_CAPACITACION("Constancia de Capacitación y Entrenamiento"),
    DESIGNACION("Resolución / Memorando de Designación"),
    CERTIFICADO_MEDICO("Certificado Médico y Aptitud Física"),
    RECORD_CONDUCTOR("Récord de Conductor MTC"),
    ANTECEDENTES("Certificado de Antecedentes Policiales/Judiciales"),
    OTRO("Otro Documento Sustentatorio");

    private final String descripcion;

    TipoDocumentoPersonalSerenazgo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
