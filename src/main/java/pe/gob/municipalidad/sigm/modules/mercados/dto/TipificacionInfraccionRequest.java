package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.GravedadHallazgo;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipificacionInfraccionRequest {

    @NotBlank(message = "El código de tipificación es obligatorio")
    private String codigo;

    @NotBlank(message = "La descripción de la infracción es obligatoria")
    private String descripcion;

    @NotBlank(message = "La norma de referencia es obligatoria")
    private String normaReferencia;

    private String articulo;

    @NotNull(message = "La gravedad base es obligatoria")
    private GravedadHallazgo gravedadBase;

    @NotBlank(message = "La autoridad competente es obligatoria")
    private String autoridadCompetente;

    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate fechaInicioVigencia;

    private LocalDate fechaFinVigencia;
    private Integer versionNormativa;
    private String estado;
    private UUID documentoFuenteId;
}
