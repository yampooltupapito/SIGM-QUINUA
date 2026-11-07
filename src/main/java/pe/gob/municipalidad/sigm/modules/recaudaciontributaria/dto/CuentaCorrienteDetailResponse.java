package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCuentaCorriente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaCorrienteDetailResponse {
    private UUID id;
    private String codigoCuenta;
    private UUID contribuyenteId;
    private String contribuyenteCodigo;
    private String contribuyenteNombre;
    private String contribuyenteDocumento;
    private String domicilioFiscal;
    private String email;
    private String telefono;
    private String moneda;
    private BigDecimal saldoActual;
    private BigDecimal totalCargos;
    private BigDecimal totalAbonos;
    private EstadoCuentaCorriente estado;
    private LocalDate fechaApertura;
    private String observaciones;

    @Builder.Default
    private List<MovimientoResponse> movimientos = new ArrayList<>();

    @Builder.Default
    private List<ObligacionSummaryDto> obligacionesPendientes = new ArrayList<>();

    public String getBadgeClass() {
        return estado != null ? estado.getBadgeClass() : "";
    }
}
