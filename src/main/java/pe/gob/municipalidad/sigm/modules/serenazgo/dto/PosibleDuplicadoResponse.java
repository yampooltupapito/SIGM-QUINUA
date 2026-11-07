package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIncidenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosibleDuplicadoResponse {

    private UUID id;
    private String codigo;
    private String titulo;
    private String tipoNombre;
    private String sectorNombre;
    private String ubicacionReferencia;
    private EstadoIncidenciaSeguridad estado;
    private PrioridadIncidenciaSeguridad prioridad;
    private OffsetDateTime fechaReporte;
    private String motivoAdvertencia;
}
