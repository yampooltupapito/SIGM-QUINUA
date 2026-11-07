package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.GeoJsonFeatureCollectionResponse;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.mapa.*;

import java.util.List;
import java.util.UUID;

public interface MapaGeograficoSerenazgoService {

    GeoJsonFeatureCollectionResponse obtenerCapaSectores(MapaFiltroRequest filtro);

    GeoJsonFeatureCollectionResponse obtenerCapaZonas(MapaFiltroRequest filtro);

    GeoJsonFeatureCollectionResponse obtenerCapaIncidencias(MapaFiltroRequest filtro, CustomUserDetails user);

    GeoJsonFeatureCollectionResponse obtenerCapaAlertas(MapaFiltroRequest filtro, CustomUserDetails user);

    GeoJsonFeatureCollectionResponse obtenerCapaIntervenciones(MapaFiltroRequest filtro, CustomUserDetails user);

    GeoJsonFeatureCollectionResponse obtenerCapaPatrullajes(MapaFiltroRequest filtro, CustomUserDetails user);

    GeoJsonFeatureCollectionResponse obtenerCapaRutas(MapaFiltroRequest filtro, CustomUserDetails user);

    GeoJsonFeatureCollectionResponse obtenerCapaPuntosControl(MapaFiltroRequest filtro, CustomUserDetails user);

    MapaEstadisticasResponse obtenerEstadisticas(MapaFiltroRequest filtro);

    ElementoGeograficoDetalleResponse obtenerDetalleElemento(String tipo, UUID id, CustomUserDetails user);

    List<ItemBusquedaMapaResponse> buscarElementos(String query, CustomUserDetails user);

    byte[] exportarExcel(MapaFiltroRequest filtro, CustomUserDetails user);

    byte[] exportarPdf(MapaFiltroRequest filtro, CustomUserDetails user);

    String exportarGeoJson(String capa, MapaFiltroRequest filtro, CustomUserDetails user);
}
