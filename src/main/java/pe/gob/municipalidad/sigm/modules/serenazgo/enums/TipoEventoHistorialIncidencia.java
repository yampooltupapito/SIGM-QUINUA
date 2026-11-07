package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoEventoHistorialIncidencia {
    REGISTRO("Registro Inicial de Incidencia"),
    ACTUALIZACION("Actualización Informativa de Hecho"),
    CAMBIO_ESTADO("Transición de Estado Operativo"),
    CAMBIO_PRIORIDAD("Actualización de Prioridad Administrativa"),
    ADJUNCION_DOCUMENTO("Documento o Registro Fotográfico Adjuntado"),
    COMENTARIO_AGREGADO("Observación Administrativa Registrada"),
    CIERRE_ADMINISTRATIVO("Cierre Administrativo del Caso"),
    DESCARTE("Descarte Administrativo / Duplicado");

    private final String descripcion;

    TipoEventoHistorialIncidencia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
