package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.AccionHistorialArbitrio;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialArbitrioResponse {
    private UUID id;
    private String entidadTipo;
    private UUID entidadId;
    private String codigoReferencia;
    private AccionHistorialArbitrio accion;
    private String accionDescripcion;
    private String estadoAnterior;
    private String nuevoEstado;
    private String motivo;
    private String usuarioRegistro;
    private OffsetDateTime fechaRegistro;

    public String getUsuarioResponsable() {
        return usuarioRegistro != null ? usuarioRegistro : "SISTEMA";
    }

    public String getEstadoResultante() {
        return nuevoEstado != null ? nuevoEstado : "-";
    }
}
