package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoUnidadMovil;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoUnidadMovil;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnidadMovilFilter {

    private String q; // Búsqueda textual por código, placa, marca o modelo
    private TipoUnidadMovil tipoUnidad;
    private String placa;
    private Long oficinaId;
    private EstadoUnidadMovil estadoOperativo;
    private Boolean disponible;
}
