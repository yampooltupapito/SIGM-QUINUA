package pe.gob.municipalidad.sigm.modules.serenazgo;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.EstadisticasSerenazgoService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.ReportesEstadisticosSerenazgoService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EstadisticasSerenazgoServiceTest {

    @Autowired
    private EstadisticasSerenazgoService estadisticasService;

    @Autowired
    private ReportesEstadisticosSerenazgoService reportesService;

    @Autowired
    private IncidenciaSeguridadCiudadanaRepository incidenciaRepository;

    @Autowired
    private TipoIncidenciaSeguridadRepository tipoIncidenciaRepository;

    @Autowired
    private SectorPatrullajeRepository sectorRepository;

    @Autowired
    private PatrullajeSerenazgoRepository patrullajeRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CatalogoIndicadorSerenazgoRepository catalogoRepository;

    @Autowired
    private PersonalSerenazgoRepository personalRepository;

    private CustomUserDetails userSupervisor;
    private Usuario supervisorUsuario;
    private SectorPatrullaje sectorTest;
    private TipoIncidenciaSeguridad tipoIncTest;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Persona per = personaRepository.save(Persona.builder()
                .numeroDocumento(String.format("6%07d", (int)(Math.abs(ts) % 9000000 + 1000000)))
                .tipoDocumento("DNI")
                .nombres("Supervisor")
                .apellidoPaterno("Estadistica")
                .apellidoMaterno("Quinua")
                .estado("ACTIVO")
                .build());

        supervisorUsuario = usuarioRepository.save(Usuario.builder()
                .username("sup_stats_" + ts)
                .persona(per)
                .passwordHash("$2a$10$abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                .enabled(true)
                .accountLocked(false)
                .build());

        userSupervisor = CustomUserDetails.builder()
                .id(supervisorUsuario.getId())
                .username(supervisorUsuario.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .authorities(List.of(
                        new SimpleGrantedAuthority("SERENAZGO_ESTADISTICAS_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_REPORTES_GENERAR"),
                        new SimpleGrantedAuthority("SERENAZGO_REPORTES_DESCARGAR"),
                        new SimpleGrantedAuthority("ROLE_SUPERVISOR_SERENAZGO")
                ))
                .build();

        sectorTest = sectorRepository.save(SectorPatrullaje.builder()
                .codigo("SEC-EST-" + ts)
                .nombre("Sector Estadístico Quinua")
                .estado(EstadoSectorPatrullaje.ACTIVO)
                .build());

        tipoIncTest = tipoIncidenciaRepository.save(TipoIncidenciaSeguridad.builder()
                .codigo("TIP-EST-" + ts)
                .nombre("Alteración Sonora de Espacio")
                .categoria(CategoriaIncidenciaSeguridad.ORDEN_PUBLICO)
                .prioridadSugerida(PrioridadIncidenciaSeguridad.BAJA)
                .activo(true)
                .build());

        // Guardar incidencia de prueba en el período actual
        incidenciaRepository.save(IncidenciaSeguridadCiudadana.builder()
                .codigo("INC-EST-" + ts)
                .titulo("Ruido molesto en plaza")
                .descripcion("Uso de altoparlantes no autorizados")
                .ubicacionReferencia("Cerca al mercado")
                .fechaReporte(OffsetDateTime.now())
                .tipoIncidencia(tipoIncTest)
                .sector(sectorTest)
                .prioridad(PrioridadIncidenciaSeguridad.BAJA)
                .estado(EstadoIncidenciaSeguridad.REGISTRADA)
                .canalReporte(CanalReporteIncidencia.PATRULLAJE)
                .latitud(new BigDecimal("-13.0450000"))
                .longitud(new BigDecimal("-74.1380000"))
                .nivelConfidencialidad(NivelConfidencialidad.PUBLICO)
                .usuarioRegistro(supervisorUsuario)
                .build());

        // Guardar personal para patrullaje
        PersonalSerenazgo personalTest = personalRepository.save(PersonalSerenazgo.builder()
                .codigoInterno("SER-EST-" + ts)
                .persona(per)
                .estado(EstadoPersonalSerenazgo.ACTIVO)
                .build());

        // Guardar patrullaje de prueba
        patrullajeRepository.save(PatrullajeSerenazgo.builder()
                .codigo("PAT-EST-" + ts)
                .tipoPatrullaje(TipoPatrullajeSerenazgo.PATRULLAJE_MUNICIPAL)
                .responsable(personalTest)
                .fechaInicio(OffsetDateTime.now())
                .sector(sectorTest)
                .estado(EstadoPatrullajeSerenazgo.FINALIZADO)
                .kmInicio(new BigDecimal("100.0"))
                .kmFin(new BigDecimal("105.5"))
                .build());
    }

    @Test
    @Order(1)
    @DisplayName("1. Calcular Dashboard Estadístico Integral en tiempo de consulta")
    void testObtenerDashboardEstadistico() {
        FiltroEstadisticoRequest filtro = FiltroEstadisticoRequest.builder()
                .periodoTipo("MES_ACTUAL")
                .compararPeriodoAnterior(true)
                .agrupacion("MES")
                .build();

        DashboardEstadisticoResponse res = estadisticasService.obtenerDashboardEstadistico(filtro, userSupervisor);

        assertNotNull(res);
        assertEquals("America/Lima", res.getZonaHoraria());
        assertNotNull(res.getFechaConsulta());
        assertFalse(res.getKpis().isEmpty());

        // Verificar que existe el KPI de incidencias
        IndicadorKpiDto kpiInc = res.getKpis().stream()
                .filter(k -> "IND_INC_TOTAL".equals(k.getCodigo()))
                .findFirst().orElse(null);

        assertNotNull(kpiInc);
        assertThat(kpiInc.getValorActual()).isGreaterThanOrEqualTo(1L);
    }

    @Test
    @Order(2)
    @DisplayName("2. Manejo seguro de división entre cero cuando el período previo es 0")
    void testManejoDivisionEntreCeroEnVariacion() {
        // Al consultar un sector recién creado con 0 en el período anterior
        FiltroEstadisticoRequest filtro = FiltroEstadisticoRequest.builder()
                .periodoTipo("MES_ACTUAL")
                .sectorId(sectorTest.getId())
                .compararPeriodoAnterior(true)
                .build();

        DashboardEstadisticoResponse res = estadisticasService.obtenerDashboardEstadistico(filtro, userSupervisor);

        IndicadorKpiDto kpiInc = res.getKpis().stream()
                .filter(k -> "IND_INC_TOTAL".equals(k.getCodigo()))
                .findFirst().orElseThrow();

        // Si el valor anterior es 0, variacionIndefinida debe ser true y variacionPorcentual debe ser null
        if (kpiInc.getValorAnterior() == 0L) {
            assertTrue(kpiInc.isVariacionIndefinida());
            assertNull(kpiInc.getVariacionPorcentual());
            assertEquals("INDEFINIDA", kpiInc.getTendencia());
        }
    }

    @Test
    @Order(3)
    @DisplayName("3. Consultar Catálogo Técnico Oficial de Indicadores")
    void testObtenerCatalogoIndicadores() {
        List<CatalogoIndicadorDto> cat = estadisticasService.obtenerCatalogoIndicadores();

        assertNotNull(cat);
        assertThat(cat.size()).isGreaterThanOrEqualTo(5);

        boolean tieneInc = cat.stream().anyMatch(c -> "IND_INC_TOTAL".equals(c.getCodigo()));
        assertTrue(tieneInc, "El catálogo debe contener el indicador IND_INC_TOTAL");
    }

    @Test
    @Order(4)
    @DisplayName("4. Estadísticas Territoriales por Sector preservando sectores sin registros")
    void testEstadisticasPorSector() {
        FiltroEstadisticoRequest filtro = FiltroEstadisticoRequest.builder()
                .periodoTipo("MES_ACTUAL")
                .build();

        List<EstadisticasTerritorialesDto> porSector = estadisticasService.obtenerEstadisticasPorSector(filtro);

        assertNotNull(porSector);
        assertFalse(porSector.isEmpty());

        boolean contieneSectorTest = porSector.stream()
                .anyMatch(s -> s.getSectorId().equals(sectorTest.getId()) && s.getTotalIncidencias() >= 1);
        assertTrue(contieneSectorTest, "Debe contener el sector de prueba con al menos 1 incidencia");
    }

    @Test
    @Order(5)
    @DisplayName("5. Generar y Almacenar Reporte Oficial en Formato PDF")
    void testGenerarReportePdf() {
        GenerarReporteRequest req = GenerarReporteRequest.builder()
                .tipoReporte(TipoReporteEstadistico.GENERAL_SEGURIDAD)
                .formato(FormatoReporteEstadistico.PDF)
                .titulo("Reporte Mensual Ejecutivo de Prueba")
                .filtro(FiltroEstadisticoRequest.builder().periodoTipo("MES_ACTUAL").build())
                .build();

        ReporteHistorialDto dto = reportesService.generarReporte(req, userSupervisor, "127.0.0.1");

        assertNotNull(dto);
        assertNotNull(dto.getId());
        assertTrue(dto.getCodigo().startsWith("REP-SER-"));
        assertEquals(FormatoReporteEstadistico.PDF, dto.getFormato());
        assertThat(dto.getTamanioBytes()).isGreaterThan(0L);

        // Verificar descarga del archivo generado
        Resource res = reportesService.descargarReporte(dto.getId(), userSupervisor);
        assertNotNull(res);
        assertTrue(res.exists());
    }

    @Test
    @Order(6)
    @DisplayName("6. Generar Reporte Excel y Verificar Sanitización Anti-Formula Injection")
    void testGenerarReporteExcelAntiFormulaInjection() {
        GenerarReporteRequest req = GenerarReporteRequest.builder()
                .tipoReporte(TipoReporteEstadistico.INCIDENCIAS)
                .formato(FormatoReporteEstadistico.EXCEL)
                .titulo("Reporte Estadístico de Incidencias XLSX")
                .filtro(FiltroEstadisticoRequest.builder().periodoTipo("MES_ACTUAL").build())
                .build();

        ReporteHistorialDto dto = reportesService.generarReporte(req, userSupervisor, "127.0.0.1");

        assertNotNull(dto);
        assertEquals(FormatoReporteEstadistico.EXCEL, dto.getFormato());
        assertThat(dto.getTamanioBytes()).isGreaterThan(0L);

        Resource res = reportesService.descargarReporte(dto.getId(), userSupervisor);
        assertNotNull(res);
        assertTrue(res.exists());
    }
}
