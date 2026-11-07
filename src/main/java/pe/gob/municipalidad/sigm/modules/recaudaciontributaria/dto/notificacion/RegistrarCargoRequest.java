package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoVinculoReceptor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarCargoRequest {

    private UUID intentoId;

    private OffsetDateTime fechaRecepcionDeclarada;

    @NotBlank(message = "El nombre del receptor es obligatorio")
    @Size(max = 255, message = "El nombre del receptor no puede exceder 255 caracteres")
    private String receptorIdentificado;

    @Size(max = 30, message = "El número de documento no puede exceder 30 caracteres")
    private String receptorNumeroDoc;

    @NotNull(message = "El tipo de vínculo del receptor es obligatorio")
    private TipoVinculoReceptor tipoVinculoReceptor;

    private Long documentoEvidenciaId;

    private Boolean tieneFirma;

    private Boolean tieneSello;

    private String observaciones;
}

