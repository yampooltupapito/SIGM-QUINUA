package pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoReporteEstadistico;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.FormatoReporteEstadistico;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoReporteEstadistico;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resumen de reporte generado en el historial.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteHistorialDto {

    private UUID id;
    private String codigo;
    private TipoReporteEstadistico tipoReporte;
    private String tipoReporteTitulo;
    private String titulo;
    private FormatoReporteEstadistico formato;
    private String formatoExtension;
    private OffsetDateTime fechaGeneracion;
    private String usernameGenerador;
    private Long tamanioBytes;
    private String tamanioFormateado;
    private EstadoReporteEstadistico estado;
    private String estadoDescripcion;
    private String estadoBadgeClass;
    private String periodoEtiqueta;
}
