package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import pe.gob.municipalidad.sigm.modules.serenazgo.dto.CatalogoSeguridadResponse;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.ItemCatalogoResponse;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.TipoIncidenciaResponse;

import java.util.List;

public interface CatalogoSeguridadService {
    CatalogoSeguridadResponse obtenerCatalogosCompletos();
    List<TipoIncidenciaResponse> obtenerTiposIncidenciaActivos();
    List<ItemCatalogoResponse> obtenerCargosOperativos();
    List<ItemCatalogoResponse> obtenerTurnos();
    List<ItemCatalogoResponse> obtenerPrioridades();
    List<ItemCatalogoResponse> obtenerEstadosIncidencia();
}
