package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.CanalAlertaCiudadana;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelConfidencialidad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaRegistroAdminRequest {

    @NotBlank(message = "El título o resumen es obligatorio")
    @Size(max = 200)
    private String titulo;

    @NotNull(message = "El tipo de reporte es obligatorio")
    private UUID tipoReporteId;

    @NotNull(message = "El canal de recepción es obligatorio")
    @Builder.Default
    private CanalAlertaCiudadana canalOrigen = CanalAlertaCiudadana.LLAMADA_REGISTRADA;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 2000)
    private String descripcion;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime fechaHoraOcurrencia;

    @NotNull(message = "La prioridad administrativa es obligatoria")
    @Builder.Default
    private PrioridadIncidenciaSeguridad prioridad = PrioridadIncidenciaSeguridad.MEDIA;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(max = 255)
    private String ubicacionReferencia;

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private BigDecimal latitud;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private BigDecimal longitud;

    private UUID sectorId;
    private UUID zonaId;

    @Builder.Default
    private Boolean reportanteAnonimo = false;

    private String reportanteNombre;
    private String reportanteEmail;
    private String reportanteTelefonoContacto;

    @Builder.Default
    private NivelConfidencialidad nivelConfidencialidad = NivelConfidencialidad.ORDINARIO;

    private String observacionesInternas;
}
