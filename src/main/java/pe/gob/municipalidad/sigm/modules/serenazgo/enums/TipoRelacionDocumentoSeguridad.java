package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

import lombok.Getter;

@Getter
public enum TipoRelacionDocumentoSeguridad {
    INCIDENCIA("Incidencia de Seguridad", "Incidencia registrada en el Centro de Operaciones", "/dashboard/serenazgo/incidencias/"),
    ALERTA("Alerta Ciudadana", "Alerta reportada por vecino o canal ciudadano", "/dashboard/serenazgo/alertas/"),
    ATENCION("Atención de Incidencia", "Despacho o atención operativa de serenazgo", "/dashboard/serenazgo/atenciones/"),
    INTERVENCION("Intervención de Serenazgo", "Actuación o intervención de efectivos en campo", "/dashboard/serenazgo/intervenciones/"),
    PATRULLAJE("Patrullaje Municipal", "Turno o ronda de patrullaje preventivo", "/dashboard/serenazgo/patrullajes/");

    private final String descripcion;
    private final String detalle;
    private final String urlBase;

    TipoRelacionDocumentoSeguridad(String descripcion, String detalle, String urlBase) {
        this.descripcion = descripcion;
        this.detalle = detalle;
        this.urlBase = urlBase;
    }
}
