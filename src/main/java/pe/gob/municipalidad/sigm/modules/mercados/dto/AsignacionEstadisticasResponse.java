package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionEstadisticasResponse {

    private long totalAsignaciones;
    private long borradores;
    private long solicitadas;
    private long enEvaluacion;
    private long observadas;
    private long aprobadas;
    private long rechazadas;
    private long vigentes;
    private long suspendidas;
    private long finalizadas;
    private long anuladas;

    private long puestosTotales;
    private long puestosDisponibles;
    private long puestosOcupados;
    private long puestosMantenimiento;

    private Map<String, Long> asignacionesPorMercado;
}
