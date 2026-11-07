package pe.gob.municipalidad.sigm.modules.serenazgo.dto.supervision;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadAdministrativaSupervision;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoRegistroSupervision;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoSeguimientoSupervision;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearSeguimientoRequest {

    @NotNull(message = "El tipo de registro de origen es obligatorio")
    private TipoRegistroSupervision tipoRegistroOrigen;

    @NotNull(message = "El identificador del registro de origen es obligatorio")
    private UUID registroOrigenId;

    private String registroOrigenCodigo;

    private Long oficinaResponsableId;

    private Long responsableAsignadoId;

    @NotNull(message = "El tipo de seguimiento es obligatorio")
    private TipoSeguimientoSupervision tipoSeguimiento;

    @NotBlank(message = "La descripción o motivo del seguimiento es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    private String descripcion;

    @Builder.Default
    private PrioridadAdministrativaSupervision prioridadAdministrativa = PrioridadAdministrativaSupervision.MEDIA;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaCompromiso;
}
