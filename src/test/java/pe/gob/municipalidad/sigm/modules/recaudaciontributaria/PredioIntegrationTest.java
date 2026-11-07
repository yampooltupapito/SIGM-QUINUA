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
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ContribuyenteService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.PredioExportService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.PredioService;
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
class PredioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PredioService predioService;

    @Autowired
    private PredioExportService exportService;

    @Autowired
    private ContribuyenteService contribuyenteService;

    private Cookie adminAuthCookie;

    @BeforeEach
    void setUp() {
        CustomUserDetails adminDetails = CustomUserDetails.builder()
                .id(102L)
                .username("admin.predios")
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN"))
                .permisos(Set.of(
                        "TRIBUTARIA_PREDIOS_VER",
                        "TRIBUTARIA_PREDIOS_CREAR",
                        "TRIBUTARIA_PREDIOS_EDITAR",
                        "TRIBUTARIA_PREDIOS_CAMBIAR_ESTADO",
                        "TRIBUTARIA_PREDIOS_GESTIONAR_CONSTRUCCIONES",
                        "TRIBUTARIA_PREDIOS_GESTIONAR_RELACIONES",
                        "TRIBUTARIA_PREDIOS_GESTIONAR_DOCUMENTOS",
                        "TRIBUTARIA_PREDIOS_EXPORTAR"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PREDIOS_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PREDIOS_CREAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PREDIOS_EDITAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PREDIOS_CAMBIAR_ESTADO"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PREDIOS_GESTIONAR_CONSTRUCCIONES"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PREDIOS_GESTIONAR_RELACIONES"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PREDIOS_GESTIONAR_DOCUMENTOS"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PREDIOS_EXPORTAR")
                ))
                .build();

        String jwt = jwtTokenProvider.generateAccessToken(adminDetails);
        adminAuthCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, jwt);
        adminAuthCookie.setPath("/");
    }

    @Test
    @Order(1)
    @DisplayName("Registro de Predio Urbano con código interno correlativo y validación de áreas")
    void testRegistrarPredioUrbano() {
        PredioCreateRequest request = PredioCreateRequest.builder()
                .tipoPredio(TipoPredio.URBANO)
                .clasificacionPredio(ClasificacionPredio.CASA_HABITACION)
                .usoDeclarado(UsoDeclaradoPredio.VIVIENDA)
                .departamento("AYACUCHO")
                .provincia("HUAMANGA")
                .distrito("QUINUA")
                .ubigeo("050110")
                .tipoVia("Jr.")
                .nombreVia("Sucre")
                .numeroMunicipal("125")
                .areaTerreno(new BigDecimal("220.50"))
                .unidadArea("M2")
                .tieneAgua(true)
                .tieneDesague(true)
                .tieneLuz(true)
                .latitud(new BigDecimal("-13.045612"))
                .longitud(new BigDecimal("-74.138945"))
                .fuenteCoordenadas(FuenteCoordenadas.GPS_NAVEGADOR_DE_CAMPO)
                .build();

        PredioDetailResponse response = predioService.registrarPredio(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertNotNull(response.getCodigoPredio());
        assertTrue(response.getCodigoPredio().startsWith("PR-"));
        assertEquals(TipoPredio.URBANO, response.getTipoPredio());
        assertEquals(EstadoPredio.ACTIVO, response.getEstado());
        assertEquals(new BigDecimal("220.50"), response.getAreaTerreno());
        assertEquals(new BigDecimal("220.50"), response.getAreaTerrenoM2());
        assertTrue(response.isTieneAgua());
    }

    @Test
    @Order(2)
    @DisplayName("Registro de Predio Rural con superficie en Hectáreas y conversión automática a m²")
    void testRegistrarPredioRuralConHectareas() {
        PredioCreateRequest request = PredioCreateRequest.builder()
                .tipoPredio(TipoPredio.RURAL)
                .clasificacionPredio(ClasificacionPredio.RUSTICO_AGRICOLA)
                .usoDeclarado(UsoDeclaradoPredio.AGRICOLA)
                .departamento("AYACUCHO")
                .provincia("HUAMANGA")
                .distrito("QUINUA")
                .ubigeo("050110")
                .comunidadCentroPoblado("Comunidad de San Cristóbal")
                .referencia("Paraje Pampas de Quinua, colindante con canal de regadío")
                .areaTerreno(new BigDecimal("1.75"))
                .unidadArea("HA")
                .tieneAgua(false)
                .tieneLuz(false)
                .build();

        PredioDetailResponse response = predioService.registrarPredio(request);

        assertNotNull(response);
        assertEquals(TipoPredio.RURAL, response.getTipoPredio());
        assertEquals("HA", response.getUnidadArea());
        assertEquals(new BigDecimal("1.75"), response.getAreaTerreno());
        // 1.75 HA * 10,000 = 17,500.00 m²
        assertEquals(0, new BigDecimal("17500.00").compareTo(response.getAreaTerrenoM2()));
    }

    @Test
    @Order(3)
    @DisplayName("Gestión de construcciones y recálculo acumulativo del área construida total")
    void testAgregarConstruccionConRecalculoAreaConstruida() {
        PredioCreateRequest predioReq = PredioCreateRequest.builder()
                .tipoPredio(TipoPredio.URBANO)
                .clasificacionPredio(ClasificacionPredio.CASA_HABITACION)
                .usoDeclarado(UsoDeclaradoPredio.VIVIENDA)
                .areaTerreno(new BigDecimal("300.00"))
                .unidadArea("M2")
                .direccion("Jr. Bolognesi S/N, Quinua")
                .build();
        PredioDetailResponse predio = predioService.registrarPredio(predioReq);

        // Nivel 1: 95.50 m²
        ConstruccionPredialRequest c1 = ConstruccionPredialRequest.builder()
                .codigoConstruccion("ED-01")
                .descripcion("Primer Piso Material Noble")
                .nivelPiso(1)
                .areaConstruida(new BigDecimal("95.50"))
                .materialPredominante(MaterialPredominante.LADRILLO_CONCRETO)
                .estadoConservacion(EstadoConservacion.BUENO)
                .anioConstruccion(2020)
                .build();
        predioService.agregarConstruccion(predio.getId(), c1);

        // Nivel 2: 80.00 m²
        ConstruccionPredialRequest c2 = ConstruccionPredialRequest.builder()
                .codigoConstruccion("ED-02")
                .descripcion("Segundo Piso")
                .nivelPiso(2)
                .areaConstruida(new BigDecimal("80.00"))
                .materialPredominante(MaterialPredominante.LADRILLO_CONCRETO)
                .estadoConservacion(EstadoConservacion.BUENO)
                .anioConstruccion(2022)
                .build();
        predioService.agregarConstruccion(predio.getId(), c2);

        PredioDetailResponse actualizado = predioService.obtenerPredioPorId(predio.getId());
        assertEquals(2, actualizado.getConstrucciones().size());
        // Total construido: 95.50 + 80.00 = 175.50 m²
        assertEquals(0, new BigDecimal("175.50").compareTo(actualizado.getAreaConstruida()));
    }

    @Test
    @Order(4)
    @DisplayName("Vinculación de Contribuyente con relación jurídica y porcentaje de participación")
    void testVincularContribuyenteConPorcentaje() {
        // Registrar predio
        PredioCreateRequest predioReq = PredioCreateRequest.builder()
                .tipoPredio(TipoPredio.URBANO)
                .clasificacionPredio(ClasificacionPredio.CASA_HABITACION)
                .usoDeclarado(UsoDeclaradoPredio.VIVIENDA)
                .areaTerreno(new BigDecimal("180.00"))
                .unidadArea("M2")
                .direccion("Jr. Sucre 450, Quinua")
                .build();
        PredioDetailResponse predio = predioService.registrarPredio(predioReq);

        // Registrar contribuyente
        ContribuyenteCreateRequest contribReq = ContribuyenteCreateRequest.builder()
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento("47895612")
                .nombres("MARIO")
                .primerApellido("FLORES")
                .segundoApellido("ROJAS")
                .build();
        ContribuyenteDetailResponse contribuyente = contribuyenteService.registrarContribuyente(contribReq, "admin");

        // Vincular relación
        RelacionContribuyentePredioRequest relReq = RelacionContribuyentePredioRequest.builder()
                .contribuyenteId(contribuyente.getId())
                .tipoRelacion(TipoRelacionContribuyentePredio.TITULAR_DECLARADO)
                .porcentajeParticipacion(new BigDecimal("100.00"))
                .observaciones("Escritura Pública N° 456-2024")
                .fechaInicio(LocalDate.now())
                .build();

        predioService.vincularContribuyente(predio.getId(), relReq);

        // Verificar desde el predio
        PredioDetailResponse predioActualizado = predioService.obtenerPredioPorId(predio.getId());
        assertEquals(1, predioActualizado.getRelacionesContribuyente().size());
        assertEquals("FLORES ROJAS, MARIO", predioActualizado.getRelacionesContribuyente().get(0).getNombreContribuyente());

        // Verificar desde el contribuyente
        List<RelacionContribuyenteResponse> prediosDelContrib = predioService.listarPrediosPorContribuyente(contribuyente.getId());
        assertEquals(1, prediosDelContrib.size());
        assertEquals(predio.getCodigoPredio(), prediosDelContrib.get(0).getCodigoPredio());
    }

    @Test
    @Order(5)
    @DisplayName("Cambio de estado administrativo con trazabilidad y auditoría de motivos")
    void testCambiarEstadoConAuditoria() {
        PredioCreateRequest predioReq = PredioCreateRequest.builder()
                .tipoPredio(TipoPredio.URBANO)
                .clasificacionPredio(ClasificacionPredio.COMERCIAL)
                .usoDeclarado(UsoDeclaradoPredio.COMERCIO)
                .areaTerreno(new BigDecimal("200.00"))
                .unidadArea("M2")
                .direccion("Av. Centenario 100, Quinua")
                .build();
        PredioDetailResponse predio = predioService.registrarPredio(predioReq);

        // Pasar a OBSERVADO
        CambioEstadoPredioRequest cambio = CambioEstadoPredioRequest.builder()
                .nuevoEstado(EstadoPredio.OBSERVADO)
                .motivo("Discrepancia en linderos declarados respecto a inspección de campo")
                .build();
        predioService.cambiarEstado(predio.getId(), cambio);

        PredioDetailResponse obs = predioService.obtenerPredioPorId(predio.getId());
        assertEquals(EstadoPredio.OBSERVADO, obs.getEstado());
        assertEquals(2, obs.getHistorialEstados().size());
        assertTrue(obs.getHistorialEstados().stream().anyMatch(h -> h.getMotivo().contains("Discrepancia en linderos")));
    }

    @Test
    @Order(6)
    @DisplayName("Exportación autorizada de predios a Excel y PDF")
    void testExportarExcelYPdf() {
        PredioFilterDto filtro = PredioFilterDto.builder().build();

        byte[] excelBytes = exportService.exportarExcel(filtro);
        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 100);

        byte[] pdfBytes = exportService.exportarPdf(filtro);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 100);
    }

    @Test
    @Order(7)
    @DisplayName("Controlador Web MVC: Vista de listado, filtros y Ficha Predial protegidas")
    void testControladorMvcListarYFichaPredial() throws Exception {
        PredioCreateRequest predioReq = PredioCreateRequest.builder()
                .tipoPredio(TipoPredio.URBANO)
                .clasificacionPredio(ClasificacionPredio.CASA_HABITACION)
                .usoDeclarado(UsoDeclaradoPredio.VIVIENDA)
                .areaTerreno(new BigDecimal("150.00"))
                .unidadArea("M2")
                .direccion("Jr. Sucre 210, Quinua")
                .latitud(new BigDecimal("-13.045612"))
                .longitud(new BigDecimal("-74.138945"))
                .build();
        PredioDetailResponse predio = predioService.registrarPredio(predioReq);

        // Listado
        mockMvc.perform(get("/dashboard/tributaria/predios")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("predios"))
                .andExpect(model().attributeExists("estadisticas"))
                .andExpect(view().name("admin/tributaria/predios/list"));

        // Ficha Predial
        mockMvc.perform(get("/dashboard/tributaria/predios/" + predio.getId())
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("predio"))
                .andExpect(view().name("admin/tributaria/predios/detail"));
    }

    @Test
    @Order(8)
    @DisplayName("Controlador REST: Consulta asíncrona de búsqueda y verificación de código catastral")
    void testApiRestBuscarYValidarCatastral() throws Exception {
        mockMvc.perform(get("/api/v1/tributaria/predios/buscar?query=PR-")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(post("/api/v1/tributaria/predios/validar-catastral")
                        .cookie(adminAuthCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigoCatastral\":\"999999999999\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicado").value(false));
    }
}
