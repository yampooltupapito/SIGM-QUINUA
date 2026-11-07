package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCuotaTributaria;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuotaResponse {
    private UUID id;
    private Integer numeroCuota;
    private BigDecimal importe;
    private BigDecimal importePagado;
    private BigDecimal saldo;
    private LocalDate fechaVencimiento;
    private EstadoCuotaTributaria estado;

    public String getBadgeClass() {
        return estado != null ? estado.getBadgeClass() : "";
    }
}
