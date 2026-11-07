package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoMovimientoCuenta;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.NaturalezaMovimiento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoMovimientoCuentaCorriente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoResponse {
    private UUID id;
    private Long numeroSecuencia;
    private TipoMovimientoCuentaCorriente tipoMovimiento;
    private NaturalezaMovimiento naturaleza;
    private BigDecimal importe;
    private BigDecimal saldoAnterior;
    private BigDecimal saldoPosterior;
    private LocalDate fechaOperacion;
    private OffsetDateTime fechaRegistro;
    private String referenciaOrigen;
    private String documentoSustento;
    private String descripcion;
    private EstadoMovimientoCuenta estado;
    private String usuarioResponsable;
    private UUID obligacionId;
    private String obligacionCodigo;

    public String getNaturalezaStyle() {
        return naturaleza != null ? naturaleza.getStyleClass() : "";
    }

    public String getBadgeClass() {
        return estado != null ? estado.getBadgeClass() : "";
    }
}
