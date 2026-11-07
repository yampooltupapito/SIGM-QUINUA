package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadOperativaZona;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZonaCreateRequest {

    @NotNull(message = "El sector al que pertenece la zona es obligatorio")
    private UUID sectorId;

    @Size(max = 30, message = "El código no debe superar 30 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre de la zona es obligatorio")
    @Size(max = 120, message = "El nombre no debe superar 120 caracteres")
    private String nombre;

    private String descripcion;

    private PrioridadOperativaZona prioridadOperativa;

    private LocalDate fechaInicioVigencia;

    private LocalDate fechaFinVigencia;

    private EstadoSectorPatrullaje estado;

    private String observaciones;

    private String geometriaGeojson;
}
