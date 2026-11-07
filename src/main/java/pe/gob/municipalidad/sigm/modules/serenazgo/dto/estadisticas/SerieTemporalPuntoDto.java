package pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas;

import lombok.*;

/**
 * Punto de datos en una serie temporal cronológica.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SerieTemporalPuntoDto {

    private String periodoClave; // Ej: 2026-10, 2026-W43, 2026-10-25
    private String etiqueta;     // Ej: Octubre 2026, Semana 43, 25 Oct
    private Long valorActual;
    private Long valorAnterior;
}
