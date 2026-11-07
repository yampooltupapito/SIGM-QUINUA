package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardNotificacionResponse {

    private long totalNotificaciones;
    private long totalBorrador;
    private long totalPendientesValidacion;
    private long totalAutorizadas;
    private long totalEnGestion;
    private long totalProgramadas;
    private long totalEnProceso;
    private long totalEntregadasConEvidencia;
    private long totalNoEntregadas;
    private long totalPendientesRevisionLegal;
    private long totalValidadasLegalmente;
    private long totalObservadas;
    private long totalAnuladas;

    private long totalLotes;
    private long totalLotesEnGestion;
}

