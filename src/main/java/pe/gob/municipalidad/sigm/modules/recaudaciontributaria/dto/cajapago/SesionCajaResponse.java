package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoSesionCaja;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionCajaResponse {
    private UUID id;
    private String codigoSesion;
    private UUID cajaId;
    private String cajaCodigo;
    private String cajaNombre;
    private Long cajeroId;
    private String cajeroNombre;
    private OffsetDateTime fechaApertura;
    private OffsetDateTime fechaCierre;
    private BigDecimal montoApertura;
    private BigDecimal montoCierreDeclarado;
    private BigDecimal montoCierreCalculado;
    private BigDecimal diferencia;
    private BigDecimal totalRecaudadoEfectivo;
    private BigDecimal totalRecaudadoOtrosMedios;
    private BigDecimal totalRecaudadoGeneral;
    private Integer totalOperaciones;
    private EstadoSesionCaja estado;
    private String estadoDescripcion;
    private String estadoBadgeClass;
    private String observaciones;
    private String justificacionDiferencia;
    private String supervisorNombre;
}
