package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalendarioVencimiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarioCuotaResponse {
    private UUID id;
    private UUID conceptoId;
    private String conceptoNombre;
    private UUID periodoId;
    private Integer anioPeriodo;
    private Integer numeroCuota;
    private String descripcionCuota;
    private LocalDate fechaVencimiento;
    private Boolean permiteDescuento;
    private BigDecimal porcentajeDescuento;
    private EstadoCalendarioVencimiento estado;
    private String estadoDesc;
    private String badgeClass;
    private String resolucionProrroga;
}
