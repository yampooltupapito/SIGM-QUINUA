package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConservacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MaterialPredominante;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConstruccionPredialRequest {

    private String codigoConstruccion;
    private String seccionBloque;

    @NotNull(message = "El nivel o piso es obligatorio")
    private Integer nivelPiso;

    @NotNull(message = "El área construida es obligatoria")
    @DecimalMin(value = "0.01", message = "El área construida debe ser mayor a cero")
    private BigDecimal areaConstruida;

    @NotNull(message = "El material predominante es obligatorio")
    private MaterialPredominante materialPredominante;

    @NotNull(message = "El estado de conservación es obligatorio")
    private EstadoConservacion estadoConservacion;

    private Integer anioConstruccion;
    private String clasificacionEdificacion;
    private String descripcion;

    public void setCodigoInterno(String cod) {
        this.codigoConstruccion = cod;
    }

    public String getCodigoInterno() {
        return this.codigoConstruccion;
    }

    public void setNivel(Integer n) {
        this.nivelPiso = n;
    }

    public Integer getNivel() {
        return this.nivelPiso;
    }

    public static class ConstruccionPredialRequestBuilder {
        public ConstruccionPredialRequestBuilder codigoInterno(String cod) {
            this.codigoConstruccion = cod;
            return this;
        }
        public ConstruccionPredialRequestBuilder nivel(Integer n) {
            this.nivelPiso = n;
            return this;
        }
    }
}
