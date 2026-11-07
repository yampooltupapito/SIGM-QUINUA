package pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas;

import lombok.*;

/**
 * Métricas operativas de cumplimiento de patrullajes, recorridos y puntos de control.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasPatrullajeDto {

    private long programados;
    private long iniciados;
    private long finalizados;
    private long cancelados;
    private Double tasaCumplimientoPorcentaje;

    private Double duracionPromedioHoras;
    private Double duracionMedianaHoras;

    private long puntosPlanificados;
    private long puntosVisitados;
    private long puntosPendientes;
    private Double tasaVisitaPuntosPorcentaje;

    private Double distanciaPlanificadaKm;
    private Double distanciaGpsRegistradaKm;
}
