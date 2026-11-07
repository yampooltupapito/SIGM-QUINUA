package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoVinculacionContribuyente;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitarVinculacionRequest {

    @NotBlank(message = "El tipo de documento es obligatorio (DNI, RUC, CE).")
    private String tipoDocumento;

    @NotBlank(message = "El número de documento de identidad es obligatorio.")
    private String numeroDocumento;

    private String codigoContribuyente;

    @NotNull(message = "El tipo de relación o vinculación es obligatorio.")
    private TipoVinculacionContribuyente tipoVinculacion;

    @NotBlank(message = "Debe fundamentar el motivo y sustento de la solicitud de vinculación.")
    private String sustento;

    private LocalDate fechaVigenciaHasta;

    private Long documentoSustentoId;
}

