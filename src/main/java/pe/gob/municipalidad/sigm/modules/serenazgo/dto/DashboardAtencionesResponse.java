package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardAtencionesResponse {

    private long incidenciasPendientesAsignacion;
    private long atencionesAsignadas;
    private long atencionesDespachadas;
    private long atencionesEnCurso;
    private long atencionesEnEspera;
    private long atencionesFinalizadas;
    private long atencionesCanceladas;
    private long totalAtenciones;

    @Builder.Default
    private Long tiempoPromedioAsignacionMinutos = 0L;

    @Builder.Default
    private Long tiempoPromedioDespachoMinutos = 0L;

    public long getPendientesAsignacion() { return incidenciasPendientesAsignacion; }
    public long getAsignadas() { return atencionesAsignadas; }
    public long getDespachadas() { return atencionesDespachadas; }
    public long getEnAtencion() { return atencionesEnCurso; }
    public long getEnEspera() { return atencionesEnEspera; }
    public long getFinalizadas() { return atencionesFinalizadas; }
    public long getCanceladas() { return atencionesCanceladas; }
}
