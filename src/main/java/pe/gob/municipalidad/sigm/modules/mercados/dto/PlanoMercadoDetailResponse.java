package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPlano;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanoMercadoDetailResponse {

    private UUID id;
    private UUID mercadoId;
    private String mercadoNombre;
    private String mercadoCodigo;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal anchoBase;
    private BigDecimal altoBase;
    private String unidadReferencia;
    private EstadoPlano estado;

    // Versión activa o seleccionada
    private VersionPlanoResponse versionSeleccionada;

    // Elementos gráficos de la versión seleccionada
    @Builder.Default
    private List<ElementoPlanoResponse> elementos = new ArrayList<>();

    // Lista de todas las versiones para selector
    @Builder.Default
    private List<VersionPlanoResponse> historialVersiones = new ArrayList<>();

    // Estadísticas agregadas
    private EstadisticasPlanoResponse estadisticas;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
