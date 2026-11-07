package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDeterminacionTributaria;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneracionLoteRequest {

    @NotNull(message = "El tipo de determinación es obligatorio")
    private TipoDeterminacionTributaria tipoDeterminacion;

    @NotNull(message = "El ejercicio o período fiscal es obligatorio")
    private UUID periodoId;

    private String observaciones;
}
