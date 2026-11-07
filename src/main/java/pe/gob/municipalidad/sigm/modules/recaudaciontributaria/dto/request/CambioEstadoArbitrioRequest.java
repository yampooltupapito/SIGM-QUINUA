package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoArbitrio;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambioEstadoArbitrioRequest {
    private EstadoCalculoArbitrio nuevoEstado;
    private String motivo;
    private String resolucionAprobacion;

    public String getResolucion() {
        return resolucionAprobacion;
    }
}
