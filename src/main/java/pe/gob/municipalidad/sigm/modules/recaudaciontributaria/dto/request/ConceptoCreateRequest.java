package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.NaturalezaTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.PeriodicidadConcepto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoConceptoTributario;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConceptoCreateRequest {

    @NotBlank(message = "El código del concepto es obligatorio")
    @Size(max = 50, message = "El código no debe superar 50 caracteres")
    private String codigo;

    @NotBlank(message = "La denominación del concepto es obligatoria")
    @Size(max = 150, message = "La denominación no debe superar 150 caracteres")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El tipo de concepto es obligatorio")
    private TipoConceptoTributario tipoConcepto;

    @NotNull(message = "La naturaleza tributaria es obligatoria")
    @Builder.Default
    private NaturalezaTributaria naturalezaTributaria = NaturalezaTributaria.IMPUESTO;

    @NotNull(message = "La periodicidad es obligatoria")
    @Builder.Default
    private PeriodicidadConcepto periodicidad = PeriodicidadConcepto.ANUAL;

    @Builder.Default
    private Boolean aplicaAPredio = true;

    private String cuentaPresupuestal;

    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate fechaInicioVigencia;

    private LocalDate fechaFinVigencia;

    private UUID normaLegalId;

    private Long oficinaResponsableId;
}
