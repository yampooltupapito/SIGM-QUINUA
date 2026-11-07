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
public class HistorialEstadoTerritorioResponse {
    private UUID id;
    private TipoEntidadTerritorial tipoEntidad;
    private UUID entidadId;
    private String estadoAnterior;
    private String estadoNuevo;
    private String motivo;
    private String usuarioRegistro;
    private OffsetDateTime createdAt;
}
