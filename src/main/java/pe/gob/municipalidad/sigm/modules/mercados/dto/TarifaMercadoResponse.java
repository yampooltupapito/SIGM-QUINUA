package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarifaMercadoResponse {
    private UUID id;
    private UUID conceptoId;
    private String conceptoCodigo;
    private String conceptoNombre;
    private UUID mercadoId;
    private String mercadoNombre;
    private Long tipoPuestoId;
    private String tipoPuestoNombre;
    private BigDecimal monto;
    private String moneda;
    private LocalDate fechaInicioVigencia;
    private LocalDate fechaFinVigencia;
    private String estado;
    private String fuenteNormativa;
    private Integer versionTarifa;
}
