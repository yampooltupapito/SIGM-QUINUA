package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoInteresTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.UnidadTemporalInteres;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametroInteresResponse {

    private UUID id;
    private String codigo;
    private String nombre;
    private UUID conceptoId;
    private String conceptoNombre;
    private TipoInteresTributario tipoInteres;
    private BigDecimal tasa;
    private UnidadTemporalInteres unidadTemporal;
    private LocalDate fechaInicioVigencia;
    private LocalDate fechaFinVigencia;
    private UUID normaId;
    private String normaNombre;
    private String baseLegal;
    private Boolean estado;
    private boolean esVigente;
}
