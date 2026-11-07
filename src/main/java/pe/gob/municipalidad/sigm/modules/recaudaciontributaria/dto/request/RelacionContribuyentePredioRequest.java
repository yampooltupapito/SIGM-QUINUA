package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoRelacionContribuyentePredio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelacionContribuyentePredioRequest {

    @NotNull(message = "El contribuyente es obligatorio")
    private UUID contribuyenteId;

    @NotNull(message = "El tipo de relación es obligatorio")
    private TipoRelacionContribuyentePredio tipoRelacion;

    @NotNull(message = "El porcentaje de participación es obligatorio")
    @DecimalMin(value = "0.01", message = "El porcentaje debe ser mayor a 0%")
    @DecimalMax(value = "100.00", message = "El porcentaje no puede superar el 100%")
    private BigDecimal porcentajeParticipacion;

    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate fechaInicio;

    private LocalDate fechaFin;
    private Boolean esPrincipal;
    private String observaciones;

    public void setPorcentajePropiedad(BigDecimal p) {
        this.porcentajeParticipacion = p;
    }

    public BigDecimal getPorcentajePropiedad() {
        return this.porcentajeParticipacion;
    }

    public void setDocumentoSustento(String s) {
        this.observaciones = s;
    }

    public String getDocumentoSustento() {
        return this.observaciones;
    }

    public static class RelacionContribuyentePredioRequestBuilder {
        public RelacionContribuyentePredioRequestBuilder porcentajePropiedad(BigDecimal p) {
            this.porcentajeParticipacion = p;
            return this;
        }

        public RelacionContribuyentePredioRequestBuilder documentoSustento(String s) {
            this.observaciones = s;
            return this;
        }
    }
}
