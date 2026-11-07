package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AplicacionResponse {
    private UUID id;
    private UUID pagoId;
    private String codigoPago;
    private UUID obligacionId;
    private String codigoObligacion;
    private String conceptoNombre;
    private UUID cuotaId;
    private Integer numeroCuota;
    private BigDecimal montoAplicado;
    private OffsetDateTime fechaAplicacion;
    private String estado;
}
