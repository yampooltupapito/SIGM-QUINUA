package pe.gob.municipalidad.sigm.modules.serenazgo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadOperativaZona;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.SectorPatrullajeService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.ZonaPatrullajeService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ZonaPatrullajeServiceTest {

    @Autowired
    private SectorPatrullajeService sectorService;

    @Autowired
    private ZonaPatrullajeService zonaService;

    private static final String GEOJSON_ZONA_VALIDO = "{\"type\":\"Polygon\",\"coordinates\":[[[-74.139,-13.041],[-74.136,-13.041],[-74.136,-13.044],[-74.139,-13.044],[-74.139,-13.041]]]}";

    @Test
    @DisplayName("01: Creación exitosa de zona asociada a sector activo")
    void test01_crearZonaExitosa() {
        long ts = System.currentTimeMillis();
        SectorResponse sector = sectorService.crearSector(SectorCreateRequest.builder()
                .nombre("Sector Base " + ts)
                .estado(EstadoSectorPatrullaje.ACTIVO)
                .build(), "admin");

        ZonaPatrullajeResponse zona = zonaService.crearZona(ZonaCreateRequest.builder()
                .sectorId(sector.getId())
                .nombre("Zona Parque " + ts)
                .prioridadOperativa(PrioridadOperativaZona.ALTA)
                .build(), "admin");

        assertThat(zona).isNotNull();
        assertThat(zona.getId()).isNotNull();
        assertThat(zona.getCodigo()).startsWith("ZON-");
        assertThat(zona.getSectorId()).isEqualTo(sector.getId());
        assertThat(zona.getPrioridadOperativa()).isEqualTo(PrioridadOperativaZona.ALTA);
    }

    @Test
    @DisplayName("02: Rechazo de creación de zona activa en sector inactivo")
    void test02_rechazarZonaActivaEnSectorInactivo() {
        long ts = System.currentTimeMillis();
        SectorResponse sector = sectorService.crearSector(SectorCreateRequest.builder()
                .nombre("Sector Inactivo " + ts)
                .estado(EstadoSectorPatrullaje.INACTIVO)
                .build(), "admin");

        assertThatThrownBy(() -> zonaService.crearZona(ZonaCreateRequest.builder()
                .sectorId(sector.getId())
                .nombre("Zona Ilegal " + ts)
                .estado(EstadoSectorPatrullaje.ACTIVO)
                .build(), "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se puede registrar una zona activa en un sector que se encuentra");
    }

    @Test
    @DisplayName("03: Rechazo de zona con nombre duplicado dentro del mismo sector")
    void test03_rechazarNombreDuplicadoEnMismoSector() {
        long ts = System.currentTimeMillis();
        SectorResponse sector = sectorService.crearSector(SectorCreateRequest.builder()
                .nombre("Sector Alfa " + ts)
                .build(), "admin");

        zonaService.crearZona(ZonaCreateRequest.builder()
                .sectorId(sector.getId())
                .nombre("Zona Repetida")
                .build(), "admin");

        assertThatThrownBy(() -> zonaService.crearZona(ZonaCreateRequest.builder()
                .sectorId(sector.getId())
                .nombre("Zona Repetida")
                .build(), "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe una zona con el nombre");
    }

    @Test
    @DisplayName("04: Reasignación exitosa de zona a nuevo sector con registro histórico")
    void test04_reasignarZonaExitoso() {
        long ts = System.currentTimeMillis();
        SectorResponse s1 = sectorService.crearSector(SectorCreateRequest.builder()
                .nombre("Sector Origen " + ts)
                .estado(EstadoSectorPatrullaje.ACTIVO)
                .build(), "admin");

        SectorResponse s2 = sectorService.crearSector(SectorCreateRequest.builder()
                .nombre("Sector Destino " + ts)
                .estado(EstadoSectorPatrullaje.ACTIVO)
                .build(), "admin");

        ZonaPatrullajeResponse zona = zonaService.crearZona(ZonaCreateRequest.builder()
                .sectorId(s1.getId())
                .nombre("Zona Movil " + ts)
                .build(), "admin");

        assertThat(zona.getSectorId()).isEqualTo(s1.getId());

        ZonaPatrullajeResponse reasig = zonaService.reasignarSector(zona.getId(), ReasignarZonaRequest.builder()
                .nuevoSectorId(s2.getId())
                .motivo("Optimización de rutas de patrullaje")
                .documentoReferencia("Informe N° 045-2026")
                .build(), "admin");

        assertThat(reasig.getSectorId()).isEqualTo(s2.getId());

        ZonaDetailResponse detalle = zonaService.obtenerDetallePorId(zona.getId());
        assertThat(detalle.getHistorialReasignaciones()).isNotEmpty();
        assertThat(detalle.getHistorialReasignaciones().get(0).getSectorAnteriorId()).isEqualTo(s1.getId());
        assertThat(detalle.getHistorialReasignaciones().get(0).getSectorNuevoId()).isEqualTo(s2.getId());
    }

    @Test
    @DisplayName("05: Guardar delimitación GeoJSON en zona")
    void test05_guardarDelimitacionZona() {
        long ts = System.currentTimeMillis();
        SectorResponse sector = sectorService.crearSector(SectorCreateRequest.builder()
                .nombre("Sector Zona Geo " + ts)
                .build(), "admin");

        ZonaPatrullajeResponse zona = zonaService.crearZona(ZonaCreateRequest.builder()
                .sectorId(sector.getId())
                .nombre("Zona Con Delimitacion " + ts)
                .build(), "admin");

        assertThat(zona.getTieneDelimitacion()).isFalse();

        ZonaPatrullajeResponse act = zonaService.guardarDelimitacion(zona.getId(), DelimitacionGeoJsonRequest.builder()
                .geometriaGeojson(GEOJSON_ZONA_VALIDO)
                .motivo("Delimitación técnica")
                .build(), "admin");

        assertThat(act.getTieneDelimitacion()).isTrue();
        assertThat(act.getGeometriaGeojson()).isNotBlank();
    }
}
