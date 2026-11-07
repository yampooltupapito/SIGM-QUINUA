package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEntidadTerritorial;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialDelimitacionResponse {
    private UUID id;
    private TipoEntidadTerritorial tipoEntidad;
    private UUID entidadId;
    private Integer numeroVersion;
    private String geometriaGeojson;
    private String motivoCambio;
    private String documentoReferencia;
    private String usuarioRegistro;
    private OffsetDateTime createdAt;
}
