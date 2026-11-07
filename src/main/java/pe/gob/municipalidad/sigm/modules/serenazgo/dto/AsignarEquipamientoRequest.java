package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignarEquipamientoRequest {

    private UUID personalId;
    private UUID unidadId;

    @Builder.Default
    private Integer cantidad = 1;

    @NotBlank(message = "El motivo de la asignación es obligatorio")
    private String motivo;

    private String estadoBienEntrega;
    private String observaciones;
}
