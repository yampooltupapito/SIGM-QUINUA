package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoObligacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalComercianteObligacionResponse {
    private UUID obligacionId;
    private String codigoObligacion;
    private String conceptoNombre;
    private String puestoCodigo;
    private LocalDate periodoInicio;
    private LocalDate periodoFin;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private BigDecimal montoOriginal;
    private BigDecimal saldoPendiente;
    private EstadoObligacion estado;
    private String estadoLabel;
    private boolean vencida;
}
