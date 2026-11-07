package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoRepresentacionLegal;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepresentanteContribuyenteRequest {

    @NotBlank(message = "El tipo de documento es obligatorio")
    private String tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    private String numeroDocumento;

    @NotBlank(message = "Los nombres y apellidos del representante son obligatorios")
    private String nombresApellidos;

    @NotNull(message = "El tipo de representación es obligatorio")
    private TipoRepresentacionLegal tipoRepresentacion;

    private String partidaRegistral;
    private String asientoRegistral;

    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate fechaInicio;

    private LocalDate fechaFin;
    private String observaciones;
}
