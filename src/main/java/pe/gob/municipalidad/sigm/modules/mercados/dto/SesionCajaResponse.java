package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoSesionCaja;

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
    private UUID cajaId;
    private String cajaCodigo;
    private String cajaNombre;
    private Long usuarioCajeroId;
    private String cajeroUsername;
    private OffsetDateTime fechaApertura;
    private OffsetDateTime fechaCierre;
    private BigDecimal montoInicial;
    private BigDecimal totalIngresos;
    private BigDecimal totalReversos;
    private BigDecimal montoFinalEsperado;
    private BigDecimal montoFinalDeclarado;
    private BigDecimal diferencia;
    private EstadoSesionCaja estado;
    private String estadoDescripcion;
    private String observaciones;
}
