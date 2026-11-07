package pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas;

import lombok.*;

/**
 * Ficha de cálculo en tiempo de consulta de un indicador KPI
 * con soporte para comparación de períodos y variación porcentual protegida.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndicadorKpiDto {

    private String codigo;
    private String nombre;
    private String modulo;

    private Long valorActual;
    private Long valorAnterior;

    private Long variacionAbsoluta;
    private Double variacionPorcentual;
    private boolean variacionIndefinida;

    private String unidad;
    private String definicion;
    private String formula;
    private String limitaciones;
    private String fuente;

    private String iconoLucide;
    private String tendencia; // POSITIVA, NEGATIVA, NEUTRA, INDEFINIDA
}
