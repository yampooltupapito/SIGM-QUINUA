package pe.gob.municipalidad.sigm.modules.serenazgo.dto.mapa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIncidenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoPatrullajeSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoPatrullajeSerenazgo;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapaFiltroRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate desde;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate hasta;

    private UUID sectorId;
    private UUID zonaId;
    private UUID tipoIncidenciaId;
    private EstadoIncidenciaSeguridad estadoIncidencia;
    private PrioridadIncidenciaSeguridad prioridad;
    private TipoPatrullajeSerenazgo tipoPatrullaje;
    private EstadoPatrullajeSerenazgo estadoPatrullaje;
    private String busqueda;

    // Bounding Box (Viewport actual del mapa)
    private Double minLat;
    private Double maxLat;
    private Double minLng;
    private Double maxLng;

    // Filtro de capas activas
    private List<String> capasActivas;
}
