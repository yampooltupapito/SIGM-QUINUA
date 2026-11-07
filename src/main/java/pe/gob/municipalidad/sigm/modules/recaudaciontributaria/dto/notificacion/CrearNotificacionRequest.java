package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearNotificacionRequest {

    @NotNull(message = "El tipo de notificación es obligatorio")
    private UUID tipoNotificacionId;

    @NotNull(message = "El contribuyente es obligatorio")
    private UUID contribuyenteId;

    private UUID representanteId;

    @NotBlank(message = "El nombre del destinatario es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String destinatarioNombre;

    @NotBlank(message = "El número de documento del destinatario es obligatorio")
    @Size(max = 30, message = "El número de documento no puede exceder 30 caracteres")
    private String destinatarioNumeroDoc;

    @NotBlank(message = "El domicilio para notificación es obligatorio")
    @Size(max = 500, message = "El domicilio no puede exceder 500 caracteres")
    private String domicilioNotificacion;

    @Size(max = 255, message = "La referencia no puede exceder 255 caracteres")
    private String referenciaUbicacion;

    @NotNull(message = "El medio de notificación es obligatorio")
    private UUID medioNotificacionId;

    private Long documentoOrigenId;

    private UUID obligacionId;

    private UUID convenioId;

    private UUID seguimientoDeudaId;

    private LocalDate fechaProgramada;

    private String observaciones;

    private List<UUID> obligacionesIds;

    private String idempotencyKey;
}

