package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DelimitacionGeoJsonRequest {

    @NotBlank(message = "La geometría en formato GeoJSON es obligatoria")
    private String geometriaGeojson;

    @NotBlank(message = "El motivo de la delimitación o actualización es obligatorio")
    private String motivo;

    private String documentoReferencia;
}
