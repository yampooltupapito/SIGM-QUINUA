package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDatoParametro;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.UnidadMedidaParametro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametroResponse {
    private UUID id;
    private UUID configuracionId;
    private String codigo;
    private String nombre;
    private String descripcion;
    private TipoDatoParametro tipoDato;
    private String tipoDatoDesc;
    private BigDecimal valorDecimal;
    private String valorTexto;
    private Boolean valorBooleano;
    private LocalDate valorFecha;
    private UnidadMedidaParametro unidad;
    private String unidadDesc;
    private String valorFormateado;
    private String origenNormativo;
    private Boolean esObligatorio;
    private Integer ordenVisual;
    private String estado;
}
