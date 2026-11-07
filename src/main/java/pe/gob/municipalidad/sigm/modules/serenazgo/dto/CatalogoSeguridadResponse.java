package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoSeguridadResponse {
    private List<ItemCatalogoResponse> cargosOperativos;
    private List<TipoIncidenciaResponse> tiposIncidencia;
    private List<ItemCatalogoResponse> turnos;
    private List<ItemCatalogoResponse> prioridades;
    private List<ItemCatalogoResponse> estadosIncidencia;
    private List<ItemCatalogoResponse> canalesReporte;
    private List<ItemCatalogoResponse> tiposUnidad;
    private List<ItemCatalogoResponse> estadosUnidad;
    private List<ItemCatalogoResponse> tiposPatrullaje;
    private List<ItemCatalogoResponse> resultadosIntervencion;
}
