package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

public class PortalComercianteIncidenciaRequest {

    @NotNull(message = "El mercado es obligatorio")
    private UUID mercadoId;

    private UUID puestoId;

    @NotNull(message = "El tipo de incidencia es obligatorio")
    private UUID tipoIncidenciaId;

    @NotBlank(message = "La descripción de la incidencia es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    private String descripcion;

    public PortalComercianteIncidenciaRequest() {}

    public PortalComercianteIncidenciaRequest(UUID mercadoId, UUID puestoId, UUID tipoIncidenciaId, String descripcion) {
        this.mercadoId = mercadoId;
        this.puestoId = puestoId;
        this.tipoIncidenciaId = tipoIncidenciaId;
        this.descripcion = descripcion;
    }

    public UUID getMercadoId() { return mercadoId; }
    public void setMercadoId(UUID mercadoId) { this.mercadoId = mercadoId; }

    public UUID getPuestoId() { return puestoId; }
    public void setPuestoId(UUID puestoId) { this.puestoId = puestoId; }

    public UUID getTipoIncidenciaId() { return tipoIncidenciaId; }
    public void setTipoIncidenciaId(UUID tipoIncidenciaId) { this.tipoIncidenciaId = tipoIncidenciaId; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID mercadoId;
        private UUID puestoId;
        private UUID tipoIncidenciaId;
        private String descripcion;

        public Builder mercadoId(UUID mercadoId) { this.mercadoId = mercadoId; return this; }
        public Builder puestoId(UUID puestoId) { this.puestoId = puestoId; return this; }
        public Builder tipoIncidenciaId(UUID tipoIncidenciaId) { this.tipoIncidenciaId = tipoIncidenciaId; return this; }
        public Builder descripcion(String descripcion) { this.descripcion = descripcion; return this; }

        public PortalComercianteIncidenciaRequest build() {
            return new PortalComercianteIncidenciaRequest(mercadoId, puestoId, tipoIncidenciaId, descripcion);
        }
    }
}
