package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConservacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MaterialPredominante;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConstruccionResponse {
    private UUID id;
    private String codigoConstruccion;
    private String seccionBloque;
    private Integer nivelPiso;
    private BigDecimal areaConstruida;
    private MaterialPredominante materialPredominante;
    private String materialPredominanteDesc;
    private EstadoConservacion estadoConservacion;
    private String estadoConservacionDesc;
    private String estadoConservacionBadgeClass;
    private Integer anioConstruccion;
    private String clasificacionEdificacion;
    private String descripcion;

    public String getCodigoInterno() {
        return codigoConstruccion;
    }

    public Integer getNivel() {
        return nivelPiso;
    }
}
