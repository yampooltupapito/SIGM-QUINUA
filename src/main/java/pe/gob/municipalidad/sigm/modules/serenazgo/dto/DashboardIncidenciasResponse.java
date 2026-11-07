package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardIncidenciasResponse {

    private long totalIncidencias;
    private long registradas;
    private long enEvaluacion;
    private long asignadas;
    private long enAtencion;
    private long resueltas;
    private long cerradas;
    private long descartadas;

    private long prioridadBaja;
    private long prioridadMedia;
    private long prioridadAlta;
    private long prioridadCritica;

    private long hoy;
    private long estaSemana;
    private long esteMes;

    public long getTotalRegistradas() { return registradas; }
    public long getTotalEnEvaluacion() { return enEvaluacion; }
    public long getTotalAsignadas() { return asignadas; }
    public long getTotalEnAtencion() { return enAtencion; }
    public long getTotalResueltas() { return resueltas; }
    public long getTotalCerradas() { return cerradas; }
    public long getTotalDescartadas() { return descartadas; }
    public long getTotalCriticas() { return prioridadCritica; }
    public long getTotalAltas() { return prioridadAlta; }
    public long getTotalMedias() { return prioridadMedia; }
    public long getTotalBajas() { return prioridadBaja; }
}
