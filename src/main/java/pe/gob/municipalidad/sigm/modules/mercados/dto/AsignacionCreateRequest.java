package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoAsignacion;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionCreateRequest {

    @NotNull(message = "El puesto comercial es obligatorio")
    private UUID puestoId;

    @NotNull(message = "El comerciante es obligatorio")
    private UUID comercianteId;

    @NotNull(message = "La fecha solicitada de inicio es obligatoria")
    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private TipoAsignacion tipoAsignacion;

    private Long expedienteId;

    private Long documentoSustentoId;

    @Size(max = 1000, message = "El motivo no puede exceder 1000 caracteres")
    private String motivo;

    @Size(max = 2000, message = "Las observaciones no pueden exceder 2000 caracteres")
    private String observaciones;

    @Builder.Default
    private boolean presentarInmediatamente = false;
}
