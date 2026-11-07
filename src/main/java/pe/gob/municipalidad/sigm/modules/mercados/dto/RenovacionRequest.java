package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoSolicitudGestion;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenovacionRequest {

    @NotNull(message = "La asignación de origen es obligatoria")
    private UUID asignacionId;

    private UUID contratoId;

    private UUID autorizacionId;

    @NotNull(message = "El tipo de renovación es obligatorio")
    private TipoSolicitudGestion tipoSolicitud;

    private Long procedimientoId;

    private LocalDate fechaInicioSolicitada;

    private LocalDate fechaFinSolicitada;

    @NotBlank(message = "El motivo de la solicitud de renovación es obligatorio")
    private String motivo;

    private Long expedienteId;

    private Long oficinaResponsableId;
}
