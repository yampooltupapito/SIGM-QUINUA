package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardRecursosResponse {

    private long totalUnidades;
    private long unidadesDisponibles;
    private long unidadesAsignadas;
    private long unidadesEnMantenimiento;
    private long unidadesFueraServicio;

    private long totalEquipos;
    private long equiposDisponibles;
    private long equiposAsignados;
    private long equiposEnReparacion;
    private long equiposDeBaja;

    private long incidenciasAbiertas;
    private long mantenimientosProgramados;
}
