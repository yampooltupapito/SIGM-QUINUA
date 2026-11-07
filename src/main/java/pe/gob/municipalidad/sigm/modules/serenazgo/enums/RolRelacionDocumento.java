package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

import lombok.Getter;

@Getter
public enum RolRelacionDocumento {
    RESPALDO("Documento de Respaldo", "Sustento administrativo general"),
    EVIDENCIA_PRIMARIA("Evidencia Primaria de Campo", "Registro directo fotográfico, video o acta levantada en el lugar"),
    ANEXO("Anexo Documental", "Documento adjunto o complementario"),
    INFORME_FINAL("Informe de Cierre", "Documento conclusivo de la actuación municipal");

    private final String descripcion;
    private final String detalle;

    RolRelacionDocumento(String descripcion, String detalle) {
        this.descripcion = descripcion;
        this.detalle = detalle;
    }
}
