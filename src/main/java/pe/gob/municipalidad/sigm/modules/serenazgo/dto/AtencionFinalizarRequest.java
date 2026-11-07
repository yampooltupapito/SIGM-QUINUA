package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.ResultadoAtencionIncidencia;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionFinalizarRequest {

    @NotNull(message = "El resultado administrativo es obligatorio")
    private ResultadoAtencionIncidencia resultadoAdministrativo;

    @NotBlank(message = "El resumen o motivo de finalización es obligatorio")
    @Size(max = 2000, message = "El motivo no puede superar los 2000 caracteres")
    private String motivoFinalizacion;

    @Size(max = 2000, message = "Las observaciones no pueden superar los 2000 caracteres")
    private String observacionesOperativas;

    @Builder.Default
    private Boolean resolverIncidencia = false;

    public ResultadoAtencionIncidencia getResultado() {
        return resultadoAdministrativo;
    }

    public void setResultado(ResultadoAtencionIncidencia r) {
        this.resultadoAdministrativo = r;
    }

    public String getResumenActuaciones() {
        return motivoFinalizacion;
    }

    public void setResumenActuaciones(String r) {
        this.motivoFinalizacion = r;
    }
}
