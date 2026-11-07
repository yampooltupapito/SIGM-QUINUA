package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoRepresentacion;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepresentanteRequest {

    private Long personaId;

    @Pattern(regexp = "^$|^[0-9]{8}$", message = "El DNI del representante debe tener 8 dígitos")
    private String dni;

    @Size(max = 100)
    private String nombres;

    @Size(max = 100)
    private String apellidoPaterno;

    @Size(max = 100)
    private String apellidoMaterno;

    @NotNull(message = "El tipo de representación es obligatorio")
    private TipoRepresentacion tipoRepresentacion;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private Long documentoSustentoId;

    private String estado;
}
