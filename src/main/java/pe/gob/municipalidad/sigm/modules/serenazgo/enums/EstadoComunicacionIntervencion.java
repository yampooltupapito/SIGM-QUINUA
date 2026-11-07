package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoComunicacionIntervencion {
    PREPARADA("Preparada", "Comunicación redactada y lista para transmisión"),
    ENVIADA("Enviada", "Transmitida por el canal operativo correspondiente"),
    RECEPCION_CONFIRMADA("Recepción Confirmada", "Recepción confirmada expresamente por la entidad destinataria"),
    RESPUESTA_RECIBIDA("Respuesta Recibida", "Respuesta o apoyo institucional efectivamente recibido");

    private final String etiqueta;
    private final String descripcion;

    EstadoComunicacionIntervencion(String etiqueta, String descripcion) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
