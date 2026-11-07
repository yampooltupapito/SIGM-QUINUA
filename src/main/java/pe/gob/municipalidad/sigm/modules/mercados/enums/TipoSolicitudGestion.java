package pe.gob.municipalidad.sigm.modules.mercados.enums;

import lombok.Getter;

@Getter
public enum TipoSolicitudGestion {
    RENOVACION_ASIGNACION("Renovación de Asignación de Puesto"),
    RENOVACION_CONTRATO("Renovación de Contrato de Ocupación"),
    RENOVACION_AUTORIZACION("Renovación de Autorización Comercial"),
    CAMBIO_TITULAR("Cambio de Titular de Puesto"),
    TRANSFERENCIA_AUTORIZADA("Transferencia Excepcional Autorizada");

    private final String label;

    TipoSolicitudGestion(String label) {
        this.label = label;
    }
}
