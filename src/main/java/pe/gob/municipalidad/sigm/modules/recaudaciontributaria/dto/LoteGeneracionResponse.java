package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoLoteGeneracion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDeterminacionTributaria;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoteGeneracionResponse {
    private UUID id;
    private String codigoLote;
    private TipoDeterminacionTributaria tipoDeterminacion;
    private UUID periodoId;
    private Integer periodoAnio;
    private Integer totalEvaluadas;
    private Integer totalElegibles;
    private Integer totalGeneradas;
    private Integer totalDuplicadas;
    private Integer totalErrores;
    private BigDecimal importeTotalGenerado;
    private EstadoLoteGeneracion estado;
    private String usuarioRegistro;
    private String observaciones;
    private java.time.LocalDateTime createdAt;

    @Builder.Default
    private List<DetalleLoteResponse> detalles = new ArrayList<>();

    public String getBadgeClass() {
        return estado != null ? estado.getBadgeClass() : "";
    }
}
