package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulacionInteresRequest {

    private UUID obligacionId;
    private BigDecimal montoCapital;
    private LocalDate fechaVencimiento;
    private LocalDate fechaCorte;
    private UUID parametroId;
    private UUID conceptoId;
}
