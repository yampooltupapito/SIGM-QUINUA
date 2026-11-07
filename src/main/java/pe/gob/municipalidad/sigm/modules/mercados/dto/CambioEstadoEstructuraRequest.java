package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;

public class CambioEstadoEstructuraRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoMercado nuevoEstado;

    @NotBlank(message = "El motivo del cambio de estado es obligatorio")
    @Size(max = 500, message = "El motivo no debe exceder 500 caracteres")
    private String motivo;

    public CambioEstadoEstructuraRequest() {
    }

    public CambioEstadoEstructuraRequest(EstadoMercado nuevoEstado, String motivo) {
        this.nuevoEstado = nuevoEstado;
        this.motivo = motivo;
    }

    public static CambioEstadoEstructuraRequestBuilder builder() {
        return new CambioEstadoEstructuraRequestBuilder();
    }

    public EstadoMercado getNuevoEstado() { return nuevoEstado; }
    public void setNuevoEstado(EstadoMercado nuevoEstado) { this.nuevoEstado = nuevoEstado; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public static class CambioEstadoEstructuraRequestBuilder {
        private EstadoMercado nuevoEstado;
        private String motivo;

        public CambioEstadoEstructuraRequestBuilder nuevoEstado(EstadoMercado nuevoEstado) {
            this.nuevoEstado = nuevoEstado;
            return this;
        }

        public CambioEstadoEstructuraRequestBuilder motivo(String motivo) {
            this.motivo = motivo;
            return this;
        }

        public CambioEstadoEstructuraRequest build() {
            return new CambioEstadoEstructuraRequest(nuevoEstado, motivo);
        }
    }
}
