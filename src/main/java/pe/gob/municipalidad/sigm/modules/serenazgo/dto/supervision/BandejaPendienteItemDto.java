package pe.gob.municipalidad.sigm.modules.serenazgo.dto.supervision;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoRegistroSupervision;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BandejaPendienteItemDto {

    private UUID registroId;
    private TipoRegistroSupervision tipoRegistro;
    private String codigo;
    private String titulo;
    private OffsetDateTime fecha;
    private String prioridad;
    private String prioridadBadge;
    private String estadoActual;
    private String estadoBadge;
    private String sector;
    private String motivoPendiente;
    private String responsable;
    private String urlDetalle;
    private String urlAccionSupervision;
}
