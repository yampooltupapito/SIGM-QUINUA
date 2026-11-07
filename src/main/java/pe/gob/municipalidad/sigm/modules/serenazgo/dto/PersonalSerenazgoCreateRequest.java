package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoPersonalSerenazgo;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalSerenazgoCreateRequest {

    @NotNull(message = "Debe vincular una persona válida del núcleo institucional")
    private Long personaId;

    private Long usuarioId;

    private Long oficinaId;

    @NotNull(message = "Debe asignar un cargo operativo")
    private UUID cargoOperativoId;

    private String tipoDocumentoLaboral;

    private LocalDate fechaIngreso;

    @Builder.Default
    private EstadoPersonalSerenazgo estado = EstadoPersonalSerenazgo.ACTIVO;

    private String observaciones;
}
