package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionGeoJsonResult {
    private boolean valido;
    private String tipoGeometria;
    private int numeroVertices;
    private Double centroideLongitud;
    private Double centroideLatitud;
    private Double minLongitud;
    private Double minLatitud;
    private Double maxLongitud;
    private Double maxLatitud;
    private String mensajeError;
    private boolean superposicionDetectada;
    private List<String> advertencias;
}
