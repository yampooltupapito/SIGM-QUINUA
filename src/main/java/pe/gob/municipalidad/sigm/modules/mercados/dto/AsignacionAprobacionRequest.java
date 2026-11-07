package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionAprobacionRequest {

    @NotBlank(message = "El sustento o acto resolutivo de aprobación es obligatorio")
    @Size(max = 2000, message = "El sustento no puede exceder 2000 caracteres")
    private String sustentoAprobacion;

    private Long documentoResolucionId;

    private LocalDate fechaInicioVigencia;

    private LocalDate fechaFinVigencia;

    @Builder.Default
    private boolean activarInmediatamente = false;
}
