package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEventoAtencion;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionNovedadRequest {

    @NotNull(message = "El tipo de novedad o evento es obligatorio")
    private TipoEventoAtencion tipoEvento;

    @NotBlank(message = "La descripción de la novedad es obligatoria")
    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String descripcion;

    private UUID personalId;

    @Builder.Default
    private Boolean visibleCiudadano = false;
}
