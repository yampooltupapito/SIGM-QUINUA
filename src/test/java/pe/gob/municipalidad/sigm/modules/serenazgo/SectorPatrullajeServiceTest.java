package pe.gob.municipalidad.sigm.modules.serenazgo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.SectorPatrullajeService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.ZonaPatrullajeService;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SectorPatrullajeServiceTest {

    @Autowired
    private SectorPatrullajeService sectorService;

    @Autowired
    private ZonaPatrullajeService zonaService;

    private static final String GEOJSON_VALIDO = "{\"type\":\"Polygon\",\"coordinates\":[[[-74.140,-13.040],[-74.135,-13.040],[-74.135,-13.045],[-74.140,-13.045],[-74.140,-13.040]]]}";
    private static final String GEOJSON_NO_CERRADO = "{\"type\":\"Polygon\",\"coordinates\":[[[-74.140,-13.040],[-74.135,-13.040],[-74.135,-13.045],[-74.140,-13.045]]]}";

    @Test
    @DisplayName("01: Creación exitosa de sector con generación correlativa de código")
    void test01_crearSectorExitoso() {
        long ts = System.currentTimeMillis();
        SectorCreateRequest req = SectorCreateRequest.builder()
                .nombre("Sector Centro " + ts)
                .descripcion("Área central del distrito de Quinua")
                .colorHex("#1d4ed8")
                .estado(EstadoSectorPatrullaje.ACTIVO)
                .fechaInicioVigencia(LocalDate.now())
                .build();

        SectorResponse resp = sectorService.crearSector(req, "admin");

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getCodigo()).startsWith("SEC-");
        assertThat(resp.getNombre()).isEqualTo("Sector Centro " + ts);
        assertThat(resp.getEstado()).isEqualTo(EstadoSectorPatrullaje.ACTIVO);
        assertThat(resp.getTieneDelimitacion()).isFalse();
    }

    @Test
    @DisplayName("02: Rechazo de creación de sector con nombre duplicado")
    void test02_rechazarSectorNombreDuplicado() {
        long ts = System.currentTimeMillis();
        String nombre = "Sector Duplicado " + ts;

        sectorService.crearSector(SectorCreateRequest.builder()
                .nombre(nombre)
                .build(), "admin");

        assertThatThrownBy(() -> sectorService.crearSector(SectorCreateRequest.builder()
                .nombre(nombre)
                .build(), "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un sector registrado con el nombre");
    }

    @Test
    @DisplayName("03: Rechazo de creación de sector con vigencia inconsistente")
    void test03_rechazarVigenciaInconsistente() {
        long ts = System.currentTimeMillis();
        SectorCreateRequest req = SectorCreateRequest.builder()
                .nombre("Sector Vigencia " + ts)
                .fechaInicioVigencia(LocalDate.of(2026, 6, 1))
                .fechaFinVigencia(LocalDate.of(2026, 5, 1))
                .build();

        assertThatThrownBy(() -> sectorService.crearSector(req, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha fin de vigencia no puede ser anterior");
    }

    @Test
    @DisplayName("04: Cambio de estado con registro de bitácora histórica")
    void test04_cambiarEstadoConHistorial() {
        long ts = System.currentTimeMillis();
        SectorResponse sector = sectorService.crearSector(SectorCreateRequest.builder()
                .nombre("Sector Transición " + ts)
                .estado(EstadoSectorPatrullaje.ACTIVO)
                .build(), "admin");

        SectorResponse mod = sectorService.cambiarEstado(sector.getId(), CambioEstadoTerritorioRequest.builder()
                .nuevoEstado(EstadoSectorPatrullaje.INACTIVO)
                .motivo("Reestructuración de cuadrantes")
                .build(), "admin");

        assertThat(mod.getEstado()).isEqualTo(EstadoSectorPatrullaje.INACTIVO);

        SectorDetailResponse detalle = sectorService.obtenerDetallePorId(sector.getId());
        assertThat(detalle.getHistorialEstados()).isNotEmpty();
        assertThat(detalle.getHistorialEstados().get(0).getEstadoNuevo()).isEqualTo("INACTIVO");
    }

    @Test
    @DisplayName("05: Desactivación de sector inactiva en cascada sus zonas dependientes")
    void test05_inactivarSectorDesactivaZonasEnCascada() {
        long ts = System.currentTimeMillis();
        SectorResponse sector = sectorService.crearSector(SectorCreateRequest.builder()
                .nombre("Sector Padre " + ts)
                .estado(EstadoSectorPatrullaje.ACTIVO)
                .build(), "admin");

        ZonaPatrullajeResponse zona = zonaService.crearZona(ZonaCreateRequest.builder()
                .sectorId(sector.getId())
                .nombre("Zona Hija " + ts)
                .estado(EstadoSectorPatrullaje.ACTIVO)
                .build(), "admin");

        assertThat(zona.getEstado()).isEqualTo(EstadoSectorPatrullaje.ACTIVO);

        // Desactivar sector
        sectorService.cambiarEstado(sector.getId(), CambioEstadoTerritorioRequest.builder()
                .nuevoEstado(EstadoSectorPatrullaje.INACTIVO)
                .motivo("Suspensión por reordenamiento")
                .build(), "admin");

        ZonaDetailResponse zonaActualizada = zonaService.obtenerDetallePorId(zona.getId());
        assertThat(zonaActualizada.getEstado()).isEqualTo(EstadoSectorPatrullaje.INACTIVO);
    }

    @Test
    @DisplayName("06: Guardar delimitación GeoJSON válida incrementando versión en historial")
    void test06_guardarDelimitacionGeoJsonValida() {
        long ts = System.currentTimeMillis();
        SectorResponse sector = sectorService.crearSector(SectorCreateRequest.builder()
                .nombre("Sector Geo " + ts)
                .build(), "admin");

        assertThat(sector.getTieneDelimitacion()).isFalse();

        SectorResponse delim = sectorService.guardarDelimitacion(sector.getId(), DelimitacionGeoJsonRequest.builder()
                .geometriaGeojson(GEOJSON_VALIDO)
                .motivo("Polígono aprobado por Consejo Municipal")
                .documentoReferencia("Ordenanza N° 012-2026")
                .build(), "admin");

        assertThat(delim.getTieneDelimitacion()).isTrue();
        assertThat(delim.getGeometriaGeojson()).isNotBlank();

        SectorDetailResponse detalle = sectorService.obtenerDetallePorId(sector.getId());
        assertThat(detalle.getHistorialDelimitaciones()).isNotEmpty();
        assertThat(detalle.getHistorialDelimitaciones().get(0).getNumeroVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("07: Rechazo de delimitación GeoJSON con polígono no cerrado")
    void test07_rechazarGeoJsonNoCerrado() {
        long ts = System.currentTimeMillis();
        SectorResponse sector = sectorService.crearSector(SectorCreateRequest.builder()
                .nombre("Sector Anillo Invalido " + ts)
                .build(), "admin");

        assertThatThrownBy(() -> sectorService.guardarDelimitacion(sector.getId(), DelimitacionGeoJsonRequest.builder()
                .geometriaGeojson(GEOJSON_NO_CERRADO)
                .motivo("Error en anillo")
                .build(), "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El anillo del polígono no está cerrado");
    }
}
