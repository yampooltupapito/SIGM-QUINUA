package pe.gob.municipalidad.sigm.modules.mercados.enums;

import lombok.Getter;

@Getter
public enum OrigenIncidencia {
    PERSONAL_MUNICIPAL("Personal Municipal", "Detectado por inspectores o servidores en funciones"),
    ADMINISTRACION_MERCADO("Administración del Mercado", "Reportado por el administrador o supervisor del mercado"),
    INSPECCION("Diligencia de Inspección", "Derivado de un hallazgo durante una supervisión formal"),
    COMUNICACION_CIUDADANA("Comunicación Ciudadana", "Reporte presentado por vecinos, usuarios o comerciantes"),
    REPORTE_COMERCIANTE("Reporte de Comerciante", "Reportado por comerciante titular desde el portal"),
    RONDA_INTERNA("Ronda Interna", "Detectado durante rondas o patrullajes de vigilancia interna"),
    EXPEDIENTE("Expediente Administrativo", "Originado en trámite documental o expediente formal"),
    OTRO("Otro Origen Autorizado", "Otros canales institucionales habilitados");

    private final String label;
    private final String descripcion;

    OrigenIncidencia(String label, String descripcion) {
        this.label = label;
        this.descripcion = descripcion;
    }
}
