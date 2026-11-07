package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoSolicitudFraccionamiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudFraccionamientoResponse {
    private UUID id;
    private String codigoSolicitud;
    private UUID contribuyenteId;
    private String contribuyenteNombre;
    private String contribuyenteDocumento;
    private String contribuyenteCodigo;
    private UUID programaId;
    private String programaNombre;
    private LocalDate fechaSolicitud;
    private EstadoSolicitudFraccionamiento estado;
    private String estadoDescripcion;
    private String estadoBadgeClass;
    private String motivo;
    private Integer numeroCuotasPropuestas;
    private BigDecimal montoTotalDeuda;
    private BigDecimal montoCuotaInicial;
    private String oficinaNombre;
    private String responsableUsername;
    private String evaluadorUsername;
    private OffsetDateTime fechaEvaluacion;
    private String observacionEvaluacion;
    private UUID documentoSustentoId;
    private List<DetalleObligacionFraccionadaDto> detalles;
    private CronogramaDto cronogramaVigente;
    private UUID convenioId;
    private String codigoConvenio;
}
