package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCuentaCorriente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaCorrienteSummaryDto {
    private UUID id;
    private UUID contribuyenteId;
    private String contribuyenteCodigo;
    private String contribuyenteNombre;
    private String contribuyenteDocumento;
    private String codigoCuenta;
    private String moneda;
    private BigDecimal saldoActual;
    private BigDecimal totalCargos;
    private BigDecimal totalAbonos;
    private EstadoCuentaCorriente estado;
    private LocalDate fechaApertura;
    private long totalObligacionesPendientes;

    public String getBadgeClass() {
        return estado != null ? estado.getBadgeClass() : "";
    }
}
