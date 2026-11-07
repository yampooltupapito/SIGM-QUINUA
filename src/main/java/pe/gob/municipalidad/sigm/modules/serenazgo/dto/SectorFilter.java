package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectorFilter {
    private String q;
    private Long oficinaId;
    private EstadoSectorPatrullaje estado;
    private Boolean conDelimitacion;
    private Boolean vigente;
}
