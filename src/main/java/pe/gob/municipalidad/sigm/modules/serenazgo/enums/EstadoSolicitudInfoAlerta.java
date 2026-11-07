package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoSolicitudInfoAlerta {
    PENDIENTE("Pendiente de Respuesta"),
    RESPONDIDA("Respondida por el Ciudadano"),
    EXPIRADA("Expirada sin Respuesta");

    private final String descripcion;

    EstadoSolicitudInfoAlerta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
