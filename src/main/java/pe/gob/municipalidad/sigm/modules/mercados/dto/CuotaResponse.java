package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoCuota;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuotaResponse {
    private UUID id;
    private Integer numeroCuota;
    private LocalDate fechaVencimiento;
    private BigDecimal montoProgramado;
    private BigDecimal montoPagado;
    private BigDecimal saldo;
    private EstadoCuota estado;
    private String estadoDescripcion;
}
