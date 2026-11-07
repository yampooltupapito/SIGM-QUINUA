package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardProgramacionResponse {

    private long totalServicios;
    private long serviciosBorrador;
    private long serviciosProgramados;
    private long serviciosConfirmados;
    private long serviciosEnCurso;
    private long serviciosFinalizados;
    private long serviciosCancelados;
    private long totalTurnosActivos;
    private long personalActualmenteAsignado;
    private long unidadesActualmenteReservadas;
}
