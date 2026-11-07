package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAtencionIncidencia;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionFilter {

    private String termino;

    private String codigo;

    private EstadoAtencionIncidencia estado;

    private PrioridadIncidenciaSeguridad prioridad;

    private UUID sectorId;

    private UUID unidadId;

    private UUID personalId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate desde;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate hasta;
}
