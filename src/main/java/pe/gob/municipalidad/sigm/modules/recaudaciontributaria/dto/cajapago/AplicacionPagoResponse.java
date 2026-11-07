package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoAplicacionPago;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AplicacionPagoResponse {
    private UUID id;
    private UUID obligacionId;
    private String obligacionCodigo;
    private String conceptoNombre;
    private Integer periodoAnio;
    private Integer cuotaNumero;
    private UUID cuotaId;
    private BigDecimal importeAplicado;
    private OffsetDateTime fechaHoraAplicacion;
    private EstadoAplicacionPago estado;
    private String estadoDescripcion;
    private String estadoBadgeClass;
    private UUID movimientoCuentaId;
    private String observaciones;
}
