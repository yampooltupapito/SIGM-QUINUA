package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConciliacionCaja;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConciliacionCajaResponse {
    private UUID id;
    private String codigoConciliacion;
    private UUID sesionCajaId;
    private String sesionCajaCodigo;
    private String cajaNombre;
    private String cajeroNombre;
    private OffsetDateTime fechaConciliacion;
    private BigDecimal importeEsperado;
    private BigDecimal importeVerificado;
    private BigDecimal diferencia;
    private EstadoConciliacionCaja estado;
    private String estadoDescripcion;
    private String estadoBadgeClass;
    private String responsableNombre;
    private String observaciones;
    private String desgloseDenominacionesJson;
}
