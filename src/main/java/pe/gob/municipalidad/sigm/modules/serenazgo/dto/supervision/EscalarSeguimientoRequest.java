package pe.gob.municipalidad.sigm.modules.serenazgo.dto.supervision;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscalarSeguimientoRequest {

    @NotBlank(message = "La oficina o entidad destinataria es obligatoria")
    @Size(max = 150, message = "El nombre de la oficina no puede exceder 150 caracteres")
    private String oficinaDestinataria;

    @NotBlank(message = "El motivo del escalamiento es obligatorio")
    @Size(max = 150, message = "El motivo no puede exceder 150 caracteres")
    private String motivo;

    @Size(max = 2000, message = "Las observaciones no pueden exceder 2000 caracteres")
    private String observaciones;
}
