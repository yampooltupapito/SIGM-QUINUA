package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadOperativaZona;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZonaUpdateRequest {

    @NotBlank(message = "El nombre de la zona es obligatorio")
    @Size(max = 120, message = "El nombre no debe superar 120 caracteres")
    private String nombre;

    private String descripcion;

    private PrioridadOperativaZona prioridadOperativa;

    private LocalDate fechaInicioVigencia;

    private LocalDate fechaFinVigencia;

    private EstadoSectorPatrullaje estado;

    private String observaciones;

    @NotNull(message = "La versión es obligatoria para control de concurrencia")
    private Long version;
}
