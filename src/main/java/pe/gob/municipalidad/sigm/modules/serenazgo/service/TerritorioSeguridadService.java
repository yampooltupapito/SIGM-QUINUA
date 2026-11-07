package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEntidadTerritorial;

import java.util.UUID;

public interface TerritorioSeguridadService {

    DashboardTerritorialResponse obtenerDashboardTerritorial();

    GeoJsonFeatureCollectionResponse obtenerMapaTerritorialCompleto();

    ValidacionGeoJsonResult validarGeometria(String geoJsonStr);

    void importarGeoJson(ImportarGeoJsonRequest request, String username);

    String exportarGeoJson(TipoEntidadTerritorial tipo, UUID entidadId);

    byte[] exportarExcel(SectorFilter sectorFilter, ZonaFilter zonaFilter);

    byte[] generarFichaSectorPdf(UUID sectorId);
}
