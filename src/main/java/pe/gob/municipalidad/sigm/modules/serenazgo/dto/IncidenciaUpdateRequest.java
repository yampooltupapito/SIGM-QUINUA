package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.CanalReporteIncidencia;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelConfidencialidad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.OrigenHechoIncidencia;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidenciaUpdateRequest {

    @NotBlank(message = "El título resumido de la incidencia es obligatorio")
    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    private String titulo;

    @NotNull(message = "El tipo de incidencia es obligatorio")
    private UUID tipoIncidenciaId;

    @NotBlank(message = "La descripción de los hechos es obligatoria")
    private String descripcion;

    private OffsetDateTime fechaHoraOcurrencia;

    private OrigenHechoIncidencia origenHecho;
    private CanalReporteIncidencia canalReporte;

    private UUID sectorId;
    private UUID zonaId;

    @NotBlank(message = "La referencia de ubicación es obligatoria")
    private String ubicacionReferencia;

    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    private BigDecimal latitud;

    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    private BigDecimal longitud;

    private Long oficinaResponsableId;
    private UUID personalReportaId;
    private UUID programacionServicioId;

    private NivelConfidencialidad nivelConfidencialidad;
    private Boolean reportanteAnonimo;
    private String reportanteNombre;
    private String reportanteTelefono;
    private String reportanteDireccion;

    private String observacionesInternas;

    @NotBlank(message = "El motivo de la modificación es obligatorio para fines de auditoría")
    private String motivoModificacion;
}
