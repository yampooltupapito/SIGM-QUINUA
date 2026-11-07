package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.FormatoExportacionReporte;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MedioPagoTributario;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Filtros de consulta y generación para el Centro de Reportes Tributarios.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteFilterRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFin;

    private Integer ejercicio;

    private Integer periodo; // 0 = Todo el ejercicio, 1..12 = Mes específico

    private UUID conceptoId;

    private Long oficinaId;

    private String estado;

    private MedioPagoTributario medioPago;

    private UUID cajaId;

    private FormatoExportacionReporte formato;

    private String q; // Búsqueda textual
}

