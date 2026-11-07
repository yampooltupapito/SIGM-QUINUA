package pe.gob.municipalidad.sigm.modules.recaudaciontributaria;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.DashboardTributarioResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.RecaudacionTributariaService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecaudacionTributariaCoreIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PeriodoTributarioRepository periodoRepository;

    @Autowired
    private ConceptoTributarioRepository conceptoRepository;

    @Autowired
    private ContribuyenteTributarioRepository contribuyenteRepository;

    @Autowired
    private PredioTributarioRepository predioRepository;

    @Autowired
    private RelacionContribuyentePredioRepository relacionRepository;

    @Autowired
    private RecaudacionTributariaService recaudacionTributariaService;

    private Cookie adminAuthCookie;

    @BeforeEach
    void setUp() {
        CustomUserDetails adminDetails = CustomUserDetails.builder()
                .id(999L)
                .username("admin.tributaria")
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN"))
                .permisos(Set.of("TRIBUTARIA_MODULO_VER", "TRIBUTARIA_DASHBOARD_VER"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("TRIBUTARIA_MODULO_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_DASHBOARD_VER")
                ))
                .build();

        String jwt = jwtTokenProvider.generateAccessToken(adminDetails);
        adminAuthCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, jwt);
        adminAuthCookie.setPath("/");
    }

    @Test
    @Order(1)
    @DisplayName("1. Flyway V52: Verificación de Períodos Fiscales y UIT Oficial de Quinua")
    void testPeriodosInicialesYCargaCatalogo() {
        Optional<PeriodoTributario> periodo2026Opt = periodoRepository.findByAnio(2026);
        assertTrue(periodo2026Opt.isPresent(), "El período fiscal 2026 debe existir en la base de datos");

        PeriodoTributario p2026 = periodo2026Opt.get();
        assertEquals(EstadoPeriodoTributario.APERTURADO, p2026.getEstado());
        assertNotNull(p2026.getUitVigente());
        assertTrue(p2026.getUitVigente().compareTo(new BigDecimal("5000.00")) > 0, "La UIT 2026 debe ser mayor a 5000");

        assertTrue(periodoRepository.existsByAnio(2024));
        assertTrue(periodoRepository.existsByAnio(2025));
    }

    @Test
    @Order(2)
    @DisplayName("2. Flyway V52: Verificación de Conceptos Tributarios Oficiales Base (D. Leg. 776)")
    void testConceptosTributariosIniciales() {
        Optional<ConceptoTributario> predial = conceptoRepository.findByCodigo("IMP_PREDIAL");
        assertTrue(predial.isPresent(), "El Impuesto Predial Anual debe estar configurado");
        assertEquals(TipoConceptoTributario.IMPUESTO_PREDIAL, predial.get().getTipoConcepto());
        assertEquals(PeriodicidadConcepto.ANUAL, predial.get().getPeriodicidad());
        assertEquals(EstadoConceptoTributario.VIGENTE, predial.get().getEstado());

        Optional<ConceptoTributario> serenazgo = conceptoRepository.findByCodigo("ARB_SERENAZGO");
        assertTrue(serenazgo.isPresent(), "El Arbitrio de Serenazgo debe estar configurado");
        assertEquals(TipoConceptoTributario.ARBITRIOS_MUNICIPALES, serenazgo.get().getTipoConcepto());
        assertEquals(PeriodicidadConcepto.TRIMESTRAL, serenazgo.get().getPeriodicidad());
    }

    @Test
    @Order(3)
    @DisplayName("3. Persistencia de Contribuyente, Enmascaramiento y Control de Versión (@Version)")
    void testPersistenciaYRestriccionesContribuyente() {
        long ts = System.currentTimeMillis();
        String doc = String.format("4%07d", (int) (Math.abs(ts) % 9000000 + 1000000));

        ContribuyenteTributario c = ContribuyenteTributario.builder()
                .codigoContribuyente("CTB-TST-" + ts)
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento(doc)
                .nombreRazonSocial("Alipio Quispe Morales")
                .domicilioFiscal("Comunidad de Huayllapampa, Quinua")
                .estado(EstadoContribuyente.ACTIVO)
                .fechaRegistro(LocalDate.now())
                .build();

        ContribuyenteTributario guardado = contribuyenteRepository.saveAndFlush(c);
        assertNotNull(guardado.getId());
        assertEquals(0L, guardado.getVersion(), "El versionado optimista inicial debe ser 0");

        // Enmascaramiento de datos personales
        String enmascarado = guardado.getNumeroDocumentoEnmascarado();
        assertTrue(enmascarado.contains("*"), "El documento de identidad debe estar protegido y enmascarado");
        assertEquals(doc.length(), enmascarado.length());

        // Búsqueda por documento único
        Optional<ContribuyenteTributario> encontrado = contribuyenteRepository.findByTipoDocumentoAndNumeroDocumento("DNI", doc);
        assertTrue(encontrado.isPresent());
        assertEquals("Alipio Quispe Morales", encontrado.get().getNombreRazonSocial());
    }

    @Test
    @Order(4)
    @DisplayName("4. Persistencia de Predio Tributario y Titularidad (Relación Contribuyente-Predio)")
    void testPersistenciaPredioYRelacion() {
        long ts = System.currentTimeMillis();

        ContribuyenteTributario contribuyente = contribuyenteRepository.save(ContribuyenteTributario.builder()
                .codigoContribuyente("CTB-REL-" + ts)
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("7%07d", (int) (Math.abs(ts) % 9000000 + 1000000)))
                .nombreRazonSocial("María Elena Cárdenas")
                .estado(EstadoContribuyente.ACTIVO)
                .build());

        PredioTributario predio = predioRepository.save(PredioTributario.builder()
                .codigoPredio("PRD-TST-" + ts)
                .tipoPredio(TipoPredio.URBANO)
                .direccion("Jr. Huamanga N° 240, Quinua")
                .sectorNombre("Centro Histórico")
                .areaTerreno(new BigDecimal("250.7500"))
                .areaConstruida(new BigDecimal("120.3000"))
                .unidadMedidaArea("M2")
                .estado(EstadoPredio.ACTIVO)
                .build());

        RelacionContribuyentePredio relacion = relacionRepository.save(RelacionContribuyentePredio.builder()
                .contribuyente(contribuyente)
                .predio(predio)
                .tipoRelacion(TipoRelacionContribuyentePredio.PROPIETARIO_UNICO)
                .porcentajeParticipacion(new BigDecimal("100.00"))
                .fechaInicio(LocalDate.now())
                .esPrincipal(true)
                .estado(EstadoRelacionContribuyentePredio.ACTIVO)
                .build());

        assertNotNull(relacion.getId());
        assertEquals(contribuyente.getId(), relacion.getContribuyente().getId());
        assertEquals(predio.getId(), relacion.getPredio().getId());
        assertEquals(new BigDecimal("100.00"), relacion.getPorcentajeParticipacion());
    }

    @Test
    @Order(5)
    @DisplayName("5. Servicio de Dashboard Tributario: Agregación real sin datos ficticios")
    void testDashboardTributarioService() {
        DashboardTributarioResponse dashboard = recaudacionTributariaService.obtenerDashboard();
        assertNotNull(dashboard);
        assertTrue(dashboard.getTotalConceptos() >= 5, "Deben existir al menos los 5 conceptos base de la migración V52");
        assertTrue(dashboard.getTotalPeriodos() >= 3, "Deben existir al menos los 3 períodos fiscales iniciales");
        assertNotNull(dashboard.getRecaudacionTotal());
        assertNotNull(dashboard.getPeriodoActivo());
    }

    @Test
    @Order(6)
    @DisplayName("6. Seguridad Web MVC: Rutas privadas protegidas contra usuarios anónimos")
    void testSeguridadRutasAnonimas() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria"))
                .andExpect(status().is3xxRedirection()); // Redirige a /login
    }

    @Test
    @Order(7)
    @DisplayName("7. Seguridad Web MVC: Acceso autorizado al Dashboard Tributario")
    void testAccesoDashboardAutorizado() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/dashboard"))
                .andExpect(model().attributeExists("dashboard", "moduloActivo"));
    }

    @Test
    @Order(8)
    @DisplayName("8. Rutas de Módulos Web: Visualización de Impuesto Predial y Obligaciones")
    void testAccesoRutasModulos() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/impuesto-predial")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/predial/list"));

        mockMvc.perform(get("/dashboard/tributaria/obligaciones")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/obligaciones/list"));
    }
}
