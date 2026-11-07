package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.OrigenIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.PrioridadIncidencia;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaCreateRequest {

    @NotNull(message = "El mercado municipal es obligatorio")
    private UUID mercadoId;

    private UUID sectorId;
    private UUID pabellonId;
    private UUID puestoId;
    private UUID comercianteId;

    @NotNull(message = "El tipo de incidencia es obligatorio")
    private UUID tipoIncidenciaId;

    @NotNull(message = "El origen del reporte es obligatorio")
    private OrigenIncidencia origen;

    @NotBlank(message = "La descripción de los hechos es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    private String descripcion;

    private OffsetDateTime fechaOcurrencia;

    private PrioridadIncidencia prioridad;

    private String reportanteNombre;
    private String reportanteContacto;
    private Boolean esAnonimo;

    private Long oficinaResponsableId;
    private Long responsableId;
    private UUID inspeccionId;
    private UUID expedienteId;
    private String expedienteNumero;
    private String observaciones;
}
