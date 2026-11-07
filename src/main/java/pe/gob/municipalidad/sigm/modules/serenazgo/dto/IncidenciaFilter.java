package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIncidenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelConfidencialidad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidenciaFilter {

    private String termino;
    private UUID tipoId;
    private EstadoIncidenciaSeguridad estado;
    private PrioridadIncidenciaSeguridad prioridad;
    private UUID sectorId;
    private UUID zonaId;
    private NivelConfidencialidad confidencialidad;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate desde;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate hasta;
}
