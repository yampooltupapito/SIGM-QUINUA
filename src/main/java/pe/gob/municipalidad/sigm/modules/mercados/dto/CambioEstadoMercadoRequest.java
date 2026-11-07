package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;

public class CambioEstadoMercadoRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoMercado nuevoEstado;

    @NotBlank(message = "El motivo del cambio de estado es obligatorio")
    @Size(max = 500, message = "El motivo no debe exceder 500 caracteres")
    private String motivo;

    public CambioEstadoMercadoRequest() {
    }

    public CambioEstadoMercadoRequest(EstadoMercado nuevoEstado, String motivo) {
        this.nuevoEstado = nuevoEstado;
        this.motivo = motivo;
    }

    public static CambioEstadoMercadoRequestBuilder builder() {
        return new CambioEstadoMercadoRequestBuilder();
    }

    public EstadoMercado getNuevoEstado() { return nuevoEstado; }
    public void setNuevoEstado(EstadoMercado nuevoEstado) { this.nuevoEstado = nuevoEstado; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public static class CambioEstadoMercadoRequestBuilder {
        private EstadoMercado nuevoEstado;
        private String motivo;

        public CambioEstadoMercadoRequestBuilder nuevoEstado(EstadoMercado nuevoEstado) {
            this.nuevoEstado = nuevoEstado;
            return this;
        }

        public CambioEstadoMercadoRequestBuilder motivo(String motivo) {
            this.motivo = motivo;
            return this;
        }

        public CambioEstadoMercadoRequest build() {
            return new CambioEstadoMercadoRequest(nuevoEstado, motivo);
        }
    }
}
