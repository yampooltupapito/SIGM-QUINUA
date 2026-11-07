package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPredio;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstadoPredioResponse {
    private UUID id;
    private EstadoPredio estadoAnterior;
    private String estadoAnteriorDesc;
    private EstadoPredio nuevoEstado;
    private String nuevoEstadoDesc;
    private String nuevoEstadoBadgeClass;
    private String motivo;
    private String resolucionSustento;
    private String usuarioRegistro;
    private OffsetDateTime fechaRegistro;

    public OffsetDateTime getFechaCambio() {
        return fechaRegistro;
    }

    public String getUsuarioResponsable() {
        return usuarioRegistro;
    }

    public EstadoPredio getEstadoNuevo() {
        return nuevoEstado;
    }

    public String getEstadoNuevoDesc() {
        return nuevoEstadoDesc;
    }
}
