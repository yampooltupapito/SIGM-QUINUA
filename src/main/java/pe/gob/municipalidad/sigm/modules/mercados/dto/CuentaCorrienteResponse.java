package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaCorrienteResponse {
    private UUID comercianteId;
    private String comercianteNombre;
    private String comercianteDocumento;
    private String comercianteCodigo;
    private BigDecimal saldoTotalPendiente;
    @Builder.Default
    private List<ObligacionResponse> obligacionesExigibles = new ArrayList<>();
    @Builder.Default
    private List<MovimientoCuentaCorrienteDto> movimientos = new ArrayList<>();
}
