package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoEventoHistorialAlerta {
    RECEPCION("Recepción del Reporte Ciudadano"),
    CLASIFICACION("Clasificación de Tipo de Hecho"),
    CAMBIO_PRIORIDAD("Ajuste de Prioridad Administrativa"),
    CAMBIO_ESTADO("Transición de Estado"),
    SOLICITUD_INFORMACION("Solicitud de Información Complementaria"),
    RESPUESTA_CIUDADANO("Respuesta Recibida del Ciudadano"),
    VINCULACION_INCIDENCIA("Vinculación con Incidencia Formal"),
    CIERRE_ADMINISTRATIVO("Cierre Administrativo del Reporte"),
    DESCARTE("Descarte Administrativo"),
    DOCUMENTO_ADJUNTO("Anexo o Fotografía Adjuntada"),
    OBSERVACION_INTERNA("Anotación Interna de Operador");

    private final String descripcion;

    TipoEventoHistorialAlerta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
