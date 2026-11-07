package pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas;

import lombok.*;

/**
 * Estadísticas avanzadas de tiempos de ciclo de atenciones de Serenazgo.
 * Integra promedios, medianas y percentil 90 con conteo de exclusión.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TiemposAtencionEstadisticasDto {

    private long totalAtencionesValidas;
    private long registrosIncompletosExcluidos;

    // Asignación
    private Double promedioAsignacionMinutos;
    private Double medianaAsignacionMinutos;
    private Double percentil90AsignacionMinutos;

    // Despacho
    private Double promedioDespachoMinutos;
    private Double medianaDespachoMinutos;

    // Aceptación
    private Double promedioAceptacionMinutos;
    private Double medianaAceptacionMinutos;

    // Duración total
    private Double promedioDuracionMinutos;
    private Double medianaDuracionMinutos;
    private Double percentil90DuracionMinutos;
}
