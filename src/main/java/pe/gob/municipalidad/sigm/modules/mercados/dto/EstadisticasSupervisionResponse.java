package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasSupervisionResponse {

    private long totalInspecciones;
    private long programadas;
    private long enProceso;
    private long finalizadas;
    private long canceladas;
    private long inspeccionesHoy;

    private long totalHallazgos;
    private long hallazgosAbiertos;

    private long totalMedidas;
    private long medidasPendientes;
    private long medidasSubsanadas;
    private long medidasVencidas;
    private long medidasPorVencer;

    private Map<String, Long> inspeccionesPorTipo;
    private Map<String, Long> inspeccionesPorEstado;
    private Map<String, Long> medidasPorEstado;
}
