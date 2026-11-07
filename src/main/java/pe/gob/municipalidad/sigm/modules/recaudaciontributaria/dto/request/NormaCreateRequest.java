package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoNormaTributaria;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormaCreateRequest {

    @NotNull(message = "El tipo de norma es obligatorio")
    private TipoNormaTributaria tipoNorma;

    @NotBlank(message = "El número o identificador de la norma es obligatorio")
    @Size(max = 120, message = "El número de norma no debe superar 120 caracteres")
    private String numeroNorma;

    @NotBlank(message = "La entidad emisora es obligatoria")
    @Builder.Default
    private String entidadEmisora = "Municipalidad Distrital de Quinua";

    @NotBlank(message = "La descripción o denominación de la norma es obligatoria")
    private String descripcion;

    private LocalDate fechaPublicacion;

    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate fechaInicioVigencia;

    private LocalDate fechaFinVigencia;

    private String enlaceOficial;

    @Builder.Default
    private String alcanceTerritorial = "DISTRITAL_QUINUA";

    private String archivoHash;

    private UUID normaModificaId;

    private String observaciones;
}
