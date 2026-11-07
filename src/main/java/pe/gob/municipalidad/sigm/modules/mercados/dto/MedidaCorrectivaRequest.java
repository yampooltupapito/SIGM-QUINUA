package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedidaCorrectivaRequest {

    @NotBlank(message = "La descripción de la medida correctiva es obligatoria")
    private String descripcion;

    @NotBlank(message = "El tipo de responsable es obligatorio")
    private String tipoResponsable; // COMERCIANTE_TITULAR, ADMINISTRACION_MERCADO, MANTENIMIENTO_MUNICIPAL

    private Long responsableId;

    private UUID comercianteResponsableId;

    @NotNull(message = "La fecha de asignación es obligatoria")
    private LocalDate fechaAsignacion;

    private LocalDate fechaLimite;

    private String observaciones;
}
