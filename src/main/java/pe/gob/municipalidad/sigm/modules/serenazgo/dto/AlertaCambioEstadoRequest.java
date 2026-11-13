package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAlertaCiudadana;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaCambioEstadoRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoAlertaCiudadana nuevoEstado;

    @NotBlank(message = "El motivo de la transición de estado es obligatorio")
    @Builder.Default
    private String motivo = "Actualización administrativa de estado";

    @Builder.Default
    private Boolean esVisibleCiudadano = true;

    public void setMotivo(String motivo) {
        this.motivo = (motivo == null || motivo.isBlank()) ? "Actualización administrativa de estado" : motivo;
    }
}
