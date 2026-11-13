package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoEventoHistorialAlerta {
    RECEPCION("Recepción del Reporte Ciudadano"),
    REGISTRO_ALERTA("Recepción del Reporte Ciudadano"),
    RECEPCION_ALERTA("Recepción del Reporte Ciudadano"),
    CLASIFICACION("Clasificación de Tipo de Hecho"),
    CLASIFICACION_ALERTA("Clasificación de Tipo de Hecho"),
    CAMBIO_PRIORIDAD("Ajuste de Prioridad Administrativa"),
    CAMBIO_PRIORIDAD_ALERTA("Ajuste de Prioridad Administrativa"),
    CAMBIO_ESTADO("Transición de Estado"),
    CAMBIO_ESTADO_ALERTA("Transición de Estado"),
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

    public static TipoEventoHistorialAlerta fromString(String valor) {
        if (valor == null || valor.isBlank()) {
            return RECEPCION;
        }
        for (TipoEventoHistorialAlerta tipo : values()) {
            if (tipo.name().equalsIgnoreCase(valor.trim())) {
                return tipo;
            }
        }
        return switch (valor.trim().toUpperCase()) {
            case "CLASIFICACION", "CLASIFICACION_ALERTA", "CLASIFICAR" -> CLASIFICACION;
            case "PRIORIDAD", "CAMBIO_PRIORIDAD", "CAMBIO_PRIORIDAD_ALERTA" -> CAMBIO_PRIORIDAD;
            case "ESTADO", "CAMBIO_ESTADO", "CAMBIO_ESTADO_ALERTA" -> CAMBIO_ESTADO;
            case "VINCULACION", "VINCULACION_INCIDENCIA", "VINCULAR" -> VINCULACION_INCIDENCIA;
            case "CIERRE", "CIERRE_ADMINISTRATIVO" -> CIERRE_ADMINISTRATIVO;
            case "DESCARTE", "DESCARTAR" -> DESCARTE;
            default -> RECEPCION;
        };
    }
}
