package pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Contenedor de serie temporal con puntos cronológicos agregados.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SerieTemporalDto {

    private String identificador;
    private String nombreSerie;
    private String unidad;

    @Builder.Default
    private List<SerieTemporalPuntoDto> puntos = new ArrayList<>();
}
