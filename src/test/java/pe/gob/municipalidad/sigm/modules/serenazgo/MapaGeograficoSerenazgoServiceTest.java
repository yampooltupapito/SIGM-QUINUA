package pe.gob.municipalidad.sigm.modules.serenazgo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.GeoJsonFeatureCollectionResponse;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.mapa.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.MapaGeograficoSerenazgoService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MapaGeograficoSerenazgoServiceTest {

    @Autowired
    private MapaGeograficoSerenazgoService mapaService;

    @Autowired
    private SectorPatrullajeRepository sectorRepository;

    @Autowired
    private ZonaPatrullajeRepository zonaRepository;

    @Autowired
    private RutaPatrullajeRepository rutaRepository;

    @Autowired
    private PuntoControlRutaRepository puntoControlRepository;

    @Autowired
    private IncidenciaSeguridadCiudadanaRepository incidenciaRepository;

    @Autowired
    private AlertaCiudadanaSeguridadRepository alertaRepository;

    @Autowired
    private IntervencionSerenazgoRepository intervencionRepository;

    @Autowired
    private PatrullajeSerenazgoRepository patrullajeRepository;

    @Autowired
    private TipoIncidenciaSeguridadRepository tipoIncidenciaRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private CustomUserDetails userAdmin;
    private SectorPatrullaje sectorUrbano;
    private IncidenciaSeguridadCiudadana incTest;
    private RutaPatrullaje rutaTest;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        // 1. Usuario Admin
        userAdmin = CustomUserDetails.builder()
                .id(999L)
                .username("admin_mapa_" + ts)
                .password("dummy")
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("SERENAZGO_MAPA_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_MAPA_VER_INCIDENCIAS"),
                        new SimpleGrantedAuthority("SERENAZGO_MAPA_VER_ALERTAS"),
                        new SimpleGrantedAuthority("SERENAZGO_MAPA_VER_INTERVENCIONES"),
                        new SimpleGrantedAuthority("SERENAZGO_MAPA_VER_PATRULLAJES"),
                        new SimpleGrantedAuthority("SERENAZGO_MAPA_EXPORTAR")
                ))
                .build();

        // 2. Sector y Delimitación Poligonal
        sectorUrbano = sectorRepository.findByCodigo("SEC-01")
                .orElseGet(() -> sectorRepository.save(SectorPatrullaje.builder()
                        .codigo("SEC-01")
                        .nombre("Sector 01 - Casco Urbano y Plaza Mayor")
                        .descripcion("Área central del distrito de Quinua")
                        .colorHex("#3b82f6")
                        .estado(EstadoSectorPatrullaje.ACTIVO)
                        .tieneDelimitacion(true)
                        .geometriaGeojson("{\"type\":\"Polygon\",\"coordinates\":[[[-74.1435,-13.0395],[-74.1345,-13.0395],[-74.1345,-13.0465],[-74.1435,-13.0465],[-74.1435,-13.0395]]]}")
                        .build()));

        // Asegurar que tiene delimitación para el test
        if (!Boolean.TRUE.equals(sectorUrbano.getTieneDelimitacion()) || sectorUrbano.getGeometriaGeojson() == null) {
            sectorUrbano.setTieneDelimitacion(true);
            sectorUrbano.setGeometriaGeojson("{\"type\":\"Polygon\",\"coordinates\":[[[-74.1435,-13.0395],[-74.1345,-13.0395],[-74.1345,-13.0465],[-74.1435,-13.0465],[-74.1435,-13.0395]]]}");
            sectorUrbano = sectorRepository.save(sectorUrbano);
        }

        // 3. Incidencia Georreferenciada en Quinua
        TipoIncidenciaSeguridad tipoInc = tipoIncidenciaRepository.findAll().stream().findFirst()
                .orElseGet(() -> tipoIncidenciaRepository.save(TipoIncidenciaSeguridad.builder()
                        .codigo("TIP-TEST-" + ts)
                        .nombre("Ruido Molesto")
                        .categoria(CategoriaIncidenciaSeguridad.ORDEN_PUBLICO)
                        .prioridadSugerida(PrioridadIncidenciaSeguridad.MEDIA)
                        .activo(true)
                        .build()));

        Persona pUser = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("47" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Operador").apellidoPaterno("GIS").estado("ACTIVO").build());
        Usuario uReg = usuarioRepository.save(Usuario.builder()
                .username("operador_gis_" + ts).persona(pUser).passwordHash("xyz").enabled(true).accountLocked(false).build());

        incTest = incidenciaRepository.save(IncidenciaSeguridadCiudadana.builder()
                .codigo("INC-MAP-" + ts)
                .titulo("Alteración del orden en Jr. Sucre")
                .descripcion("Discusión en vía pública sin violencia")
                .tipoIncidencia(tipoInc)
                .sector(sectorUrbano)
                .prioridad(PrioridadIncidenciaSeguridad.MEDIA)
                .estado(EstadoIncidenciaSeguridad.REGISTRADA)
                .latitud(BigDecimal.valueOf(-13.0425000))
                .longitud(BigDecimal.valueOf(-74.1388000))
                .ubicacionReferencia("Frente a Plaza Mayor de Quinua")
                .fechaReporte(OffsetDateTime.now())
                .canalReporte(CanalReporteIncidencia.LLAMADA_OPERADOR)
                .usuarioRegistro(uReg)
                .build());

        // 4. Ruta con Puntos de Control
        rutaTest = rutaRepository.findByCodigo("RUT-QUI-01")
                .orElseGet(() -> rutaRepository.save(RutaPatrullaje.builder()
                        .codigo("RUT-QUI-01")
                        .nombre("Ruta 01 - Perímetro Cívico Plaza Mayor")
                        .sector(sectorUrbano)
                        .distanciaEstimadaKm(BigDecimal.valueOf(2.5))
                        .tiempoEstimadoMin(45)
                        .estado("ACTIVA")
                        .build()));

        if (puntoControlRepository.findByRutaId(rutaTest.getId()).isEmpty()) {
            puntoControlRepository.save(PuntoControlRuta.builder()
                    .ruta(rutaTest)
                    .orden(1)
                    .codigo("PC-01-01")
                    .nombre("Caseta de Seguridad Plaza Mayor")
                    .referenciaUbicacion("Frontis Municipalidad Distrital")
                    .latitud(BigDecimal.valueOf(-13.0425000))
                    .longitud(BigDecimal.valueOf(-74.1388000))
                    .activo(true)
                    .build());
            puntoControlRepository.save(PuntoControlRuta.builder()
                    .ruta(rutaTest)
                    .orden(2)
                    .codigo("PC-01-02")
                    .nombre("Centro de Salud Quinua")
                    .referenciaUbicacion("Jr. Arequipa s/n")
                    .latitud(BigDecimal.valueOf(-13.0435000))
                    .longitud(BigDecimal.valueOf(-74.1378000))
                    .activo(true)
                    .build());
        }
    }

    @Test
    @Order(1)
    @DisplayName("CP-01: Obtener capa GeoJSON de sectores de patrullaje")
    void testObtenerCapaSectores() {
        GeoJsonFeatureCollectionResponse resp = mapaService.obtenerCapaSectores(MapaFiltroRequest.builder().build());

        assertThat(resp).isNotNull();
        assertThat(resp.getType()).isEqualTo("FeatureCollection");
        assertThat(resp.getFeatures()).isNotEmpty();

        GeoJsonFeatureCollectionResponse.GeoJsonFeature sectorFeature = resp.getFeatures().stream()
                .filter(f -> "SEC-01".equals(f.getProperties().get("codigo")))
                .findFirst().orElse(null);

        assertThat(sectorFeature).isNotNull();
        assertThat(sectorFeature.getProperties().get("nombre")).toString().contains("Sector 01");
        assertThat(sectorFeature.getGeometry()).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("CP-02: Obtener capa GeoJSON de incidencias georreferenciadas y validar privacidad")
    void testObtenerCapaIncidenciasYPrivacidad() {
        MapaFiltroRequest filtro = MapaFiltroRequest.builder()
                .sectorId(sectorUrbano.getId())
                .build();

        GeoJsonFeatureCollectionResponse resp = mapaService.obtenerCapaIncidencias(filtro, userAdmin);

        assertThat(resp).isNotNull();
        assertThat(resp.getFeatures()).isNotEmpty();

        GeoJsonFeatureCollectionResponse.GeoJsonFeature incFeature = resp.getFeatures().stream()
                .filter(f -> incTest.getCodigo().equals(f.getProperties().get("codigo")))
                .findFirst().orElse(null);

        assertThat(incFeature).isNotNull();
        assertThat(incFeature.getProperties().get("titulo")).isEqualTo(incTest.getTitulo());
        assertThat(incFeature.getProperties().get("prioridad")).isEqualTo("MEDIA");

        // Regla estricta de privacidad: No se exponen datos personales de víctimas ni reportantes
        assertThat(incFeature.getProperties().containsKey("reportanteNombre")).isFalse();
        assertThat(incFeature.getProperties().containsKey("reportanteDni")).isFalse();
        assertThat(incFeature.getProperties().containsKey("reportanteTelefono")).isFalse();

        // Validar coordenadas WGS84: [longitud, latitud]
        @SuppressWarnings("unchecked")
        Map<String, Object> geom = (Map<String, Object>) incFeature.getGeometry();
        assertThat(geom.get("type")).isEqualTo("Point");
        @SuppressWarnings("unchecked")
        List<Double> coords = (List<Double>) geom.get("coordinates");
        assertThat(coords.get(0)).isEqualTo(-74.1388000); // Longitud
        assertThat(coords.get(1)).isEqualTo(-13.0425000); // Latitud
    }

    @Test
    @Order(3)
    @DisplayName("CP-03: Filtrar incidencias por Bounding Box válido")
    void testFiltrarPorBoundingBox() {
        // Bounding box que abarca la Plaza Mayor de Quinua
        MapaFiltroRequest filtroBbox = MapaFiltroRequest.builder()
                .minLat(-13.0500)
                .maxLat(-13.0400)
                .minLng(-74.1450)
                .maxLng(-74.1350)
                .build();

        GeoJsonFeatureCollectionResponse resp = mapaService.obtenerCapaIncidencias(filtroBbox, userAdmin);
        assertThat(resp).isNotNull();
        assertThat(resp.getFeatures()).anyMatch(f -> incTest.getCodigo().equals(f.getProperties().get("codigo")));

        // Bounding box fuera de Quinua (ej. Lima: -12.0, -77.0)
        MapaFiltroRequest filtroBboxFuera = MapaFiltroRequest.builder()
                .minLat(-12.1000)
                .maxLat(-12.0000)
                .minLng(-77.1000)
                .maxLng(-77.0000)
                .build();

        GeoJsonFeatureCollectionResponse respFuera = mapaService.obtenerCapaIncidencias(filtroBboxFuera, userAdmin);
        assertThat(respFuera.getFeatures()).isEmpty();
    }

    @Test
    @Order(4)
    @DisplayName("CP-04: Obtener capa GeoJSON de rutas de patrullaje")
    void testObtenerCapaRutas() {
        GeoJsonFeatureCollectionResponse resp = mapaService.obtenerCapaRutas(MapaFiltroRequest.builder().build(), userAdmin);

        assertThat(resp).isNotNull();
        assertThat(resp.getFeatures()).isNotEmpty();

        GeoJsonFeatureCollectionResponse.GeoJsonFeature rutaFeature = resp.getFeatures().stream()
                .filter(f -> "RUT-QUI-01".equals(f.getProperties().get("codigo")))
                .findFirst().orElse(null);

        assertThat(rutaFeature).isNotNull();
        @SuppressWarnings("unchecked")
        Map<String, Object> geom = (Map<String, Object>) rutaFeature.getGeometry();
        assertThat(geom.get("type")).isEqualTo("LineString");
    }

    @Test
    @Order(5)
    @DisplayName("CP-05: Obtener capa GeoJSON de puntos de control de rutas")
    void testObtenerCapaPuntosControl() {
        GeoJsonFeatureCollectionResponse resp = mapaService.obtenerCapaPuntosControl(MapaFiltroRequest.builder().build(), userAdmin);

        assertThat(resp).isNotNull();
        assertThat(resp.getFeatures()).isNotEmpty();

        GeoJsonFeatureCollectionResponse.GeoJsonFeature ptFeature = resp.getFeatures().stream()
                .filter(f -> "PC-01-01".equals(f.getProperties().get("codigo")))
                .findFirst().orElse(null);

        assertThat(ptFeature).isNotNull();
        assertThat(ptFeature.getProperties().get("nombre")).isEqualTo("Caseta de Seguridad Plaza Mayor");
    }

    @Test
    @Order(6)
    @DisplayName("CP-06: Calcular estadísticas geográficas del mapa")
    void testObtenerEstadisticasGeograficas() {
        MapaEstadisticasResponse stats = mapaService.obtenerEstadisticas(MapaFiltroRequest.builder().build());

        assertThat(stats).isNotNull();
        assertThat(stats.getTotalIncidencias()).isGreaterThanOrEqualTo(1);
        assertThat(stats.getIncidenciasGeorreferenciadas()).isGreaterThanOrEqualTo(1);
        assertThat(stats.getTotalRutasActivas()).isGreaterThanOrEqualTo(1);
        assertThat(stats.getTotalPuntosControl()).isGreaterThanOrEqualTo(2);
        assertThat(stats.getIncidenciasPorSector()).isNotEmpty();
    }

    @Test
    @Order(7)
    @DisplayName("CP-07: Obtener detalle autorizado de un elemento del mapa")
    void testObtenerDetalleElemento() {
        ElementoGeograficoDetalleResponse det = mapaService.obtenerDetalleElemento("INCIDENCIA", incTest.getId(), userAdmin);

        assertThat(det).isNotNull();
        assertThat(det.getCodigo()).isEqualTo(incTest.getCodigo());
        assertThat(det.getTitulo()).isEqualTo(incTest.getTitulo());
        assertThat(det.isTieneCoordenadas()).isTrue();
        assertThat(det.getLatitud()).isEqualTo(-13.0425000);
        assertThat(det.getLongitud()).isEqualTo(-74.1388000);
        assertThat(det.getUrlDetalle()).contains(incTest.getId().toString());
    }

    @Test
    @Order(8)
    @DisplayName("CP-08: Búsqueda rápida geográfica por término libre")
    void testBuscarElementos() {
        List<ItemBusquedaMapaResponse> res = mapaService.buscarElementos(incTest.getCodigo(), userAdmin);

        assertThat(res).isNotEmpty();
        ItemBusquedaMapaResponse item = res.get(0);
        assertThat(item.getCodigo()).isEqualTo(incTest.getCodigo());
        assertThat(item.getLatitud()).isNotNull();
        assertThat(item.getLongitud()).isNotNull();
    }

    @Test
    @Order(9)
    @DisplayName("CP-09: Exportar cartografía a Excel")
    void testExportarExcel() {
        byte[] excelBytes = mapaService.exportarExcel(MapaFiltroRequest.builder().build(), userAdmin);

        assertThat(excelBytes).isNotNull();
        assertThat(excelBytes.length).isGreaterThan(100);
    }

    @Test
    @Order(10)
    @DisplayName("CP-10: Exportar reporte cartográfico a PDF")
    void testExportarPdf() {
        byte[] pdfBytes = mapaService.exportarPdf(MapaFiltroRequest.builder().build(), userAdmin);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(100);
    }

    @Test
    @Order(11)
    @DisplayName("CP-11: Exportar capas en formato GeoJSON FeatureCollection")
    void testExportarGeoJson() {
        String geoJsonStr = mapaService.exportarGeoJson("SECTORES", MapaFiltroRequest.builder().build(), userAdmin);

        assertThat(geoJsonStr).isNotNull();
        assertThat(geoJsonStr).contains("FeatureCollection");
        assertThat(geoJsonStr).contains("SEC-01");
    }
}
