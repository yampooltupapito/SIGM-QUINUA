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
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidenciaCreateRequest {

    @NotBlank(message = "El título resumido de la incidencia es obligatorio")
    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    private String titulo;

    @NotNull(message = "El tipo de incidencia es obligatorio")
    private UUID tipoIncidenciaId;

    @NotNull(message = "La prioridad administrativa es obligatoria")
    private PrioridadIncidenciaSeguridad prioridad;

    @NotBlank(message = "La descripción de los hechos comunicados u observados es obligatoria")
    private String descripcion;

    private OffsetDateTime fechaHoraOcurrencia;

    @Builder.Default
    private OrigenHechoIncidencia origenHecho = OrigenHechoIncidencia.HECHO_REPORTADO;

    @Builder.Default
    private CanalReporteIncidencia canalReporte = CanalReporteIncidencia.LLAMADA_OPERADOR;

    private UUID sectorId;
    private UUID zonaId;

    @NotBlank(message = "La referencia de ubicación o dirección es obligatoria")
    @Size(max = 255, message = "La ubicación no puede exceder los 255 caracteres")
    private String ubicacionReferencia;

    @DecimalMin(value = "-90.0", message = "La latitud debe ser mayor o igual a -90")
    @DecimalMax(value = "90.0", message = "La latitud debe ser menor o igual a 90")
    private BigDecimal latitud;

    @DecimalMin(value = "-180.0", message = "La longitud debe ser mayor o igual a -180")
    @DecimalMax(value = "180.0", message = "La longitud debe ser menor o igual a 180")
    private BigDecimal longitud;

    private Long oficinaResponsableId;
    private UUID personalReportaId;
    private UUID programacionServicioId;

    @Builder.Default
    private NivelConfidencialidad nivelConfidencialidad = NivelConfidencialidad.ORDINARIO;

    @Builder.Default
    private Boolean reportanteAnonimo = false;

    private String reportanteNombre;
    private String reportanteTelefono;
    private String reportanteDireccion;

    private String observacionesInternas;
}
