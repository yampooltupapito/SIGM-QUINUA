package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPlano;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanoMercadoResponse {
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
    private UUID versionActualId;
    private Integer versionActualNumero;
    private String versionActualEstado;
    private Integer totalVersiones;
    private Long totalElementos;
    private LocalDateTime updatedAt;
}
