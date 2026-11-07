package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConvenioFraccionamiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvenioFraccionamientoResponse {
    private UUID id;
    private String codigoConvenio;
    private UUID solicitudId;
    private String codigoSolicitud;
    private UUID contribuyenteId;
    private String contribuyenteNombre;
    private String contribuyenteDocumento;
    private String contribuyenteCodigo;
    private String contribuyenteDireccion;
    private UUID programaId;
    private String programaNombre;
    private String programaNorma;
    private LocalDate fechaAprobacion;
    private LocalDate fechaInicioVigencia;
    private String resolucionAprobacion;
    private EstadoConvenioFraccionamiento estado;
    private String estadoDescripcion;
    private String estadoBadgeClass;
    private String responsableAprobacionNombre;
    private UUID documentoConvenioId;
    private String codigoVerificacion;
    private String observaciones;

    // Métricas del cronograma financiado
    private BigDecimal importeBase;
    private BigDecimal importeCuotaInicial;
    private BigDecimal importeFinanciado;
    private BigDecimal importeAccesorios;
    private BigDecimal importeTotal;
    private Integer numeroCuotas;
    private BigDecimal totalPagado;
    private BigDecimal saldoPendiente;
    private long cuotasPagadas;
    private long cuotasPendientes;
    private long cuotasVencidas;

    private CronogramaDto cronograma;
    private List<DetalleObligacionFraccionadaDto> obligaciones;
    private List<IncidenciaConvenioDto> incidencias;
}
