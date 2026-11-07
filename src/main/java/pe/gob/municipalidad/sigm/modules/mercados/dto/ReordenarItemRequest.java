package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class ReordenarItemRequest {

    @NotNull(message = "El ID del elemento a reordenar es obligatorio")
    private UUID id;

    @NotNull(message = "El nuevo orden visual es obligatorio")
    @Min(value = 0, message = "El orden visual no puede ser negativo")
    private Integer nuevoOrden;

    public ReordenarItemRequest() {
    }

    public ReordenarItemRequest(UUID id, Integer nuevoOrden) {
        this.id = id;
        this.nuevoOrden = nuevoOrden;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Integer getNuevoOrden() { return nuevoOrden; }
    public void setNuevoOrden(Integer nuevoOrden) { this.nuevoOrden = nuevoOrden; }
}
