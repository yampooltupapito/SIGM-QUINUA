package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.PeriodicidadConcepto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoConceptoCobro;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConceptoCobroRequest {

    @NotBlank(message = "El código es obligatorio.")
    @Size(max = 50, message = "El código no debe exceder 50 caracteres.")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 150, message = "El nombre no debe exceder 150 caracteres.")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El tipo de concepto es obligatorio.")
    private TipoConceptoCobro tipoConcepto;

    @NotNull(message = "La periodicidad es obligatoria.")
    @Builder.Default
    private PeriodicidadConcepto periodicidad = PeriodicidadConcepto.MENSUAL;

    @Builder.Default
    private String estado = "ACTIVO";

    @Builder.Default
    private boolean requiereAsignacion = true;

    private Long oficinaResponsableId;
}
