package pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Filtro global multidimensional para consultas estadísticas,
 * series temporales y reportes de Seguridad Ciudadana.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiltroEstadisticoRequest {

    @Builder.Default
    private String periodoTipo = "MES_ACTUAL";

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFin;

    private UUID sectorId;
    private UUID zonaId;
    private Long oficinaId;

    private UUID tipoIncidenciaId;
    private String estadoIncidencia;
    private String prioridadIncidencia;

    private String modalidadPatrullaje;

    @Builder.Default
    private Boolean compararPeriodoAnterior = true;

    @Builder.Default
    private String agrupacion = "MES";
}
