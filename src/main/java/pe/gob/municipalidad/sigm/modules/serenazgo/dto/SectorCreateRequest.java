package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectorCreateRequest {

    @Size(max = 30, message = "El código no debe superar 30 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre del sector es obligatorio")
    @Size(max = 120, message = "El nombre no debe superar 120 caracteres")
    private String nombre;

    private String descripcion;

    @Size(max = 10, message = "El color hexadecimal no debe superar 10 caracteres")
    private String colorHex;

    private Long oficinaResponsableId;

    private LocalDate fechaInicioVigencia;

    private LocalDate fechaFinVigencia;

    private EstadoSectorPatrullaje estado;

    private String observaciones;

    private String geometriaGeojson;
}
