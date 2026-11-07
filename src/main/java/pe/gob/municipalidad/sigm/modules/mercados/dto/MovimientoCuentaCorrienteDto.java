package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoMovimientoCuentaCorriente;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoCuentaCorrienteDto {
    private Long id;
    private TipoMovimientoCuentaCorriente tipoMovimiento;
    private String tipoMovimientoDescripcion;
    private String concepto;
    private BigDecimal monto;
    private BigDecimal saldoResultante;
    private UUID referenciaId;
    private String tipoReferencia;
    private OffsetDateTime fechaMovimiento;
    private String usuario;
}
