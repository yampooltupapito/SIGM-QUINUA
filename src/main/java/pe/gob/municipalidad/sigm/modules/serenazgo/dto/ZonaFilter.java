package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadOperativaZona;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZonaFilter {
    private String q;
    private UUID sectorId;
    private EstadoSectorPatrullaje estado;
    private PrioridadOperativaZona prioridad;
    private Boolean conDelimitacion;
}
