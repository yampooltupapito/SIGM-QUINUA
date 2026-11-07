package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;

public class PuestoEstadoRequest {

    private EstadoFisicoPuesto nuevoEstadoFisico;
    private EstadoPuesto nuevoEstadoOperativo;

    @NotBlank(message = "El motivo del cambio de estado es obligatorio")
    @Size(min = 5, max = 500, message = "El motivo debe tener entre 5 y 500 caracteres")
    private String motivo;

    private String observaciones;

    public PuestoEstadoRequest() {
    }

    public PuestoEstadoRequest(EstadoFisicoPuesto nuevoEstadoFisico, EstadoPuesto nuevoEstadoOperativo, String motivo, String observaciones) {
        this.nuevoEstadoFisico = nuevoEstadoFisico;
        this.nuevoEstadoOperativo = nuevoEstadoOperativo;
        this.motivo = motivo;
        this.observaciones = observaciones;
    }

    public static PuestoEstadoRequestBuilder builder() {
        return new PuestoEstadoRequestBuilder();
    }

    public EstadoFisicoPuesto getNuevoEstadoFisico() { return nuevoEstadoFisico; }
    public void setNuevoEstadoFisico(EstadoFisicoPuesto nuevoEstadoFisico) { this.nuevoEstadoFisico = nuevoEstadoFisico; }

    public EstadoPuesto getNuevoEstadoOperativo() { return nuevoEstadoOperativo; }
    public void setNuevoEstadoOperativo(EstadoPuesto nuevoEstadoOperativo) { this.nuevoEstadoOperativo = nuevoEstadoOperativo; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public static class PuestoEstadoRequestBuilder {
        private EstadoFisicoPuesto nuevoEstadoFisico;
        private EstadoPuesto nuevoEstadoOperativo;
        private String motivo;
        private String observaciones;

        public PuestoEstadoRequestBuilder nuevoEstadoFisico(EstadoFisicoPuesto nuevoEstadoFisico) { this.nuevoEstadoFisico = nuevoEstadoFisico; return this; }
        public PuestoEstadoRequestBuilder nuevoEstadoOperativo(EstadoPuesto nuevoEstadoOperativo) { this.nuevoEstadoOperativo = nuevoEstadoOperativo; return this; }
        public PuestoEstadoRequestBuilder motivo(String motivo) { this.motivo = motivo; return this; }
        public PuestoEstadoRequestBuilder observaciones(String observaciones) { this.observaciones = observaciones; return this; }

        public PuestoEstadoRequest build() {
            return new PuestoEstadoRequest(nuevoEstadoFisico, nuevoEstadoOperativo, motivo, observaciones);
        }
    }
}
