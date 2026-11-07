package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoReglaTributaria;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReglaResponse {
    private UUID id;
    private UUID configuracionId;
    private String codigo;
    private String nombre;
    private TipoReglaTributaria tipoRegla;
    private String tipoReglaDesc;
    private String condicionAplicacion;
    private String baseLegalArticulo;
    private BigDecimal porcentajeBeneficio;
    private BigDecimal montoFijoBeneficio;
    private Integer ordenAplicacion;
    private Boolean activo;
}
