package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalComercianteRenovacionRequest {

    @NotNull(message = "Debe seleccionar la asignación a renovar")
    private UUID asignacionId;

    private Long procedimientoId;

    @Positive(message = "El plazo solicitado en meses debe ser un valor positivo")
    private Integer plazoMesesSolicitado;

    private String justificacion;
}
