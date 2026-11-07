package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConfiguracionArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MetodoDistribucionArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoServicioArbitrio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionArbitrioDetailResponse {
    private UUID id;
    private String codigoConfiguracion;
    private UUID servicioId;
    private String servicioCodigo;
    private String servicioNombre;
    private TipoServicioArbitrio tipoServicio;
    private UUID periodoId;
    private Integer periodoAnio;
    private UUID normaId;
    private String normaNumero;
    private String normaDescripcion;
    private UUID sectorId;
    private String sectorNombre;
    private String zona;
    private Integer versionConfiguracion;
    private UUID configuracionPredecesoraId;
    private LocalDate fechaInicioVigencia;
    private LocalDate fechaFinVigencia;
    private EstadoConfiguracionArbitrio estado;
    private String badgeClass;
    private BigDecimal costoTotalAnual;
    private BigDecimal costoTotalMensual;
    private MetodoDistribucionArbitrio metodologiaDistribucion;
    private String metodologiaDescripcion;
    private Integer cantidadPrediosAfectos;
    private String descripcionJustificacion;
    private String usuarioAprobador;
    private OffsetDateTime fechaAprobacion;
    private String resolucionAprobacion;
    private String usuarioActivador;
    private OffsetDateTime fechaActivacion;
    private String motivoDesactivacion;

    @Builder.Default
    private List<CostoArbitrioResponse> costos = new ArrayList<>();

    @Builder.Default
    private List<CriterioArbitrioResponse> criterios = new ArrayList<>();

    @Builder.Default
    private List<HistorialArbitrioResponse> historial = new ArrayList<>();
}
