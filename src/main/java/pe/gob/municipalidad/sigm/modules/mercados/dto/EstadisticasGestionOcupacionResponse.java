package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasGestionOcupacionResponse {

    private long totalSolicitudes;
    private long renovacionesPresentadas;
    private long renovacionesAprobadas;
    private long renovacionesPendientes;

    private long cambiosTitularPresentados;
    private long cambiosTitularFormalizados;

    private long transferenciasEnEvaluacion;
    private long transferenciasFormalizadas;

    private long solicitudesObservadas;
    private long solicitudesRechazadas;

    private Map<String, Long> porTipoSolicitud;
    private Map<String, Long> porEstado;
}
