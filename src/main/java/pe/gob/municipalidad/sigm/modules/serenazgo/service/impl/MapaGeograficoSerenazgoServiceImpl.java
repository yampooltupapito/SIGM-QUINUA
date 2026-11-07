package pe.gob.municipalidad.sigm.modules.serenazgo.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.GeoJsonFeatureCollectionResponse;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.GeoJsonFeatureCollectionResponse.GeoJsonFeature;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.mapa.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIncidenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoPatrullajeSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelConfidencialidad;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.MapaGeograficoSerenazgoService;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapaGeograficoSerenazgoServiceImpl implements MapaGeograficoSerenazgoService {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SectorPatrullajeRepository sectorRepository;
    private final ZonaPatrullajeRepository zonaRepository;
    private final IncidenciaSeguridadCiudadanaRepository incidenciaRepository;
    private final AlertaCiudadanaSeguridadRepository alertaRepository;
    private final IntervencionSerenazgoRepository intervencionRepository;
    private final RutaPatrullajeRepository rutaRepository;
    private final PuntoControlRutaRepository puntoControlRepository;
    private final PatrullajeSerenazgoRepository patrullajeRepository;
    private final CapaInstitucionalGeograficaRepository capaRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public GeoJsonFeatureCollectionResponse obtenerCapaSectores(MapaFiltroRequest filtro) {
        List<GeoJsonFeature> features = new ArrayList<>();
        List<SectorPatrullaje> sectores = (filtro != null && filtro.getSectorId() != null)
                ? sectorRepository.findById(filtro.getSectorId()).map(List::of).orElse(Collections.emptyList())
                : sectorRepository.findAll();

        for (SectorPatrullaje s : sectores) {
            if (s.getTieneDelimitacion() != null && s.getTieneDelimitacion() && s.getGeometriaGeojson() != null && !s.getGeometriaGeojson().isBlank()) {
                try {
                    JsonNode geom = parsearGeoJson(s.getGeometriaGeojson());
                    if (geom != null) {
                        Map<String, Object> props = new LinkedHashMap<>();
                        props.put("tipo", "SECTOR");
                        props.put("id", s.getId().toString());
                        props.put("codigo", s.getCodigo());
                        props.put("nombre", s.getNombre());
                        props.put("descripcion", s.getDescripcion());
                        props.put("colorHex", s.getColorHex() != null ? s.getColorHex() : "#3b82f6");
                        props.put("estado", s.getEstado() != null ? s.getEstado().name() : "ACTIVO");
                        props.put("estadoDesc", s.getEstado() != null ? s.getEstado().getDescripcion() : "Activo");
                        props.put("totalZonas", s.getZonas() != null ? s.getZonas().size() : 0);

                        features.add(GeoJsonFeature.builder()
                                .id("sector-" + s.getId())
                                .properties(props)
                                .geometry(geom)
                                .build());
                    }
                } catch (Exception e) {
                    log.warn("Error al procesar geometría del sector {}: {}", s.getCodigo(), e.getMessage());
                }
            }
        }
        return GeoJsonFeatureCollectionResponse.builder().features(features).build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeoJsonFeatureCollectionResponse obtenerCapaZonas(MapaFiltroRequest filtro) {
        List<GeoJsonFeature> features = new ArrayList<>();
        List<ZonaPatrullaje> zonas = (filtro != null && filtro.getZonaId() != null)
                ? zonaRepository.findById(filtro.getZonaId()).map(List::of).orElse(Collections.emptyList())
                : (filtro != null && filtro.getSectorId() != null)
                ? zonaRepository.findBySectorId(filtro.getSectorId())
                : zonaRepository.findAll();

        for (ZonaPatrullaje z : zonas) {
            if (z.getTieneDelimitacion() != null && z.getTieneDelimitacion() && z.getGeometriaGeojson() != null && !z.getGeometriaGeojson().isBlank()) {
                try {
                    JsonNode geom = parsearGeoJson(z.getGeometriaGeojson());
                    if (geom != null) {
                        Map<String, Object> props = new LinkedHashMap<>();
                        props.put("tipo", "ZONA");
                        props.put("id", z.getId().toString());
                        props.put("codigo", z.getCodigo());
                        props.put("nombre", z.getNombre());
                        props.put("descripcion", z.getDescripcion());
                        props.put("colorHex", z.getSector() != null && z.getSector().getColorHex() != null ? z.getSector().getColorHex() : "#10b981");
                        props.put("estado", z.getEstado() != null ? z.getEstado().name() : "ACTIVO");
                        props.put("estadoDesc", z.getEstado() != null ? z.getEstado().getDescripcion() : "Activo");
                        props.put("prioridad", z.getPrioridadOperativa() != null ? z.getPrioridadOperativa().name() : "MEDIA");
                        props.put("prioridadDesc", z.getPrioridadOperativa() != null ? z.getPrioridadOperativa().getDescripcion() : "Media");
                        props.put("sectorId", z.getSector() != null ? z.getSector().getId().toString() : null);
                        props.put("sectorNombre", z.getSector() != null ? z.getSector().getNombre() : null);

                        features.add(GeoJsonFeature.builder()
                                .id("zona-" + z.getId())
                                .properties(props)
                                .geometry(geom)
                                .build());
                    }
                } catch (Exception e) {
                    log.warn("Error al procesar geometría de la zona {}: {}", z.getCodigo(), e.getMessage());
                }
            }
        }
        return GeoJsonFeatureCollectionResponse.builder().features(features).build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeoJsonFeatureCollectionResponse obtenerCapaIncidencias(MapaFiltroRequest filtro, CustomUserDetails user) {
        if (!tienePermiso(user, "SERENAZGO_MAPA_VER_INCIDENCIAS") && !tienePermiso(user, "SERENAZGO_INCIDENCIAS_VER")) {
            return GeoJsonFeatureCollectionResponse.builder().features(Collections.emptyList()).build();
        }

        Specification<IncidenciaSeguridadCiudadana> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            preds.add(cb.isNotNull(root.get("latitud")));
            preds.add(cb.isNotNull(root.get("longitud")));

            if (filtro != null) {
                if (filtro.getSectorId() != null) {
                    preds.add(cb.equal(root.get("sector").get("id"), filtro.getSectorId()));
                }
                if (filtro.getZonaId() != null) {
                    preds.add(cb.equal(root.get("zona").get("id"), filtro.getZonaId()));
                }
                if (filtro.getTipoIncidenciaId() != null) {
                    preds.add(cb.equal(root.get("tipoIncidencia").get("id"), filtro.getTipoIncidenciaId()));
                }
                if (filtro.getEstadoIncidencia() != null) {
                    preds.add(cb.equal(root.get("estado"), filtro.getEstadoIncidencia()));
                }
                if (filtro.getPrioridad() != null) {
                    preds.add(cb.equal(root.get("prioridad"), filtro.getPrioridad()));
                }
                if (filtro.getDesde() != null) {
                    preds.add(cb.greaterThanOrEqualTo(root.get("fechaReporte"), filtro.getDesde().atStartOfDay(ZONA_LIMA).toOffsetDateTime()));
                }
                if (filtro.getHasta() != null) {
                    preds.add(cb.lessThan(root.get("fechaReporte"), filtro.getHasta().plusDays(1).atStartOfDay(ZONA_LIMA).toOffsetDateTime()));
                }
                if (filtro.getBusqueda() != null && !filtro.getBusqueda().isBlank()) {
                    String term = "%" + filtro.getBusqueda().trim().toLowerCase() + "%";
                    preds.add(cb.or(
                            cb.like(cb.lower(root.get("codigo")), term),
                            cb.like(cb.lower(root.get("titulo")), term),
                            cb.like(cb.lower(root.get("ubicacionReferencia")), term)
                    ));
                }
                // Bounding box
                if (sonCoordenadasValidas(filtro.getMinLat(), filtro.getMaxLat(), filtro.getMinLng(), filtro.getMaxLng())) {
                    preds.add(cb.greaterThanOrEqualTo(root.get("latitud"), BigDecimal.valueOf(filtro.getMinLat())));
                    preds.add(cb.lessThanOrEqualTo(root.get("latitud"), BigDecimal.valueOf(filtro.getMaxLat())));
                    preds.add(cb.greaterThanOrEqualTo(root.get("longitud"), BigDecimal.valueOf(filtro.getMinLng())));
                    preds.add(cb.lessThanOrEqualTo(root.get("longitud"), BigDecimal.valueOf(filtro.getMaxLng())));
                }
            }
            return cb.and(preds.toArray(new Predicate[0]));
        };

        List<IncidenciaSeguridadCiudadana> lista = incidenciaRepository.findAll(spec);
        List<GeoJsonFeature> features = new ArrayList<>();

        for (IncidenciaSeguridadCiudadana inc : lista) {
            double lat = inc.getLatitud().doubleValue();
            double lng = inc.getLongitud().doubleValue();
            if (!esPuntoValido(lat, lng)) continue;

            Map<String, Object> props = new LinkedHashMap<>();
            props.put("tipo", "INCIDENCIA");
            props.put("id", inc.getId().toString());
            props.put("codigo", inc.getCodigo());
            props.put("titulo", inc.getTitulo());
            props.put("tipoIncidencia", inc.getTipoIncidencia() != null ? inc.getTipoIncidencia().getNombre() : "-");
            props.put("estado", inc.getEstado().name());
            props.put("estadoDesc", inc.getEstado().getDescripcion());
            props.put("prioridad", inc.getPrioridad().name());
            props.put("prioridadDesc", inc.getPrioridad().getDescripcion());
            props.put("fechaReporte", inc.getFechaReporte().format(FORMATO_FECHA_HORA));
            props.put("sectorNombre", inc.getSector() != null ? inc.getSector().getNombre() : "-");
            props.put("zonaNombre", inc.getZona() != null ? inc.getZona().getNombre() : "-");
            props.put("ubicacionReferencia", inc.getUbicacionReferencia());
            props.put("urlDetalle", "/dashboard/serenazgo/incidencias/" + inc.getId());

            features.add(GeoJsonFeature.builder()
                    .id("incidencia-" + inc.getId())
                    .properties(props)
                    .geometry(crearPuntoGeoJson(lng, lat))
                    .build());
        }
        return GeoJsonFeatureCollectionResponse.builder().features(features).build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeoJsonFeatureCollectionResponse obtenerCapaAlertas(MapaFiltroRequest filtro, CustomUserDetails user) {
        if (!tienePermiso(user, "SERENAZGO_MAPA_VER_ALERTAS") && !tienePermiso(user, "SERENAZGO_ALERTAS_VER")) {
            return GeoJsonFeatureCollectionResponse.builder().features(Collections.emptyList()).build();
        }

        Specification<AlertaCiudadanaSeguridad> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            preds.add(cb.isNotNull(root.get("latitud")));
            preds.add(cb.isNotNull(root.get("longitud")));

            if (filtro != null) {
                if (filtro.getSectorId() != null) {
                    preds.add(cb.equal(root.get("sector").get("id"), filtro.getSectorId()));
                }
                if (filtro.getZonaId() != null) {
                    preds.add(cb.equal(root.get("zona").get("id"), filtro.getZonaId()));
                }
                if (filtro.getPrioridad() != null) {
                    preds.add(cb.equal(root.get("prioridad"), filtro.getPrioridad()));
                }
                if (filtro.getDesde() != null) {
                    preds.add(cb.greaterThanOrEqualTo(root.get("fechaRecepcion"), filtro.getDesde().atStartOfDay(ZONA_LIMA).toOffsetDateTime()));
                }
                if (filtro.getHasta() != null) {
                    preds.add(cb.lessThan(root.get("fechaRecepcion"), filtro.getHasta().plusDays(1).atStartOfDay(ZONA_LIMA).toOffsetDateTime()));
                }
                if (sonCoordenadasValidas(filtro.getMinLat(), filtro.getMaxLat(), filtro.getMinLng(), filtro.getMaxLng())) {
                    preds.add(cb.greaterThanOrEqualTo(root.get("latitud"), BigDecimal.valueOf(filtro.getMinLat())));
                    preds.add(cb.lessThanOrEqualTo(root.get("latitud"), BigDecimal.valueOf(filtro.getMaxLat())));
                    preds.add(cb.greaterThanOrEqualTo(root.get("longitud"), BigDecimal.valueOf(filtro.getMinLng())));
                    preds.add(cb.lessThanOrEqualTo(root.get("longitud"), BigDecimal.valueOf(filtro.getMaxLng())));
                }
            }
            return cb.and(preds.toArray(new Predicate[0]));
        };

        List<AlertaCiudadanaSeguridad> lista = alertaRepository.findAll(spec);
        List<GeoJsonFeature> features = new ArrayList<>();

        for (AlertaCiudadanaSeguridad al : lista) {
            double lat = al.getLatitud().doubleValue();
            double lng = al.getLongitud().doubleValue();
            if (!esPuntoValido(lat, lng)) continue;

            Map<String, Object> props = new LinkedHashMap<>();
            props.put("tipo", "ALERTA");
            props.put("id", al.getId().toString());
            props.put("codigo", al.getCodigo());
            props.put("titulo", al.getTitulo());
            props.put("estado", al.getEstado().name());
            props.put("estadoDesc", al.getEstado().getDescripcion());
            props.put("prioridad", al.getPrioridad().name());
            props.put("prioridadDesc", al.getPrioridad().getDescripcion());
            props.put("fechaRecepcion", al.getFechaRecepcion().format(FORMATO_FECHA_HORA));
            props.put("sectorNombre", al.getSector() != null ? al.getSector().getNombre() : "-");
            props.put("zonaNombre", al.getZona() != null ? al.getZona().getNombre() : "-");
            props.put("ubicacionReferencia", al.getUbicacionReferencia());
            props.put("urlDetalle", "/dashboard/serenazgo/alertas/" + al.getId());

            features.add(GeoJsonFeature.builder()
                    .id("alerta-" + al.getId())
                    .properties(props)
                    .geometry(crearPuntoGeoJson(lng, lat))
                    .build());
        }
        return GeoJsonFeatureCollectionResponse.builder().features(features).build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeoJsonFeatureCollectionResponse obtenerCapaIntervenciones(MapaFiltroRequest filtro, CustomUserDetails user) {
        if (!tienePermiso(user, "SERENAZGO_MAPA_VER_INTERVENCIONES") && !tienePermiso(user, "SERENAZGO_INTERVENCIONES_VER")) {
            return GeoJsonFeatureCollectionResponse.builder().features(Collections.emptyList()).build();
        }

        Specification<IntervencionSerenazgo> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            preds.add(cb.isNotNull(root.get("latitud")));
            preds.add(cb.isNotNull(root.get("longitud")));

            if (filtro != null) {
                if (filtro.getSectorId() != null) {
                    preds.add(cb.equal(root.get("sector").get("id"), filtro.getSectorId()));
                }
                if (filtro.getZonaId() != null) {
                    preds.add(cb.equal(root.get("zona").get("id"), filtro.getZonaId()));
                }
                if (filtro.getDesde() != null) {
                    preds.add(cb.greaterThanOrEqualTo(root.get("fechaInicio"), filtro.getDesde().atStartOfDay(ZONA_LIMA).toOffsetDateTime()));
                }
                if (filtro.getHasta() != null) {
                    preds.add(cb.lessThan(root.get("fechaInicio"), filtro.getHasta().plusDays(1).atStartOfDay(ZONA_LIMA).toOffsetDateTime()));
                }
                if (sonCoordenadasValidas(filtro.getMinLat(), filtro.getMaxLat(), filtro.getMinLng(), filtro.getMaxLng())) {
                    preds.add(cb.greaterThanOrEqualTo(root.get("latitud"), filtro.getMinLat()));
                    preds.add(cb.lessThanOrEqualTo(root.get("latitud"), filtro.getMaxLat()));
                    preds.add(cb.greaterThanOrEqualTo(root.get("longitud"), filtro.getMinLng()));
                    preds.add(cb.lessThanOrEqualTo(root.get("longitud"), filtro.getMaxLng()));
                }
            }
            return cb.and(preds.toArray(new Predicate[0]));
        };

        List<IntervencionSerenazgo> lista = intervencionRepository.findAll(spec);
        List<GeoJsonFeature> features = new ArrayList<>();

        for (IntervencionSerenazgo it : lista) {
            double lat = it.getLatitud();
            double lng = it.getLongitud();
            if (!esPuntoValido(lat, lng)) continue;

            Map<String, Object> props = new LinkedHashMap<>();
            props.put("tipo", "INTERVENCION");
            props.put("id", it.getId().toString());
            props.put("codigo", it.getCodigo());
            props.put("titulo", "Intervención " + it.getCodigo());
            props.put("tipoIntervencion", it.getTipoIntervencion() != null ? it.getTipoIntervencion().getDescripcion() : "-");
            props.put("estado", it.getEstado().name());
            props.put("estadoDesc", it.getEstado().getDescripcion());
            props.put("resultado", it.getResultado() != null ? it.getResultado().getDescripcion() : "-");
            props.put("fechaHora", it.getFechaInicio().format(FORMATO_FECHA_HORA));
            props.put("sectorNombre", it.getSector() != null ? it.getSector().getNombre() : "-");
            props.put("zonaNombre", it.getZona() != null ? it.getZona().getNombre() : "-");
            props.put("ubicacionReferencia", it.getUbicacionDescripcion());
            props.put("urlDetalle", "/dashboard/serenazgo/intervenciones/" + it.getId());

            features.add(GeoJsonFeature.builder()
                    .id("intervencion-" + it.getId())
                    .properties(props)
                    .geometry(crearPuntoGeoJson(lng, lat))
                    .build());
        }
        return GeoJsonFeatureCollectionResponse.builder().features(features).build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeoJsonFeatureCollectionResponse obtenerCapaPatrullajes(MapaFiltroRequest filtro, CustomUserDetails user) {
        if (!tienePermiso(user, "SERENAZGO_MAPA_VER_PATRULLAJES") && !tienePermiso(user, "SERENAZGO_PATRULLAJES_VER")) {
            return GeoJsonFeatureCollectionResponse.builder().features(Collections.emptyList()).build();
        }

        Specification<PatrullajeSerenazgo> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            preds.add(cb.isNotNull(root.get("ruta")));

            if (filtro != null) {
                if (filtro.getSectorId() != null) {
                    preds.add(cb.equal(root.get("sector").get("id"), filtro.getSectorId()));
                }
                if (filtro.getEstadoPatrullaje() != null) {
                    preds.add(cb.equal(root.get("estado"), filtro.getEstadoPatrullaje()));
                }
                if (filtro.getTipoPatrullaje() != null) {
                    preds.add(cb.equal(root.get("tipoPatrullaje"), filtro.getTipoPatrullaje()));
                }
                if (filtro.getDesde() != null) {
                    preds.add(cb.greaterThanOrEqualTo(root.get("fechaHoraInicioProgramada"), filtro.getDesde().atStartOfDay(ZONA_LIMA).toOffsetDateTime()));
                }
                if (filtro.getHasta() != null) {
                    preds.add(cb.lessThan(root.get("fechaHoraInicioProgramada"), filtro.getHasta().plusDays(1).atStartOfDay(ZONA_LIMA).toOffsetDateTime()));
                }
            }
            return cb.and(preds.toArray(new Predicate[0]));
        };

        List<PatrullajeSerenazgo> lista = patrullajeRepository.findAll(spec);
        List<GeoJsonFeature> features = new ArrayList<>();

        for (PatrullajeSerenazgo p : lista) {
            RutaPatrullaje ruta = p.getRuta();
            if (ruta == null || ruta.getPuntosControl() == null || ruta.getPuntosControl().isEmpty()) continue;

            // Recopilar coordenadas en orden
            List<List<Double>> coords = new ArrayList<>();
            for (PuntoControlRuta pt : ruta.getPuntosControl()) {
                if (pt.getLatitud() != null && pt.getLongitud() != null) {
                    double lat = pt.getLatitud().doubleValue();
                    double lng = pt.getLongitud().doubleValue();
                    if (esPuntoValido(lat, lng)) {
                        coords.add(List.of(lng, lat));
                    }
                }
            }

            if (coords.size() >= 2) {
                Map<String, Object> props = new LinkedHashMap<>();
                props.put("tipo", "PATRULLAJE");
                props.put("id", p.getId().toString());
                props.put("codigo", p.getCodigo());
                props.put("tipoPatrullaje", p.getTipoPatrullaje().getDescripcion());
                props.put("estado", p.getEstado().name());
                props.put("estadoDesc", p.getEstado().getDescripcion());
                props.put("rutaCodigo", ruta.getCodigo());
                props.put("rutaNombre", ruta.getNombre());
                props.put("sectorNombre", p.getSector() != null ? p.getSector().getNombre() : "-");
                props.put("responsableNombre", p.getResponsable() != null && p.getResponsable().getPersona() != null ? p.getResponsable().getPersona().getNombreCompleto() : "-");
                props.put("unidadPlaca", p.getUnidadMovil() != null ? p.getUnidadMovil().getPlaca() : "-");
                props.put("totalCheckpoints", ruta.getPuntosControl().size());
                props.put("urlDetalle", "/dashboard/serenazgo/patrullajes/" + p.getId());

                Map<String, Object> lineGeom = new LinkedHashMap<>();
                lineGeom.put("type", "LineString");
                lineGeom.put("coordinates", coords);

                features.add(GeoJsonFeature.builder()
                        .id("patrullaje-" + p.getId())
                        .properties(props)
                        .geometry(lineGeom)
                        .build());
            }
        }
        return GeoJsonFeatureCollectionResponse.builder().features(features).build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeoJsonFeatureCollectionResponse obtenerCapaRutas(MapaFiltroRequest filtro, CustomUserDetails user) {
        List<RutaPatrullaje> rutas = (filtro != null && filtro.getSectorId() != null)
                ? rutaRepository.findBySectorId(filtro.getSectorId())
                : rutaRepository.findAll();

        List<GeoJsonFeature> features = new ArrayList<>();
        for (RutaPatrullaje r : rutas) {
            if (r.getPuntosControl() == null || r.getPuntosControl().isEmpty()) continue;

            List<List<Double>> coords = new ArrayList<>();
            for (PuntoControlRuta pt : r.getPuntosControl()) {
                if (pt.getLatitud() != null && pt.getLongitud() != null) {
                    double lat = pt.getLatitud().doubleValue();
                    double lng = pt.getLongitud().doubleValue();
                    if (esPuntoValido(lat, lng)) {
                        coords.add(List.of(lng, lat));
                    }
                }
            }

            if (coords.size() >= 2) {
                Map<String, Object> props = new LinkedHashMap<>();
                props.put("tipo", "RUTA");
                props.put("id", r.getId().toString());
                props.put("codigo", r.getCodigo());
                props.put("nombre", r.getNombre());
                props.put("descripcion", r.getDescripcion());
                props.put("sectorNombre", r.getSector() != null ? r.getSector().getNombre() : "-");
                props.put("distanciaKm", r.getDistanciaEstimadaKm());
                props.put("tiempoMin", r.getTiempoEstimadoMin());
                props.put("estado", r.getEstado());
                props.put("totalPuntos", r.getPuntosControl().size());

                Map<String, Object> lineGeom = new LinkedHashMap<>();
                lineGeom.put("type", "LineString");
                lineGeom.put("coordinates", coords);

                features.add(GeoJsonFeature.builder()
                        .id("ruta-" + r.getId())
                        .properties(props)
                        .geometry(lineGeom)
                        .build());
            }
        }
        return GeoJsonFeatureCollectionResponse.builder().features(features).build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeoJsonFeatureCollectionResponse obtenerCapaPuntosControl(MapaFiltroRequest filtro, CustomUserDetails user) {
        List<PuntoControlRuta> puntos = (filtro != null && filtro.getSectorId() != null)
                ? puntoControlRepository.findByRutaSectorId(filtro.getSectorId())
                : puntoControlRepository.findAll();

        List<GeoJsonFeature> features = new ArrayList<>();
        for (PuntoControlRuta pt : puntos) {
            if (pt.getLatitud() != null && pt.getLongitud() != null) {
                double lat = pt.getLatitud().doubleValue();
                double lng = pt.getLongitud().doubleValue();
                if (!esPuntoValido(lat, lng)) continue;

                Map<String, Object> props = new LinkedHashMap<>();
                props.put("tipo", "PUNTO_CONTROL");
                props.put("id", pt.getId().toString());
                props.put("codigo", pt.getCodigo());
                props.put("nombre", pt.getNombre());
                props.put("orden", pt.getOrden());
                props.put("descripcion", pt.getDescripcion());
                props.put("referenciaUbicacion", pt.getReferenciaUbicacion());
                props.put("rutaCodigo", pt.getRuta() != null ? pt.getRuta().getCodigo() : "-");
                props.put("rutaNombre", pt.getRuta() != null ? pt.getRuta().getNombre() : "-");
                props.put("sectorNombre", pt.getRuta() != null && pt.getRuta().getSector() != null ? pt.getRuta().getSector().getNombre() : "-");

                features.add(GeoJsonFeature.builder()
                        .id("punto-" + pt.getId())
                        .properties(props)
                        .geometry(crearPuntoGeoJson(lng, lat))
                        .build());
            }
        }
        return GeoJsonFeatureCollectionResponse.builder().features(features).build();
    }

    @Override
    @Transactional(readOnly = true)
    public MapaEstadisticasResponse obtenerEstadisticas(MapaFiltroRequest filtro) {
        long totalInc = incidenciaRepository.count();
        long incGeo = incidenciaRepository.count((root, query, cb) -> cb.and(
                cb.isNotNull(root.get("latitud")),
                cb.isNotNull(root.get("longitud"))
        ));

        long totalAle = alertaRepository.count();
        long aleGeo = alertaRepository.count((root, query, cb) -> cb.and(
                cb.isNotNull(root.get("latitud")),
                cb.isNotNull(root.get("longitud"))
        ));

        long totalInt = intervencionRepository.count();
        long intGeo = intervencionRepository.count((root, query, cb) -> cb.and(
                cb.isNotNull(root.get("latitud")),
                cb.isNotNull(root.get("longitud"))
        ));

        long patActivos = patrullajeRepository.count((root, query, cb) -> root.get("estado").in(
                EstadoPatrullajeSerenazgo.EN_CURSO, EstadoPatrullajeSerenazgo.EN_PAUSA
        ));

        long totalRutas = rutaRepository.count();
        long totalPuntos = puntoControlRepository.count();

        // Agrupación por sector
        Map<String, Long> porSector = new LinkedHashMap<>();
        for (SectorPatrullaje sec : sectorRepository.findAll()) {
            long c = incidenciaRepository.count((root, query, cb) -> cb.equal(root.get("sector").get("id"), sec.getId()));
            porSector.put(sec.getNombre(), c);
        }

        // Agrupación por prioridad
        Map<String, Long> porPrioridad = new LinkedHashMap<>();
        for (pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad pri : pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad.values()) {
            long c = incidenciaRepository.countByPrioridad(pri);
            porPrioridad.put(pri.getDescripcion(), c);
        }

        // Agrupación de patrullajes por estado
        Map<String, Long> porEstadoPat = new LinkedHashMap<>();
        for (EstadoPatrullajeSerenazgo est : EstadoPatrullajeSerenazgo.values()) {
            long c = patrullajeRepository.countByEstado(est);
            if (c > 0) porEstadoPat.put(est.getDescripcion(), c);
        }

        return MapaEstadisticasResponse.builder()
                .totalIncidencias(totalInc)
                .incidenciasGeorreferenciadas(incGeo)
                .incidenciasSinCoordenadas(totalInc - incGeo)
                .totalAlertas(totalAle)
                .alertasGeorreferenciadas(aleGeo)
                .totalIntervenciones(totalInt)
                .intervencionesGeorreferenciadas(intGeo)
                .totalPatrullajesActivos(patActivos)
                .totalRutasActivas(totalRutas)
                .totalPuntosControl(totalPuntos)
                .incidenciasPorSector(porSector)
                .incidenciasPorPrioridad(porPrioridad)
                .patrullajesPorEstado(porEstadoPat)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ElementoGeograficoDetalleResponse obtenerDetalleElemento(String tipo, UUID id, CustomUserDetails user) {
        if (tipo == null || id == null) {
            throw new IllegalArgumentException("El tipo y el ID son obligatorios.");
        }

        switch (tipo.toUpperCase()) {
            case "INCIDENCIA": {
                IncidenciaSeguridadCiudadana inc = incidenciaRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Incidencia no encontrada: " + id));
                ElementoGeograficoDetalleResponse resp = ElementoGeograficoDetalleResponse.builder()
                        .tipo("INCIDENCIA")
                        .id(inc.getId().toString())
                        .codigo(inc.getCodigo())
                        .titulo(inc.getTitulo())
                        .descripcion(inc.getDescripcion())
                        .fechaHora(inc.getFechaReporte().format(FORMATO_FECHA_HORA))
                        .estado(inc.getEstado().name())
                        .estadoDescripcion(inc.getEstado().getDescripcion())
                        .prioridad(inc.getPrioridad().name())
                        .prioridadDescripcion(inc.getPrioridad().getDescripcion())
                        .sectorNombre(inc.getSector() != null ? inc.getSector().getNombre() : "-")
                        .zonaNombre(inc.getZona() != null ? inc.getZona().getNombre() : "-")
                        .ubicacionReferencia(inc.getUbicacionReferencia())
                        .latitud(inc.getLatitud() != null ? inc.getLatitud().doubleValue() : null)
                        .longitud(inc.getLongitud() != null ? inc.getLongitud().doubleValue() : null)
                        .tieneCoordenadas(inc.getLatitud() != null && inc.getLongitud() != null)
                        .urlDetalle("/dashboard/serenazgo/incidencias/" + inc.getId())
                        .build();

                resp.getAtributos().put("Tipo de Incidencia", inc.getTipoIncidencia() != null ? inc.getTipoIncidencia().getNombre() : "-");
                resp.getAtributos().put("Canal de Reporte", inc.getCanalReporte() != null ? inc.getCanalReporte().getDescripcion() : "-");
                return resp;
            }
            case "ALERTA": {
                AlertaCiudadanaSeguridad al = alertaRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Alerta no encontrada: " + id));
                ElementoGeograficoDetalleResponse resp = ElementoGeograficoDetalleResponse.builder()
                        .tipo("ALERTA")
                        .id(al.getId().toString())
                        .codigo(al.getCodigo())
                        .titulo(al.getTitulo())
                        .descripcion(al.getDescripcion())
                        .fechaHora(al.getFechaRecepcion().format(FORMATO_FECHA_HORA))
                        .estado(al.getEstado().name())
                        .estadoDescripcion(al.getEstado().getDescripcion())
                        .prioridad(al.getPrioridad().name())
                        .prioridadDescripcion(al.getPrioridad().getDescripcion())
                        .sectorNombre(al.getSector() != null ? al.getSector().getNombre() : "-")
                        .zonaNombre(al.getZona() != null ? al.getZona().getNombre() : "-")
                        .ubicacionReferencia(al.getUbicacionReferencia())
                        .latitud(al.getLatitud() != null ? al.getLatitud().doubleValue() : null)
                        .longitud(al.getLongitud() != null ? al.getLongitud().doubleValue() : null)
                        .tieneCoordenadas(al.getLatitud() != null && al.getLongitud() != null)
                        .urlDetalle("/dashboard/serenazgo/alertas/" + al.getId())
                        .build();

                resp.getAtributos().put("Canal de Alerta", al.getCanalOrigen() != null ? al.getCanalOrigen().getDescripcion() : "-");
                return resp;
            }
            case "INTERVENCION": {
                IntervencionSerenazgo it = intervencionRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Intervención no encontrada: " + id));
                ElementoGeograficoDetalleResponse resp = ElementoGeograficoDetalleResponse.builder()
                        .tipo("INTERVENCION")
                        .id(it.getId().toString())
                        .codigo(it.getCodigo())
                        .titulo("Actuación de Serenazgo " + it.getCodigo())
                        .descripcion(it.getDescripcionActuacion())
                        .fechaHora(it.getFechaInicio().format(FORMATO_FECHA_HORA))
                        .estado(it.getEstado().name())
                        .estadoDescripcion(it.getEstado().getDescripcion())
                        .sectorNombre(it.getSector() != null ? it.getSector().getNombre() : "-")
                        .zonaNombre(it.getZona() != null ? it.getZona().getNombre() : "-")
                        .ubicacionReferencia(it.getUbicacionDescripcion())
                        .latitud(it.getLatitud())
                        .longitud(it.getLongitud())
                        .tieneCoordenadas(it.getLatitud() != null && it.getLongitud() != null)
                        .urlDetalle("/dashboard/serenazgo/intervenciones/" + it.getId())
                        .build();

                resp.getAtributos().put("Tipo Intervención", it.getTipoIntervencion() != null ? it.getTipoIntervencion().getDescripcion() : "-");
                resp.getAtributos().put("Resultado Administrativo", it.getResultado() != null ? it.getResultado().getDescripcion() : "-");
                return resp;
            }
            case "PATRULLAJE": {
                PatrullajeSerenazgo p = patrullajeRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Patrullaje no encontrado: " + id));
                ElementoGeograficoDetalleResponse resp = ElementoGeograficoDetalleResponse.builder()
                        .tipo("PATRULLAJE")
                        .id(p.getId().toString())
                        .codigo(p.getCodigo())
                        .titulo("Patrullaje " + p.getTipoPatrullaje().getDescripcion())
                        .descripcion(p.getObjetivoOperativo())
                        .fechaHora(p.getFechaHoraInicioProgramada() != null ? p.getFechaHoraInicioProgramada().format(FORMATO_FECHA_HORA) : "-")
                        .estado(p.getEstado().name())
                        .estadoDescripcion(p.getEstado().getDescripcion())
                        .sectorNombre(p.getSector() != null ? p.getSector().getNombre() : "-")
                        .zonaNombre(p.getZona() != null ? p.getZona().getNombre() : "-")
                        .ubicacionReferencia(p.getRuta() != null ? p.getRuta().getNombre() : "Ruta libre")
                        .tieneCoordenadas(p.getRuta() != null)
                        .urlDetalle("/dashboard/serenazgo/patrullajes/" + p.getId())
                        .build();

                resp.getAtributos().put("Modalidad", p.getTipoPatrullaje().getDescripcion());
                resp.getAtributos().put("Responsable", p.getResponsable() != null && p.getResponsable().getPersona() != null ? p.getResponsable().getPersona().getNombreCompleto() : "-");
                resp.getAtributos().put("Unidad Móvil", p.getUnidadMovil() != null ? p.getUnidadMovil().getPlaca() : "A pie");
                if (p.getRuta() != null) {
                    resp.getAtributos().put("Ruta Asignada", p.getRuta().getCodigo() + " - " + p.getRuta().getNombre());
                }
                return resp;
            }
            case "SECTOR": {
                SectorPatrullaje s = sectorRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Sector no encontrado: " + id));
                ElementoGeograficoDetalleResponse resp = ElementoGeograficoDetalleResponse.builder()
                        .tipo("SECTOR")
                        .id(s.getId().toString())
                        .codigo(s.getCodigo())
                        .titulo(s.getNombre())
                        .descripcion(s.getDescripcion())
                        .estado(s.getEstado().name())
                        .estadoDescripcion(s.getEstado().getDescripcion())
                        .sectorNombre(s.getNombre())
                        .tieneCoordenadas(s.getTieneDelimitacion() != null && s.getTieneDelimitacion())
                        .urlDetalle("/dashboard/serenazgo/sectores/" + s.getId())
                        .build();

                resp.getAtributos().put("Color Identificador", s.getColorHex());
                resp.getAtributos().put("Total Zonas", s.getZonas() != null ? s.getZonas().size() : 0);
                return resp;
            }
            case "ZONA": {
                ZonaPatrullaje z = zonaRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Zona no encontrada: " + id));
                ElementoGeograficoDetalleResponse resp = ElementoGeograficoDetalleResponse.builder()
                        .tipo("ZONA")
                        .id(z.getId().toString())
                        .codigo(z.getCodigo())
                        .titulo(z.getNombre())
                        .descripcion(z.getDescripcion())
                        .estado(z.getEstado().name())
                        .estadoDescripcion(z.getEstado().getDescripcion())
                        .prioridad(z.getPrioridadOperativa().name())
                        .prioridadDescripcion(z.getPrioridadOperativa().getDescripcion())
                        .sectorNombre(z.getSector() != null ? z.getSector().getNombre() : "-")
                        .tieneCoordenadas(z.getTieneDelimitacion() != null && z.getTieneDelimitacion())
                        .urlDetalle("/dashboard/serenazgo/zonas/" + z.getId())
                        .build();

                return resp;
            }
            case "RUTA": {
                RutaPatrullaje r = rutaRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada: " + id));
                ElementoGeograficoDetalleResponse resp = ElementoGeograficoDetalleResponse.builder()
                        .tipo("RUTA")
                        .id(r.getId().toString())
                        .codigo(r.getCodigo())
                        .titulo(r.getNombre())
                        .descripcion(r.getDescripcion())
                        .estado(r.getEstado())
                        .sectorNombre(r.getSector() != null ? r.getSector().getNombre() : "-")
                        .tieneCoordenadas(r.getPuntosControl() != null && !r.getPuntosControl().isEmpty())
                        .urlDetalle("/dashboard/serenazgo/patrullajes")
                        .build();

                resp.getAtributos().put("Distancia Estimada", r.getDistanciaEstimadaKm() + " km");
                resp.getAtributos().put("Tiempo Estimado", r.getTiempoEstimadoMin() + " min");
                resp.getAtributos().put("Puntos de Control", r.getPuntosControl() != null ? r.getPuntosControl().size() : 0);
                return resp;
            }
            case "PUNTO_CONTROL": {
                PuntoControlRuta pt = puntoControlRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Punto de control no encontrado: " + id));
                ElementoGeograficoDetalleResponse resp = ElementoGeograficoDetalleResponse.builder()
                        .tipo("PUNTO_CONTROL")
                        .id(pt.getId().toString())
                        .codigo(pt.getCodigo())
                        .titulo(pt.getNombre())
                        .descripcion(pt.getDescripcion())
                        .sectorNombre(pt.getRuta() != null && pt.getRuta().getSector() != null ? pt.getRuta().getSector().getNombre() : "-")
                        .ubicacionReferencia(pt.getReferenciaUbicacion())
                        .latitud(pt.getLatitud() != null ? pt.getLatitud().doubleValue() : null)
                        .longitud(pt.getLongitud() != null ? pt.getLongitud().doubleValue() : null)
                        .tieneCoordenadas(pt.getLatitud() != null && pt.getLongitud() != null)
                        .urlDetalle("/dashboard/serenazgo/patrullajes")
                        .build();

                resp.getAtributos().put("Orden en Ruta", "#" + pt.getOrden());
                resp.getAtributos().put("Ruta", pt.getRuta() != null ? pt.getRuta().getCodigo() + " - " + pt.getRuta().getNombre() : "-");
                return resp;
            }
            default:
                throw new IllegalArgumentException("Tipo de elemento no reconocido: " + tipo);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemBusquedaMapaResponse> buscarElementos(String query, CustomUserDetails user) {
        if (query == null || query.trim().length() < 2) {
            return Collections.emptyList();
        }
        String term = query.trim().toLowerCase();
        List<ItemBusquedaMapaResponse> resultados = new ArrayList<>();

        // 1. Incidencias
        for (IncidenciaSeguridadCiudadana inc : incidenciaRepository.findAll()) {
            if (inc.getCodigo().toLowerCase().contains(term) || inc.getTitulo().toLowerCase().contains(term)) {
                resultados.add(ItemBusquedaMapaResponse.builder()
                        .tipo("INCIDENCIA")
                        .id(inc.getId().toString())
                        .codigo(inc.getCodigo())
                        .titulo(inc.getTitulo())
                        .subtitulo("Incidencia en " + (inc.getSector() != null ? inc.getSector().getNombre() : "Sector General"))
                        .latitud(inc.getLatitud() != null ? inc.getLatitud().doubleValue() : null)
                        .longitud(inc.getLongitud() != null ? inc.getLongitud().doubleValue() : null)
                        .tieneCoordenadas(inc.getLatitud() != null && inc.getLongitud() != null)
                        .urlDetalle("/dashboard/serenazgo/incidencias/" + inc.getId())
                        .referencia(inc.getUbicacionReferencia())
                        .build());
            }
            if (resultados.size() >= 25) break;
        }

        // 2. Patrullajes
        for (PatrullajeSerenazgo p : patrullajeRepository.findAll()) {
            if (p.getCodigo().toLowerCase().contains(term) || (p.getRuta() != null && p.getRuta().getNombre().toLowerCase().contains(term))) {
                Double lat = null;
                Double lng = null;
                if (p.getRuta() != null && p.getRuta().getPuntosControl() != null && !p.getRuta().getPuntosControl().isEmpty()) {
                    PuntoControlRuta first = p.getRuta().getPuntosControl().get(0);
                    if (first.getLatitud() != null) lat = first.getLatitud().doubleValue();
                    if (first.getLongitud() != null) lng = first.getLongitud().doubleValue();
                }
                resultados.add(ItemBusquedaMapaResponse.builder()
                        .tipo("PATRULLAJE")
                        .id(p.getId().toString())
                        .codigo(p.getCodigo())
                        .titulo("Patrullaje " + p.getCodigo())
                        .subtitulo("Estado: " + p.getEstado().getDescripcion())
                        .latitud(lat)
                        .longitud(lng)
                        .tieneCoordenadas(lat != null && lng != null)
                        .urlDetalle("/dashboard/serenazgo/patrullajes/" + p.getId())
                        .referencia(p.getRuta() != null ? p.getRuta().getNombre() : "-")
                        .build());
            }
            if (resultados.size() >= 35) break;
        }

        // 3. Rutas
        for (RutaPatrullaje r : rutaRepository.findAll()) {
            if (r.getCodigo().toLowerCase().contains(term) || r.getNombre().toLowerCase().contains(term)) {
                Double lat = null;
                Double lng = null;
                if (r.getPuntosControl() != null && !r.getPuntosControl().isEmpty()) {
                    PuntoControlRuta first = r.getPuntosControl().get(0);
                    if (first.getLatitud() != null) lat = first.getLatitud().doubleValue();
                    if (first.getLongitud() != null) lng = first.getLongitud().doubleValue();
                }
                resultados.add(ItemBusquedaMapaResponse.builder()
                        .tipo("RUTA")
                        .id(r.getId().toString())
                        .codigo(r.getCodigo())
                        .titulo(r.getNombre())
                        .subtitulo("Ruta de Patrullaje Oficial")
                        .latitud(lat)
                        .longitud(lng)
                        .tieneCoordenadas(lat != null && lng != null)
                        .urlDetalle("/dashboard/serenazgo/patrullajes")
                        .referencia(r.getDescripcion())
                        .build());
            }
        }

        // 4. Sectores
        for (SectorPatrullaje s : sectorRepository.findAll()) {
            if (s.getCodigo().toLowerCase().contains(term) || s.getNombre().toLowerCase().contains(term)) {
                resultados.add(ItemBusquedaMapaResponse.builder()
                        .tipo("SECTOR")
                        .id(s.getId().toString())
                        .codigo(s.getCodigo())
                        .titulo(s.getNombre())
                        .subtitulo("Sector Territorial de Quinua")
                        .tieneCoordenadas(s.getTieneDelimitacion() != null && s.getTieneDelimitacion())
                        .urlDetalle("/dashboard/serenazgo/sectores/" + s.getId())
                        .referencia(s.getDescripcion())
                        .build());
            }
        }

        return resultados;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarExcel(MapaFiltroRequest filtro, CustomUserDetails user) {
        try (Workbook wb = new XSSFWorkbook()) {
            // Estilos
            org.apache.poi.ss.usermodel.Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Hoja 1: Incidencias
            Sheet sheetInc = wb.createSheet("Incidencias Geocodificadas");
            String[] colsInc = {"CÓDIGO", "TÍTULO", "TIPO", "PRIORIDAD", "ESTADO", "FECHA/HORA", "SECTOR", "LATITUD", "LONGITUD", "REFERENCIA"};
            Row r0 = sheetInc.createRow(0);
            for (int i = 0; i < colsInc.length; i++) {
                Cell c = r0.createCell(i);
                c.setCellValue(colsInc[i]);
                c.setCellStyle(headerStyle);
            }

            GeoJsonFeatureCollectionResponse incFeatures = obtenerCapaIncidencias(filtro, user);
            int rowIdx = 1;
            for (GeoJsonFeature f : incFeatures.getFeatures()) {
                Row r = sheetInc.createRow(rowIdx++);
                Map<String, Object> p = f.getProperties();
                r.createCell(0).setCellValue(String.valueOf(p.getOrDefault("codigo", "-")));
                r.createCell(1).setCellValue(String.valueOf(p.getOrDefault("titulo", "-")));
                r.createCell(2).setCellValue(String.valueOf(p.getOrDefault("tipoIncidencia", "-")));
                r.createCell(3).setCellValue(String.valueOf(p.getOrDefault("prioridadDesc", "-")));
                r.createCell(4).setCellValue(String.valueOf(p.getOrDefault("estadoDesc", "-")));
                r.createCell(5).setCellValue(String.valueOf(p.getOrDefault("fechaReporte", "-")));
                r.createCell(6).setCellValue(String.valueOf(p.getOrDefault("sectorNombre", "-")));

                if (f.getGeometry() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> geom = (Map<String, Object>) f.getGeometry();
                    @SuppressWarnings("unchecked")
                    List<Double> coords = (List<Double>) geom.get("coordinates");
                    if (coords != null && coords.size() >= 2) {
                        r.createCell(7).setCellValue(coords.get(1));
                        r.createCell(8).setCellValue(coords.get(0));
                    }
                }
                r.createCell(9).setCellValue(String.valueOf(p.getOrDefault("ubicacionReferencia", "-")));
            }
            for (int i = 0; i < colsInc.length; i++) sheetInc.autoSizeColumn(i);

            // Hoja 2: Rutas y Checkpoints
            Sheet sheetRutas = wb.createSheet("Rutas y Puntos de Control");
            String[] colsRuta = {"RUTA CÓDIGO", "RUTA NOMBRE", "SECTOR", "ORDEN", "CHECKPOINT", "LATITUD", "LONGITUD", "REFERENCIA UBICACIÓN"};
            Row rRuta0 = sheetRutas.createRow(0);
            for (int i = 0; i < colsRuta.length; i++) {
                Cell c = rRuta0.createCell(i);
                c.setCellValue(colsRuta[i]);
                c.setCellStyle(headerStyle);
            }

            int rIdx2 = 1;
            for (RutaPatrullaje r : rutaRepository.findAll()) {
                if (r.getPuntosControl() != null) {
                    for (PuntoControlRuta pt : r.getPuntosControl()) {
                        Row rw = sheetRutas.createRow(rIdx2++);
                        rw.createCell(0).setCellValue(r.getCodigo());
                        rw.createCell(1).setCellValue(r.getNombre());
                        rw.createCell(2).setCellValue(r.getSector() != null ? r.getSector().getNombre() : "-");
                        rw.createCell(3).setCellValue(pt.getOrden());
                        rw.createCell(4).setCellValue(pt.getCodigo() + " - " + pt.getNombre());
                        rw.createCell(5).setCellValue(pt.getLatitud() != null ? pt.getLatitud().doubleValue() : 0.0);
                        rw.createCell(6).setCellValue(pt.getLongitud() != null ? pt.getLongitud().doubleValue() : 0.0);
                        rw.createCell(7).setCellValue(pt.getReferenciaUbicacion() != null ? pt.getReferenciaUbicacion() : "-");
                    }
                }
            }
            for (int i = 0; i < colsRuta.length; i++) sheetRutas.autoSizeColumn(i);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar cartografía a Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar reporte Excel cartográfico: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarPdf(MapaFiltroRequest filtro, CustomUserDetails user) {
        Document doc = new Document(PageSize.A4, 36, 36, 40, 40);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // Fuentes
            Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, Color.BLACK);
            Font fSub = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(50, 70, 90));
            Font fNorm = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            Font fLegal = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, new Color(100, 100, 100));

            // Encabezado Municipal
            Paragraph pHeader = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA\nSUBGERENCIA DE SEGURIDAD CIUDADANA Y SERENAZGO", fSub);
            pHeader.setAlignment(Element.ALIGN_CENTER);
            doc.add(pHeader);

            Paragraph pTit = new Paragraph("REPORTE CARTOGRÁFICO Y GEORREFERENCIACIÓN OPERATIVA", fTitulo);
            pTit.setAlignment(Element.ALIGN_CENTER);
            pTit.setSpacingBefore(8);
            pTit.setSpacingAfter(12);
            doc.add(pTit);

            // Descargo Legal
            Paragraph pLegal = new Paragraph("AVISO LEGAL OBLIGATORIO (Ley N° 31297 / Ley N° 27933 SINASEC): La información cartográfica y geográfica registrada en este informe corresponde estrictamente a actuaciones preventivas, disuasivas y de auxilio ciudadano de serenazgo municipal dentro de la circunscripción distrital de Quinua. No constituye mapa de investigación criminalística ni prejuzgamiento policial o penal.", fLegal);
            pLegal.setSpacingAfter(12);
            doc.add(pLegal);

            // Resumen Estadístico
            MapaEstadisticasResponse stats = obtenerEstadisticas(filtro);
            PdfPTable tStats = new PdfPTable(4);
            tStats.setWidthPercentage(100);
            agregarCeldaHeader(tStats, "INCIDENCIAS GEO");
            agregarCeldaHeader(tStats, "ALERTAS GEO");
            agregarCeldaHeader(tStats, "INTERVENCIONES GEO");
            agregarCeldaHeader(tStats, "PATRULLAJES ACTIVOS");

            agregarCeldaDato(tStats, String.valueOf(stats.getIncidenciasGeorreferenciadas()));
            agregarCeldaDato(tStats, String.valueOf(stats.getAlertasGeorreferenciadas()));
            agregarCeldaDato(tStats, String.valueOf(stats.getIntervencionesGeorreferenciadas()));
            agregarCeldaDato(tStats, String.valueOf(stats.getTotalPatrullajesActivos()));
            tStats.setSpacingAfter(14);
            doc.add(tStats);

            // Tabla de Sectores Oficiales
            Paragraph pSec = new Paragraph("SECTORES TERRITORIALES Y COBERTURA GEOGRÁFICA:", fSub);
            pSec.setSpacingAfter(6);
            doc.add(pSec);

            PdfPTable tSect = new PdfPTable(4);
            tSect.setWidthPercentage(100);
            tSect.setWidths(new float[]{1.5f, 3.5f, 1.5f, 1.5f});
            agregarCeldaHeader(tSect, "CÓDIGO");
            agregarCeldaHeader(tSect, "NOMBRE DEL SECTOR");
            agregarCeldaHeader(tSect, "ESTADO");
            agregarCeldaHeader(tSect, "DELIMITACIÓN");

            for (SectorPatrullaje s : sectorRepository.findAll()) {
                agregarCeldaDato(tSect, s.getCodigo());
                agregarCeldaDato(tSect, s.getNombre());
                agregarCeldaDato(tSect, s.getEstado().getDescripcion());
                agregarCeldaDato(tSect, Boolean.TRUE.equals(s.getTieneDelimitacion()) ? "Conformada" : "Sin Delimitación");
            }
            tSect.setSpacingAfter(14);
            doc.add(tSect);

            // Tabla de Incidencias Geocodificadas (Top 20)
            Paragraph pInc = new Paragraph("MUESTRA DE INCIDENCIAS CON COORDENADAS VÁLIDAS:", fSub);
            pInc.setSpacingAfter(6);
            doc.add(pInc);

            PdfPTable tInc = new PdfPTable(5);
            tInc.setWidthPercentage(100);
            tInc.setWidths(new float[]{1.5f, 3.0f, 1.5f, 1.5f, 2.5f});
            agregarCeldaHeader(tInc, "CÓDIGO");
            agregarCeldaHeader(tInc, "TÍTULO / TIPO");
            agregarCeldaHeader(tInc, "PRIORIDAD");
            agregarCeldaHeader(tInc, "FECHA");
            agregarCeldaHeader(tInc, "COORDENADAS WGS84");

            GeoJsonFeatureCollectionResponse incs = obtenerCapaIncidencias(filtro, user);
            int count = 0;
            for (GeoJsonFeature f : incs.getFeatures()) {
                if (++count > 25) break;
                Map<String, Object> p = f.getProperties();
                agregarCeldaDato(tInc, String.valueOf(p.getOrDefault("codigo", "-")));
                agregarCeldaDato(tInc, String.valueOf(p.getOrDefault("titulo", "-")));
                agregarCeldaDato(tInc, String.valueOf(p.getOrDefault("prioridadDesc", "-")));
                agregarCeldaDato(tInc, String.valueOf(p.getOrDefault("fechaReporte", "-")));

                String coordsStr = "-";
                if (f.getGeometry() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> gm = (Map<String, Object>) f.getGeometry();
                    @SuppressWarnings("unchecked")
                    List<Double> c = (List<Double>) gm.get("coordinates");
                    if (c != null && c.size() >= 2) {
                        coordsStr = String.format("%.5f, %.5f", c.get(1), c.get(0));
                    }
                }
                agregarCeldaDato(tInc, coordsStr);
            }
            doc.add(tInc);

            // Pie de página
            Paragraph pFooter = new Paragraph("\nGenerado por el sistema SIGM-QUINUA el " +
                    OffsetDateTime.now(ZONA_LIMA).format(FORMATO_FECHA_HORA) + " por el usuario " +
                    (user != null ? user.getUsername() : "ADMIN"), fLegal);
            pFooter.setAlignment(Element.ALIGN_RIGHT);
            doc.add(pFooter);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar PDF cartográfico: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar reporte PDF cartográfico: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String exportarGeoJson(String capa, MapaFiltroRequest filtro, CustomUserDetails user) {
        GeoJsonFeatureCollectionResponse fc;
        if (capa == null || capa.isBlank()) capa = "TODAS";

        switch (capa.toUpperCase()) {
            case "SECTORES":
                fc = obtenerCapaSectores(filtro);
                break;
            case "ZONAS":
                fc = obtenerCapaZonas(filtro);
                break;
            case "INCIDENCIAS":
                fc = obtenerCapaIncidencias(filtro, user);
                break;
            case "ALERTAS":
                fc = obtenerCapaAlertas(filtro, user);
                break;
            case "INTERVENCIONES":
                fc = obtenerCapaIntervenciones(filtro, user);
                break;
            case "PATRULLAJES":
                fc = obtenerCapaPatrullajes(filtro, user);
                break;
            case "RUTAS":
                fc = obtenerCapaRutas(filtro, user);
                break;
            case "PUNTOS_CONTROL":
                fc = obtenerCapaPuntosControl(filtro, user);
                break;
            default: {
                // Combinar capas principales
                List<GeoJsonFeature> combinadas = new ArrayList<>();
                combinadas.addAll(obtenerCapaSectores(filtro).getFeatures());
                combinadas.addAll(obtenerCapaZonas(filtro).getFeatures());
                combinadas.addAll(obtenerCapaRutas(filtro, user).getFeatures());
                combinadas.addAll(obtenerCapaPuntosControl(filtro, user).getFeatures());
                combinadas.addAll(obtenerCapaIncidencias(filtro, user).getFeatures());
                fc = GeoJsonFeatureCollectionResponse.builder().features(combinadas).build();
                break;
            }
        }

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(fc);
        } catch (Exception e) {
            log.error("Error al serializar GeoJSON: {}", e.getMessage(), e);
            return "{\"type\":\"FeatureCollection\",\"features\":[]}";
        }
    }

    // --- Métodos de Utilidad Cartográfica y Helpers Privados ---

    private boolean tienePermiso(CustomUserDetails user, String permiso) {
        if (user == null) return false;
        return user.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") ||
                a.getAuthority().equals("ROLE_SUPERADMIN") ||
                a.getAuthority().equals(permiso)
        );
    }

    private boolean esPuntoValido(double lat, double lng) {
        return Double.isFinite(lat) && Double.isFinite(lng) &&
                lat >= -90.0 && lat <= 90.0 &&
                lng >= -180.0 && lng <= 180.0;
    }

    private boolean sonCoordenadasValidas(Double minLat, Double maxLat, Double minLng, Double maxLng) {
        return minLat != null && maxLat != null && minLng != null && maxLng != null &&
                Double.isFinite(minLat) && Double.isFinite(maxLat) &&
                Double.isFinite(minLng) && Double.isFinite(maxLng) &&
                minLat >= -90.0 && maxLat <= 90.0 && minLat <= maxLat &&
                minLng >= -180.0 && maxLng <= 180.0 && minLng <= maxLng;
    }

    private Map<String, Object> crearPuntoGeoJson(double lng, double lat) {
        Map<String, Object> geom = new LinkedHashMap<>();
        geom.put("type", "Point");
        geom.put("coordinates", List.of(lng, lat));
        return geom;
    }

    private JsonNode parsearGeoJson(String geoJsonStr) {
        try {
            JsonNode root = objectMapper.readTree(geoJsonStr);
            if (root.has("geometry")) {
                return root.get("geometry");
            }
            return root;
        } catch (Exception e) {
            log.debug("No se pudo parsear texto a JsonNode: {}", e.getMessage());
            return null;
        }
    }

    private void agregarCeldaHeader(PdfPTable table, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
        cell.setBackgroundColor(new Color(30, 41, 59));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void agregarCeldaDato(PdfPTable table, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto != null ? texto : "-", FontFactory.getFont(FontFactory.HELVETICA, 7, Color.BLACK)));
        cell.setPadding(4);
        table.addCell(cell);
    }
}
