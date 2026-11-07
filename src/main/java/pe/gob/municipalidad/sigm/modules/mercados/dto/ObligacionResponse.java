package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoObligacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObligacionResponse {
    private UUID id;
    private String codigoObligacion;
    private UUID comercianteId;
    private String comercianteNombre;
    private String comercianteDocumento;
    private UUID puestoId;
    private String puestoNumero;
    private String mercadoNombre;
    private UUID asignacionId;
    private UUID conceptoId;
    private String conceptoCodigo;
    private String conceptoNombre;
    private LocalDate periodoInicio;
    private LocalDate periodoFin;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private BigDecimal montoOriginal;
    private BigDecimal montoPagado;
    private BigDecimal saldoPendiente;
    private String moneda;
    private EstadoObligacion estado;
    private String estadoDescripcion;
    private String observaciones;
}
