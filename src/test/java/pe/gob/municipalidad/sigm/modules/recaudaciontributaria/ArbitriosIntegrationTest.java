package pe.gob.municipalidad.sigm.modules.recaudaciontributaria;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoArbitrioCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.SimulacionArbitrioRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.ArbitrioDetailResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ArbitrioMunicipalService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ArbitriosIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ArbitrioMunicipalService arbitrioService;

    @Autowired
    private ContribuyenteTributarioRepository contribuyenteRepository;

    @Autowired
    private PredioTributarioRepository predioRepository;

    @Autowired
    private RelacionContribuyentePredioRepository relacionRepository;

    @Autowired
    private PeriodoTributarioRepository periodoTributarioRepository;

    @Autowired
    private CalculoArbitrioMunicipalRepository calculoRepository;

    @Autowired
    private ServicioArbitrioMunicipalRepository servicioRepository;

    @Autowired
    private ConfiguracionArbitrioMunicipalRepository configuracionRepository;

    @Autowired
    private CostoServicioArbitrioRepository costoRepository;

    private Cookie adminAuthCookie;
    private ContribuyenteTributario testContribuyente;
    private PredioTributario testPredio;
    private PeriodoTributario testPeriodo;

    @BeforeEach
    void setUp() {
        CustomUserDetails adminDetails = CustomUserDetails.builder()
                .id(105L)
                .username("admin_arbitrios_test")
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN"))
                .permisos(Set.of(
                        "TRIBUTARIA_ARBITRIOS_VER",
                        "TRIBUTARIA_ARBITRIOS_SIMULAR",
                        "TRIBUTARIA_ARBITRIOS_CALCULAR",
                        "TRIBUTARIA_ARBITRIOS_REVISAR_CALCULO",
                        "TRIBUTARIA_ARBITRIOS_APROBAR_CALCULO",
                        "TRIBUTARIA_ARBITRIOS_ANULAR_CALCULO",
                        "TRIBUTARIA_ARBITRIOS_EXPORTAR"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("TRIBUTARIA_ARBITRIOS_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_ARBITRIOS_SIMULAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_ARBITRIOS_CALCULAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_ARBITRIOS_REVISAR_CALCULO"),
                        new SimpleGrantedAuthority("TRIBUTARIA_ARBITRIOS_APROBAR_CALCULO"),
                        new SimpleGrantedAuthority("TRIBUTARIA_ARBITRIOS_ANULAR_CALCULO"),
                        new SimpleGrantedAuthority("TRIBUTARIA_ARBITRIOS_EXPORTAR")
                ))
                .build();
        String token = jwtTokenProvider.generateAccessToken(adminDetails);
        adminAuthCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, token);
        adminAuthCookie.setPath("/");

        // Contribuyente
        testContribuyente = contribuyenteRepository.save(ContribuyenteTributario.builder()
                .codigoContribuyente("CT-ARB-001")
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento("28495031")
                .nombreRazonSocial("María Huamán Gómez")
                .domicilioFiscal("Plaza Mayor de Quinua S/N")
                .estado(EstadoContribuyente.ACTIVO)
                .build());

        // Predio
        testPredio = predioRepository.save(PredioTributario.builder()
                .codigoPredio("PR-ARB-001")
                .tipoPredio(TipoPredio.URBANO)
                .clasificacionPredio(ClasificacionPredio.CASA_HABITACION)
                .usoDeclarado(UsoDeclaradoPredio.VIVIENDA)
                .direccion("Jr. Sucre 140, Quinua")
                .areaTerreno(new BigDecimal("160.00"))
                .areaTerrenoM2(new BigDecimal("160.00"))
                .areaConstruida(new BigDecimal("110.00"))
                .estado(EstadoPredio.ACTIVO)
                .build());

        // Periodo
        testPeriodo = periodoTributarioRepository.findByAnio(2026).orElseGet(() ->
                periodoTributarioRepository.save(PeriodoTributario.builder()
                        .anio(2026)
                        .fechaInicio(LocalDate.of(2026, 1, 1))
                        .fechaFin(LocalDate.of(2026, 12, 31))
                        .uitVigente(new BigDecimal("5150.00"))
                        .estado(EstadoPeriodoTributario.APERTURADO)
                        .build())
        );

        // Relacion
        relacionRepository.save(RelacionContribuyentePredio.builder()
                .contribuyente(testContribuyente)
                .predio(testPredio)
                .tipoRelacion(TipoRelacionContribuyentePredio.PROPIETARIO_UNICO)
                .porcentajeParticipacion(new BigDecimal("100.00"))
                .estado(EstadoRelacionContribuyentePredio.ACTIVO)
                .build());

        // Setup base services and configs if not present
        asegurarConfiguracionesServicio();
    }

    private void asegurarConfiguracionesServicio() {
        // Inactivar servicios tipo OTROS para aislar los 4 servicios de arbitrios municipales oficiales
        servicioRepository.findByTipoServicioAndEstado(TipoServicioArbitrio.OTROS, EstadoServicioArbitrio.ACTIVO)
                .forEach(s -> {
                    s.setEstado(EstadoServicioArbitrio.INACTIVO);
                    servicioRepository.save(s);
                });

        TipoServicioArbitrio[] cuatroServicios = {
                TipoServicioArbitrio.RECOLECCION_RESIDUOS,
                TipoServicioArbitrio.BARRIDO_CALLES,
                TipoServicioArbitrio.PARQUES_JARDINES,
                TipoServicioArbitrio.SERENAZGO
        };

        for (TipoServicioArbitrio tipo : cuatroServicios) {
            ServicioArbitrioMunicipal srv = servicioRepository.findByTipoServicioAndEstado(tipo, EstadoServicioArbitrio.ACTIVO).stream().findFirst().orElseGet(() ->
                    servicioRepository.save(ServicioArbitrioMunicipal.builder()
                            .codigo("SRV-" + tipo.name())
                            .nombre(tipo.getDescripcion())
                            .tipoServicio(tipo)
                            .estado(EstadoServicioArbitrio.ACTIVO)
                            .build())
            );

            if (configuracionRepository.findByServicioAndPeriodoOrderByVersionDesc(srv.getId(), testPeriodo.getId()).isEmpty()) {
                ConfiguracionArbitrioMunicipal cfg = configuracionRepository.save(ConfiguracionArbitrioMunicipal.builder()
                        .codigoConfiguracion("CFG-" + tipo.name() + "-2026")
                        .servicio(srv)
                        .periodo(testPeriodo)
                        .versionConfiguracion(1)
                        .fechaInicioVigencia(LocalDate.of(2026, 1, 1))
                        .fechaFinVigencia(LocalDate.of(2026, 12, 31))
                        .costoTotalAnual(new BigDecimal("30000.00"))
                        .costoTotalMensual(new BigDecimal("2500.00"))
                        .cantidadPrediosAfectos(1000)
                        .estado(EstadoConfiguracionArbitrio.ACTIVO)
                        .build());

                costoRepository.save(CostoServicioArbitrio.builder()
                        .configuracion(cfg)
                        .categoriaCosto(CategoriaCostoArbitrio.COSTO_DIRECTO_MANO_OBRA)
                        .descripcion("Personal operativo municipal")
                        .importe(new BigDecimal("30000.00"))
                        .estadoValidacion(EstadoValidacionCosto.APROBADO)
                        .build());
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. Vista Principal: Listado de determinaciones y configuraciones de arbitrios")
    void testListarArbitriosView() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/arbitrios")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/arbitrios/list"))
                .andExpect(model().attributeExists("calculos"))
                .andExpect(model().attributeExists("stats"))
                .andExpect(model().attributeExists("configuraciones"));
    }

    @Test
    @Order(2)
    @DisplayName("2. Vista Simulación: Formulario accesible para simular arbitrios")
    void testFormularioSimularView() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/arbitrios/simular")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/arbitrios/simular"))
                .andExpect(model().attributeExists("contribuyentes"))
                .andExpect(model().attributeExists("predios"))
                .andExpect(model().attributeExists("periodos"));
    }

    @Test
    @Order(3)
    @DisplayName("3. Simulación: Ejecución exitosa sin crear deuda exigible ni registro persistente definitivo")
    void testEjecutarSimulacion() throws Exception {
        long conteoAntes = calculoRepository.count();

        mockMvc.perform(post("/dashboard/tributaria/arbitrios/simular")
                        .cookie(adminAuthCookie)
                        .with(csrf())
                        .param("contribuyenteId", testContribuyente.getId().toString())
                        .param("predioId", testPredio.getId().toString())
                        .param("periodoId", testPeriodo.getId().toString())
                        .param("beneficio", "NINGUNO")
                        .param("porcentajeCondomino", "100.00")
                        .param("fechaReferencia", "2026-05-15"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/arbitrios/simular"))
                .andExpect(model().attributeExists("resultado"))
                .andExpect(model().attribute("esSimulacion", true));

        // Solo se crea un registro efímero en estado SIMULACION si aplica, pero no PRELIMINAR/APROBADO
        List<CalculoArbitrioMunicipal> preliminares = calculoRepository.findByEstado(EstadoCalculoArbitrio.PRELIMINAR);
        assertEquals(0, preliminares.size(), "La simulación no debe crear determinaciones en estado PRELIMINAR");
    }

    @Test
    @Order(4)
    @DisplayName("4. Determinación Preliminar: Creación formal de cálculo en estado PRELIMINAR")
    void testCrearCalculoPreliminar() throws Exception {
        CalculoArbitrioCreateRequest req = CalculoArbitrioCreateRequest.builder()
                .contribuyenteId(testContribuyente.getId())
                .predioId(testPredio.getId())
                .periodoId(testPeriodo.getId())
                .beneficio(TipoBeneficioArbitrio.NINGUNO)
                .porcentajeCondomino(new BigDecimal("100.00"))
                .fechaReferencia(LocalDate.now())
                .build();

        ArbitrioDetailResponse nuevo = arbitrioService.crearCalculoPreliminar(req, "admin_arbitrios_test");
        assertNotNull(nuevo);
        assertNotNull(nuevo.getId());
        assertEquals(EstadoCalculoArbitrio.PRELIMINAR, nuevo.getEstado());
        assertTrue(nuevo.getImporteAnualTotal().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(4, nuevo.getDetalles().size(), "Debe desglosar los 4 servicios públicos municipales");

        // Verificación de la vista detalle
        mockMvc.perform(get("/dashboard/tributaria/arbitrios/calculos/" + nuevo.getId())
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/arbitrios/detail"))
                .andExpect(model().attributeExists("calculo"));
    }

    @Test
    @Order(5)
    @DisplayName("5. Ciclo de Vida: PRELIMINAR -> EN_REVISION -> OBSERVADO -> EN_REVISION -> APROBADO")
    void testFlujoEstadosCalculo() throws Exception {
        CalculoArbitrioCreateRequest req = CalculoArbitrioCreateRequest.builder()
                .contribuyenteId(testContribuyente.getId())
                .predioId(testPredio.getId())
                .periodoId(testPeriodo.getId())
                .beneficio(TipoBeneficioArbitrio.NINGUNO)
                .porcentajeCondomino(new BigDecimal("100.00"))
                .fechaReferencia(LocalDate.now())
                .build();
        ArbitrioDetailResponse calculo = arbitrioService.crearCalculoPreliminar(req, "admin_arbitrios_test");
        UUID id = calculo.getId();

        // 1. Enviar a revisión
        mockMvc.perform(post("/dashboard/tributaria/arbitrios/calculos/" + id + "/revision")
                        .cookie(adminAuthCookie)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard/tributaria/arbitrios/calculos/" + id));

        CalculoArbitrioMunicipal c1 = calculoRepository.findById(id).orElseThrow();
        assertEquals(EstadoCalculoArbitrio.EN_REVISION, c1.getEstado());

        // 2. Observar
        mockMvc.perform(post("/dashboard/tributaria/arbitrios/calculos/" + id + "/observar")
                        .cookie(adminAuthCookie)
                        .with(csrf())
                        .param("motivo", "Verificar frente lineal con catastro"))
                .andExpect(status().is3xxRedirection());

        CalculoArbitrioMunicipal c2 = calculoRepository.findById(id).orElseThrow();
        assertEquals(EstadoCalculoArbitrio.OBSERVADO, c2.getEstado());

        // 3. Volver a revisión
        mockMvc.perform(post("/dashboard/tributaria/arbitrios/calculos/" + id + "/revision")
                        .cookie(adminAuthCookie)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        // 4. Aprobar
        mockMvc.perform(post("/dashboard/tributaria/arbitrios/calculos/" + id + "/aprobar")
                        .cookie(adminAuthCookie)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        CalculoArbitrioMunicipal c3 = calculoRepository.findById(id).orElseThrow();
        assertEquals(EstadoCalculoArbitrio.APROBADO, c3.getEstado());
    }

    @Test
    @Order(6)
    @DisplayName("6. Versionado: Recálculo genera v2 conservando historial inmutable")
    void testRecalcularNuevaVersion() throws Exception {
        CalculoArbitrioCreateRequest req = CalculoArbitrioCreateRequest.builder()
                .contribuyenteId(testContribuyente.getId())
                .predioId(testPredio.getId())
                .periodoId(testPeriodo.getId())
                .beneficio(TipoBeneficioArbitrio.NINGUNO)
                .porcentajeCondomino(new BigDecimal("100.00"))
                .fechaReferencia(LocalDate.now())
                .build();
        ArbitrioDetailResponse calculo = arbitrioService.crearCalculoPreliminar(req, "admin_arbitrios_test");
        assertEquals(1, calculo.getVersionCalculo());

        mockMvc.perform(post("/dashboard/tributaria/arbitrios/calculos/" + calculo.getId() + "/recalcular")
                        .cookie(adminAuthCookie)
                        .with(csrf())
                        .param("motivo", "Actualización por rectificación de uso declarado"))
                .andExpect(status().is3xxRedirection());

        // Debe haber 2 determinaciones (v1 y v2)
        List<CalculoArbitrioMunicipal> todas = calculoRepository.findAll();
        boolean existeV2 = todas.stream().anyMatch(c -> c.getVersionCalculo() == 2);
        assertTrue(existeV2, "Debe existir un cálculo con versionCalculo = 2");
    }

    @Test
    @Order(7)
    @DisplayName("7. Anulación: Requiere motivo y cambia estado a ANULADO con trazabilidad")
    void testAnulacionCalculo() throws Exception {
        CalculoArbitrioCreateRequest req = CalculoArbitrioCreateRequest.builder()
                .contribuyenteId(testContribuyente.getId())
                .predioId(testPredio.getId())
                .periodoId(testPeriodo.getId())
                .beneficio(TipoBeneficioArbitrio.NINGUNO)
                .porcentajeCondomino(new BigDecimal("100.00"))
                .fechaReferencia(LocalDate.now())
                .build();
        ArbitrioDetailResponse calculo = arbitrioService.crearCalculoPreliminar(req, "admin_arbitrios_test");

        mockMvc.perform(post("/dashboard/tributaria/arbitrios/calculos/" + calculo.getId() + "/anular")
                        .cookie(adminAuthCookie)
                        .with(csrf())
                        .param("motivo", "Transferencia de dominio informada extemporáneamente"))
                .andExpect(status().is3xxRedirection());

        CalculoArbitrioMunicipal anulado = calculoRepository.findById(calculo.getId()).orElseThrow();
        assertEquals(EstadoCalculoArbitrio.ANULADO, anulado.getEstado());
    }

    @Test
    @Order(8)
    @DisplayName("8. Exportaciones: Descarga de Excel, PDF de Padrón y Hoja de Liquidación")
    void testExportacionesDocumentales() throws Exception {
        CalculoArbitrioCreateRequest req = CalculoArbitrioCreateRequest.builder()
                .contribuyenteId(testContribuyente.getId())
                .predioId(testPredio.getId())
                .periodoId(testPeriodo.getId())
                .beneficio(TipoBeneficioArbitrio.NINGUNO)
                .porcentajeCondomino(new BigDecimal("100.00"))
                .fechaReferencia(LocalDate.now())
                .build();
        ArbitrioDetailResponse calculo = arbitrioService.crearCalculoPreliminar(req, "admin_arbitrios_test");

        // Excel
        mockMvc.perform(get("/dashboard/tributaria/arbitrios/exportar/excel")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        // PDF Listado
        mockMvc.perform(get("/dashboard/tributaria/arbitrios/exportar/pdf")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        // PDF Hoja Liquidación
        mockMvc.perform(get("/dashboard/tributaria/arbitrios/calculos/" + calculo.getId() + "/liquidacion-pdf")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    @Order(9)
    @DisplayName("9. API REST: Endpoints seguros de simulación y estadísticas")
    void testApiRestEndpoints() throws Exception {
        // Estadísticas
        mockMvc.perform(get("/api/v1/tributaria/arbitrios/estadisticas")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCalculos").exists())
                .andExpect(jsonPath("$.aprobados").exists());

        // Listado JSON
        mockMvc.perform(get("/api/v1/tributaria/arbitrios/calculos")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @Order(10)
    @DisplayName("10. Seguridad: Rechazo de acceso a usuarios no autorizados o sin sesión")
    void testSeguridadSinSesion() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/arbitrios"))
                .andExpect(status().is3xxRedirection()); // Redirige a login

        mockMvc.perform(get("/api/v1/tributaria/arbitrios/calculos"))
                .andExpect(status().isUnauthorized());
    }
}
