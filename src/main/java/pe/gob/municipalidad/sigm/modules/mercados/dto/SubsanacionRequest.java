package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubsanacionRequest {

    @NotBlank(message = "La descripción de las acciones de subsanación es obligatoria")
    private String descripcionSubsanacion;

    private UUID documentoEvidenciaId;
}
