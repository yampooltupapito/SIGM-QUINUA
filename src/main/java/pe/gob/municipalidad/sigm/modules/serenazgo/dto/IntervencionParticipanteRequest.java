package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntervencionParticipanteRequest {

    @NotNull(message = "Debe seleccionar al efectivo de Serenazgo")
    private UUID personalId;

    @NotBlank(message = "Debe consignar la función desempeñada en la intervención")
    private String funcionDesempenada;

    private String observaciones;
}
