package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasIncidenciasResponse {
    private long totalIncidencias;
    private long abiertas;
    private long enAtencion;
    private long resueltas;
    private long cerradas;
    private long criticasAbiertas;
    private long sinResponsable;

    private long totalPresuntasInfracciones;
    private long infraccionesEnEvaluacion;
    private long infraccionesDerivadas;
    private long infraccionesDescartadas;

    private Map<String, Long> incidenciasPorPrioridad;
    private Map<String, Long> incidenciasPorEstado;
    private Map<String, Long> incidenciasPorTipo;
    private Map<String, Long> infraccionesPorEstado;
}
