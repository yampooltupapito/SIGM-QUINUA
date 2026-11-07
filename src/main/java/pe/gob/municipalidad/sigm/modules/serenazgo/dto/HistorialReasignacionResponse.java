package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialReasignacionResponse {
    private UUID id;
    private UUID zonaId;
    private String zonaCodigo;
    private String zonaNombre;
    private UUID sectorAnteriorId;
    private String sectorAnteriorNombre;
    private UUID sectorNuevoId;
    private String sectorNuevoNombre;
    private String motivo;
    private String documentoReferencia;
    private String usuarioRegistro;
    private OffsetDateTime createdAt;
}
