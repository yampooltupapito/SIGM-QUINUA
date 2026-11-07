package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

import lombok.Getter;

@Getter
public enum OrigenDocumentoSeguridad {
    CIUDADANO("Carga Ciudadana / Portal", "Archivo remitido mediante reporte vecinal o alerta ciudadana"),
    ADMINISTRATIVO("Carga Administrativa de Serenazgo", "Incorporado por efectivos o supervisores de serenazgo"),
    SISTEMA("Generado por el Sistema", "Reporte, ficha o documento emitido automáticamente por SIGM"),
    CANAL_INSTITUCIONAL("Canal Institucional Autorizado", "Remitido por PNP, fiscalía, juzgado u otra entidad oficial");

    private final String descripcion;
    private final String detalle;

    OrigenDocumentoSeguridad(String descripcion, String detalle) {
        this.descripcion = descripcion;
        this.detalle = detalle;
    }
}
