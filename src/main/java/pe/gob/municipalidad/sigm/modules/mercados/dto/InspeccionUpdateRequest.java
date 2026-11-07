package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoInspeccionMercado;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspeccionUpdateRequest {

    @NotNull(message = "El tipo de inspección es obligatorio")
    private TipoInspeccionMercado tipoInspeccion;

    private UUID sectorId;

    private UUID pabellonId;

    private UUID puestoId;

    private UUID comercianteId;

    private UUID plantillaId;

    @NotNull(message = "La fecha programada es obligatoria")
    private LocalDate fechaProgramada;

    private LocalTime horaProgramada;

    @NotBlank(message = "El motivo de la inspección es obligatorio")
    @Size(max = 500, message = "El motivo no puede exceder los 500 caracteres")
    private String motivo;

    @NotBlank(message = "El objetivo de la inspección es obligatorio")
    private String objetivo;

    private Long oficinaResponsableId;

    private Long inspectorResponsableId;

    private String observacionesGenerales;
}
