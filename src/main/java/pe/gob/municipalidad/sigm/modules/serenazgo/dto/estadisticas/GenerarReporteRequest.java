package pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.FormatoReporteEstadistico;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoReporteEstadistico;

/**
 * Petición para solicitar la generación de un reporte analítico.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerarReporteRequest {

    @NotNull(message = "El tipo de reporte es obligatorio")
    private TipoReporteEstadistico tipoReporte;

    @NotNull(message = "El formato de exportación es obligatorio")
    private FormatoReporteEstadistico formato;

    private String titulo;
    private String descripcion;

    @Builder.Default
    private FiltroEstadisticoRequest filtro = new FiltroEstadisticoRequest();
}
