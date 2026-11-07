package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignarUnidadRequest {

    @NotNull(message = "Debe seleccionar el personal de serenazgo responsable")
    private UUID personalId;

    private Long oficinaId;

    private OffsetDateTime fechaInicio;

    private OffsetDateTime fechaFinProgramada;

    private BigDecimal kilometrajeSalida;

    @NotBlank(message = "Debe especificar el motivo o servicio de asignación")
    private String motivoServicio;

    private String estadoInicialUnidad;

    private String observacionesEntrega;
}
