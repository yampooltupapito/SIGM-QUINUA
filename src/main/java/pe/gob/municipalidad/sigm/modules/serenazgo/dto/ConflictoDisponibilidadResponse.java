package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConflictoDisponibilidadResponse {

    private String tipoConflicto; // 'PERSONAL', 'UNIDAD', 'EQUIPAMIENTO', 'TURNO', 'SECTOR'
    private UUID recursoId;
    private String recursoIdentificador;
    private String recursoNombre;
    private UUID servicioEnConflictoId;
    private String servicioEnConflictoCodigo;
    private OffsetDateTime conflictoInicio;
    private OffsetDateTime conflictoFin;
    private String mensaje;
}
