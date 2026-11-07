package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoNovedadPatrullaje {
    OBSERVACION_PREVENTIVA("Observación Preventiva"),
    CAMBIO_RECORRIDO("Cambio de Recorrido Autorizado"),
    PAUSA_OPERATIVA("Pausa Operativa Justificada"),
    SOLICITUD_APOYO("Solicitud de Apoyo a Central / Base"),
    INCIDENCIA_DETECTADA("Incidencia Detectada en Campo"),
    DESPERFECTO_UNIDAD("Desperfecto Técnico de Unidad Móvil"),
    OTRO("Otra Novedad Operativa");

    private final String descripcion;

    TipoNovedadPatrullaje(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
