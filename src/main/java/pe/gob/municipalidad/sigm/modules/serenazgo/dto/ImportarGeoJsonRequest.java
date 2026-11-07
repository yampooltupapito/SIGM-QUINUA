package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEntidadTerritorial;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportarGeoJsonRequest {

    @NotNull(message = "El tipo de entidad (SECTOR o ZONA) es obligatorio")
    private TipoEntidadTerritorial tipoEntidad;

    @NotNull(message = "El identificador de la entidad es obligatorio")
    private UUID entidadId;

    @NotBlank(message = "El contenido GeoJSON es obligatorio")
    private String contenidoGeojson;

    @NotBlank(message = "El motivo de la importación es obligatorio")
    private String motivo;

    private String documentoReferencia;
}
