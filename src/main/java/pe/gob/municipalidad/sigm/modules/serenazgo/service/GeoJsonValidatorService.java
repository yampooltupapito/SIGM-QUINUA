package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.ValidacionGeoJsonResult;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoJsonValidatorService {

    private final ObjectMapper objectMapper;

    /**
     * Valida sintáctica y geométricamente un texto GeoJSON conforme a RFC 7946 (WGS84).
     */
    public ValidacionGeoJsonResult validar(String geoJsonStr) {
        List<String> advertencias = new ArrayList<>();

        if (geoJsonStr == null || geoJsonStr.trim().isEmpty()) {
            return ValidacionGeoJsonResult.builder()
                    .valido(false)
                    .mensajeError("El contenido GeoJSON no puede estar vacío")
                    .advertencias(advertencias)
                    .build();
        }

        try {
            JsonNode root = objectMapper.readTree(geoJsonStr.trim());
            JsonNode geometryNode = root;

            if (root.has("type")) {
                String rootType = root.get("type").asText();
                if ("Feature".equalsIgnoreCase(rootType) && root.has("geometry")) {
                    geometryNode = root.get("geometry");
                } else if ("FeatureCollection".equalsIgnoreCase(rootType) && root.has("features") && root.get("features").isArray()) {
                    if (root.get("features").isEmpty()) {
                        return ValidacionGeoJsonResult.builder()
                                .valido(false)
                                .mensajeError("El FeatureCollection no contiene ningún elemento")
                                .build();
                    }
                    geometryNode = root.get("features").get(0).get("geometry");
                    if (root.get("features").size() > 1) {
                        advertencias.add("El FeatureCollection contenía múltiples geometrías; se tomó la primera");
                    }
                }
            }

            if (geometryNode == null || !geometryNode.has("type") || !geometryNode.has("coordinates")) {
                return ValidacionGeoJsonResult.builder()
                        .valido(false)
                        .mensajeError("Estructura GeoJSON inválida: se requiere un objeto con 'type' y 'coordinates'")
                        .build();
            }

            String geomType = geometryNode.get("type").asText();
            JsonNode coordinates = geometryNode.get("coordinates");

            if (!"Polygon".equalsIgnoreCase(geomType) && !"MultiPolygon".equalsIgnoreCase(geomType)) {
                return ValidacionGeoJsonResult.builder()
                        .valido(false)
                        .mensajeError("Tipo de geometría no permitido: '" + geomType + "'. Solo se admiten 'Polygon' o 'MultiPolygon'")
                        .build();
            }

            int totalVertices = 0;
            double minLon = Double.MAX_VALUE;
            double maxLon = -Double.MAX_VALUE;
            double minLat = Double.MAX_VALUE;
            double maxLat = -Double.MAX_VALUE;
            double sumLon = 0.0;
            double sumLat = 0.0;

            if ("Polygon".equalsIgnoreCase(geomType)) {
                if (!coordinates.isArray() || coordinates.isEmpty()) {
                    return ValidacionGeoJsonResult.builder()
                            .valido(false)
                            .mensajeError("El polígono no posee anillos de coordenadas")
                            .build();
                }

                JsonNode exteriorRing = coordinates.get(0);
                if (!exteriorRing.isArray() || exteriorRing.size() < 4) {
                    return ValidacionGeoJsonResult.builder()
                            .valido(false)
                            .mensajeError("El anillo exterior del polígono debe tener al menos 4 puntos (3 distintos más cierre)")
                            .build();
                }

                for (JsonNode ring : coordinates) {
                    if (!ring.isArray() || ring.size() < 4) {
                        return ValidacionGeoJsonResult.builder()
                                .valido(false)
                                .mensajeError("Cada anillo del polígono debe tener al menos 4 puntos")
                                .build();
                    }

                    JsonNode firstPoint = ring.get(0);
                    JsonNode lastPoint = ring.get(ring.size() - 1);

                    double firstLon = firstPoint.get(0).asDouble();
                    double firstLat = firstPoint.get(1).asDouble();
                    double lastLon = lastPoint.get(0).asDouble();
                    double lastLat = lastPoint.get(1).asDouble();

                    if (Math.abs(firstLon - lastLon) > 1e-6 || Math.abs(firstLat - lastLat) > 1e-6) {
                        return ValidacionGeoJsonResult.builder()
                                .valido(false)
                                .mensajeError("El anillo del polígono no está cerrado: el primer punto [" + firstLon + "," + firstLat + "] no coincide con el último [" + lastLon + "," + lastLat + "]")
                                .build();
                    }

                    for (int i = 0; i < ring.size(); i++) {
                        JsonNode pt = ring.get(i);
                        if (!pt.isArray() || pt.size() < 2) {
                            return ValidacionGeoJsonResult.builder()
                                    .valido(false)
                                    .mensajeError("Punto inválido en anillo: debe contener al menos [longitud, latitud]")
                                    .build();
                        }
                        double lon = pt.get(0).asDouble();
                        double lat = pt.get(1).asDouble();

                        if (lon < -180.0 || lon > 180.0) {
                            return ValidacionGeoJsonResult.builder()
                                    .valido(false)
                                    .mensajeError("Longitud fuera de rango WGS84 [-180, 180]: " + lon)
                                    .build();
                        }
                        if (lat < -90.0 || lat > 90.0) {
                            return ValidacionGeoJsonResult.builder()
                                    .valido(false)
                                    .mensajeError("Latitud fuera de rango WGS84 [-90, 90]: " + lat)
                                    .build();
                        }

                        // Solo contabilizamos los puntos sin el punto de cierre duplicado para el centroide
                        if (i < ring.size() - 1) {
                            totalVertices++;
                            sumLon += lon;
                            sumLat += lat;
                            minLon = Math.min(minLon, lon);
                            maxLon = Math.max(maxLon, lon);
                            minLat = Math.min(minLat, lat);
                            maxLat = Math.max(maxLat, lat);
                        }
                    }
                }
            } else if ("MultiPolygon".equalsIgnoreCase(geomType)) {
                if (!coordinates.isArray() || coordinates.isEmpty()) {
                    return ValidacionGeoJsonResult.builder()
                            .valido(false)
                            .mensajeError("El MultiPolygon no contiene polígonos")
                            .build();
                }

                for (JsonNode polygon : coordinates) {
                    if (!polygon.isArray() || polygon.isEmpty()) continue;
                    JsonNode exteriorRing = polygon.get(0);
                    if (!exteriorRing.isArray() || exteriorRing.size() < 4) {
                        return ValidacionGeoJsonResult.builder()
                                .valido(false)
                                .mensajeError("Cada anillo exterior en MultiPolygon debe tener al menos 4 puntos")
                                .build();
                    }

                    JsonNode firstPoint = exteriorRing.get(0);
                    JsonNode lastPoint = exteriorRing.get(exteriorRing.size() - 1);
                    double firstLon = firstPoint.get(0).asDouble();
                    double firstLat = firstPoint.get(1).asDouble();
                    double lastLon = lastPoint.get(0).asDouble();
                    double lastLat = lastPoint.get(1).asDouble();

                    if (Math.abs(firstLon - lastLon) > 1e-6 || Math.abs(firstLat - lastLat) > 1e-6) {
                        return ValidacionGeoJsonResult.builder()
                                .valido(false)
                                .mensajeError("Un anillo de MultiPolygon no está cerrado correctamente")
                                .build();
                    }

                    for (int i = 0; i < exteriorRing.size() - 1; i++) {
                        JsonNode pt = exteriorRing.get(i);
                        double lon = pt.get(0).asDouble();
                        double lat = pt.get(1).asDouble();
                        totalVertices++;
                        sumLon += lon;
                        sumLat += lat;
                        minLon = Math.min(minLon, lon);
                        maxLon = Math.max(maxLon, lon);
                        minLat = Math.min(minLat, lat);
                        maxLat = Math.max(maxLat, lat);
                    }
                }
            }

            double centroideLon = totalVertices > 0 ? (sumLon / totalVertices) : 0.0;
            double centroideLat = totalVertices > 0 ? (sumLat / totalVertices) : 0.0;

            return ValidacionGeoJsonResult.builder()
                    .valido(true)
                    .tipoGeometria(geomType)
                    .numeroVertices(totalVertices)
                    .centroideLongitud(centroideLon)
                    .centroideLatitud(centroideLat)
                    .minLongitud(minLon)
                    .minLatitud(minLat)
                    .maxLongitud(maxLon)
                    .maxLatitud(maxLat)
                    .advertencias(advertencias)
                    .build();

        } catch (Exception e) {
            log.warn("Error al parsear GeoJSON: {}", e.getMessage());
            return ValidacionGeoJsonResult.builder()
                    .valido(false)
                    .mensajeError("Error de formato JSON: " + e.getMessage())
                    .advertencias(advertencias)
                    .build();
        }
    }

    /**
     * Comprueba si una delimitación de zona se encuentra espacialmente contenida dentro del sector
     * mediante comparación de Bounding Box.
     */
    public boolean verificarContencionAproximada(String geoJsonSector, String geoJsonZona) {
        ValidacionGeoJsonResult vSector = validar(geoJsonSector);
        ValidacionGeoJsonResult vZona = validar(geoJsonZona);

        if (!vSector.isValido() || !vZona.isValido()) {
            return false;
        }

        double tol = 0.005; // Margen de tolerancia de ~500 metros
        return vZona.getMinLongitud() >= (vSector.getMinLongitud() - tol)
                && vZona.getMaxLongitud() <= (vSector.getMaxLongitud() + tol)
                && vZona.getMinLatitud() >= (vSector.getMinLatitud() - tol)
                && vZona.getMaxLatitud() <= (vSector.getMaxLatitud() + tol);
    }

    /**
     * Comprueba si dos delimitaciones tienen solapamiento de Bounding Box.
     */
    public boolean verificarSolapamientoBbox(String geoJsonA, String geoJsonB) {
        ValidacionGeoJsonResult vA = validar(geoJsonA);
        ValidacionGeoJsonResult vB = validar(geoJsonB);

        if (!vA.isValido() || !vB.isValido()) return false;

        return !(vA.getMaxLongitud() < vB.getMinLongitud()
                || vA.getMinLongitud() > vB.getMaxLongitud()
                || vA.getMaxLatitud() < vB.getMinLatitud()
                || vA.getMinLatitud() > vB.getMaxLatitud());
    }
}
