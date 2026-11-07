package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoIncidenciaConvenio;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaConvenioDto {
    private UUID id;
    private TipoIncidenciaConvenio tipoIncidencia;
    private String tipoIncidenciaDescripcion;
    private String descripcion;
    private OffsetDateTime fechaRegistro;
    private String responsableNombre;
    private UUID documentoSustentoId;
}
