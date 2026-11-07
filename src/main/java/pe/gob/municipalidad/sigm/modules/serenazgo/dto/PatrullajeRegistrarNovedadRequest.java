package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoNovedadPatrullaje;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatrullajeRegistrarNovedadRequest {

    @NotNull(message = "El tipo de novedad es obligatorio")
    private TipoNovedadPatrullaje tipoNovedad;

    @NotBlank(message = "La descripción de la novedad es obligatoria")
    @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
    private String descripcion;

    private UUID incidenciaVinculadaId;

    private String observaciones;
}
