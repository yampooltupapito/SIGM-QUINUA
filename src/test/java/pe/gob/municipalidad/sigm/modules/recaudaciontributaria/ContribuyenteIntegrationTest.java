package pe.gob.municipalidad.sigm.modules.recaudaciontributaria;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ContribuyenteTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ContribuyenteExportService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ContribuyenteService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

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
class ContribuyenteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ContribuyenteService contribuyenteService;

    @Autowired
    private ContribuyenteExportService exportService;

    @Autowired
    private ContribuyenteTributarioRepository contribuyenteRepository;

    private Cookie adminAuthCookie;

    @BeforeEach
    void setUp() {
        CustomUserDetails adminDetails = CustomUserDetails.builder()
                .id(101L)
                .username("admin.contribuyentes")
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN"))
                .permisos(Set.of(
                        "TRIBUTARIA_CONTRIBUYENTES_VER",
                        "TRIBUTARIA_CONTRIBUYENTES_CREAR",
                        "TRIBUTARIA_CONTRIBUYENTES_EDITAR",
                        "TRIBUTARIA_CONTRIBUYENTES_CAMBIAR_ESTADO",
                        "TRIBUTARIA_CONTRIBUYENTES_GESTIONAR_DOMICILIOS",
                        "TRIBUTARIA_CONTRIBUYENTES_GESTIONAR_CONTACTOS",
                        "TRIBUTARIA_CONTRIBUYENTES_GESTIONAR_REPRESENTANTES",
                        "TRIBUTARIA_CONTRIBUYENTES_EXPORTAR"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONTRIBUYENTES_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONTRIBUYENTES_CREAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONTRIBUYENTES_EDITAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONTRIBUYENTES_CAMBIAR_ESTADO"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONTRIBUYENTES_GESTIONAR_DOMICILIOS"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONTRIBUYENTES_GESTIONAR_CONTACTOS"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONTRIBUYENTES_GESTIONAR_REPRESENTANTES"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONTRIBUYENTES_EXPORTAR")
                ))
                .build();

        String jwt = jwtTokenProvider.generateAccessToken(adminDetails);
        adminAuthCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, jwt);
        adminAuthCookie.setPath("/");
    }

    @Test
    @Order(1)
    @DisplayName("1. Registro de Persona Natural con DNI, cálculo de nombre y generación de código único")
    void testRegistrarPersonaNatural() {
        ContribuyenteCreateRequest req = ContribuyenteCreateRequest.builder()
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento("45892134")
                .nombres("WALTER")
                .primerApellido("MENDOZA")
                .segundoApellido("FLORES")
                .telefono("966554433")
                .correo("walter.mendoza@quinua.pe")
                .comunidadCentroPoblado("Suso")
                .nombreVia("Jr. Los Artesanos")
                .numeroMunicipal("120")
                .build();

        ContribuyenteDetailResponse resp = contribuyenteService.registrarContribuyente(req, "admin.test");

        assertNotNull(resp.getId());
        assertNotNull(resp.getCodigoContribuyente());
        assertTrue(resp.getCodigoContribuyente().startsWith("CT-"), "El código debe tener el prefijo CT-");
        assertEquals("MENDOZA FLORES, WALTER", resp.getNombreRazonSocial());
        assertEquals("45****34", resp.getNumeroDocumentoEnmascarado());
        assertEquals(EstadoContribuyente.ACTIVO, resp.getEstado());
        assertEquals(EstadoVerificacionDocumento.VALIDADO_FORMATO, resp.getEstadoVerificacion());

        // Verificar que se crearon los domicilios, contactos e historial inicial
        assertFalse(resp.getDomicilios().isEmpty(), "Debe registrarse el domicilio inicial");
        assertEquals("Ayacucho", resp.getDomicilios().get(0).getDepartamento());
        assertFalse(resp.getContactos().isEmpty(), "Debe registrarse el contacto inicial");
        assertFalse(resp.getHistorialEstados().isEmpty(), "Debe crearse el registro de auditoría inicial");
    }

    @Test
    @Order(2)
    @DisplayName("2. Registro de Persona Jurídica con RUC oficial y algoritmo Módulo 11")
    void testRegistrarPersonaJuridica() {
        ContribuyenteCreateRequest req = ContribuyenteCreateRequest.builder()
                .tipoContribuyente(TipoContribuyente.PERSONA_JURIDICA)
                .tipoDocumento("RUC")
                .numeroDocumento("20131312955") // RUC válido con checksum
                .nombreRazonSocial("ASOCIACION DE ARTESANOS CERAMISTAS DE QUINUA")
                .nombreComercial("CERAMICAS QUINUA")
                .representanteLegalNombre("JUAN CARLOS QUISPE")
                .telefono("966987654")
                .correo("artesanos.quinua@gmail.com")
                .domicilioFiscal("Plaza de Armas S/N, Quinua")
                .build();

        ContribuyenteDetailResponse resp = contribuyenteService.registrarContribuyente(req, "admin.test");

        assertNotNull(resp.getId());
        assertEquals("ASOCIACION DE ARTESANOS CERAMISTAS DE QUINUA", resp.getNombreRazonSocial());
        assertEquals(TipoContribuyente.PERSONA_JURIDICA, resp.getTipoContribuyente());
        assertEquals("20*******55", resp.getNumeroDocumentoEnmascarado());
    }

    @Test
    @Order(3)
    @DisplayName("3. Detección y rechazo de duplicados de documento")
    void testRechazoDuplicado() {
        ContribuyenteCreateRequest req1 = ContribuyenteCreateRequest.builder()
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento("44332211")
                .nombres("PEDRO")
                .primerApellido("SUAREZ")
                .build();
        contribuyenteService.registrarContribuyente(req1, "admin.test");

        // Intentar registrar el mismo documento
        ContribuyenteCreateRequest req2 = ContribuyenteCreateRequest.builder()
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento("44332211")
                .nombres("PEDRO SEGUNDO")
                .primerApellido("SUAREZ")
                .build();

        assertThrows(DuplicateResourceException.class, () -> contribuyenteService.registrarContribuyente(req2, "admin.test"));
    }

    @Test
    @Order(4)
    @DisplayName("4. Gestión de sub-recursos: Domicilios, Contactos y Representantes Legales")
    void testGestionSubRecursos() {
        ContribuyenteCreateRequest req = ContribuyenteCreateRequest.builder()
                .tipoContribuyente(TipoContribuyente.PERSONA_JURIDICA)
                .tipoDocumento("RUC")
                .numeroDocumento("20601234567") // Formato RUC
                .nombreRazonSocial("EMPRESA TURISTICA PAMPA DE AYACUCHO SAC")
                .build();

        // En caso que el checksum falle con un número inventado, usemos un RUC con checksum válido
        // RUC SUNAT 20131312955 ya se usó, probemos otro válido o persona natural con DNI
        ContribuyenteCreateRequest reqNatural = ContribuyenteCreateRequest.builder()
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento("77889900")
                .nombres("MARIA")
                .primerApellido("HUAMAN")
                .build();

        ContribuyenteDetailResponse creado = contribuyenteService.registrarContribuyente(reqNatural, "admin.test");
        UUID contribId = creado.getId();

        // 1. Agregar Domicilio Rural
        DomicilioContribuyenteRequest domReq = DomicilioContribuyenteRequest.builder()
                .tipoDomicilio(TipoDomicilioContribuyente.DECLARADO)
                .comunidadCentroPoblado("Comunidad Campesina Muruncancha")
                .referencia("Sector Pampa Baja, lote 12")
                .esFiscal(false)
                .esNotificacion(true)
                .build();

        DomicilioResponse domResp = contribuyenteService.agregarDomicilio(contribId, domReq, "admin.test");
        assertNotNull(domResp.getId());
        assertTrue(domResp.getDireccionCompleta().contains("Muruncancha"));

        // 2. Agregar Contacto Alternativo
        ContactoContribuyenteRequest contReq = ContactoContribuyenteRequest.builder()
                .tipoContacto(TipoContactoContribuyente.TELEFONO_MOVIL)
                .valor("966112233")
                .descripcion("Celular de emergencias")
                .autorizadoNotificacion(true)
                .build();

        ContactoResponse contResp = contribuyenteService.agregarContacto(contribId, contReq, "admin.test");
        assertNotNull(contResp.getId());

        // 3. Agregar Representante / Apoderado
        RepresentanteContribuyenteRequest repReq = RepresentanteContribuyenteRequest.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("10203040")
                .nombresApellidos("ALBERTO HUAMAN QUISPE")
                .tipoRepresentacion(TipoRepresentacionLegal.APODERADO_LEGAL)
                .fechaInicio(LocalDate.now())
                .partidaRegistral("SUNARP-112233")
                .build();

        RepresentanteResponse repResp = contribuyenteService.agregarRepresentante(contribId, repReq, "admin.test");
        assertNotNull(repResp.getId());
        assertEquals("ALBERTO HUAMAN QUISPE", repResp.getNombresApellidos());

        // Verificar el detalle consolidado
        ContribuyenteDetailResponse detalle = contribuyenteService.obtenerDetalle(contribId);
        assertFalse(detalle.getDomicilios().isEmpty());
        assertFalse(detalle.getContactos().isEmpty());
        assertFalse(detalle.getRepresentantes().isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("5. Cambio de Estado con justificación administrativa e Historial de Auditoría")
    void testCambioEstadoYAuditoria() {
        ContribuyenteCreateRequest req = ContribuyenteCreateRequest.builder()
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento("32145678")
                .nombres("CARLOS")
                .primerApellido("QUISPE")
                .build();

        ContribuyenteDetailResponse creado = contribuyenteService.registrarContribuyente(req, "admin.test");

        CambioEstadoContribuyenteRequest cambio = CambioEstadoContribuyenteRequest.builder()
                .nuevoEstado(EstadoContribuyente.OBSERVADO)
                .motivo("Observación registral: No coincide con dirección catastral declarada en campo")
                .resolucionSustento("INF-Rentas-023-2026")
                .build();

        ContribuyenteDetailResponse actualizado = contribuyenteService.cambiarEstado(creado.getId(), cambio, "fiscalizador.test");

        assertEquals(EstadoContribuyente.OBSERVADO, actualizado.getEstado());
        assertEquals("Observación registral: No coincide con dirección catastral declarada en campo", actualizado.getHistorialEstados().get(0).getMotivo());
    }

    @Test
    @Order(6)
    @DisplayName("6. Exportación autorizada a formatos Excel y PDF")
    void testExportacionPadrón() {
        ContribuyenteFilterDto filtro = ContribuyenteFilterDto.builder().build();

        byte[] excelBytes = exportService.exportarExcel(filtro, true);
        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 500, "El archivo Excel generado debe contener bytes de estructura válidos");

        byte[] pdfBytes = exportService.exportarPdf(filtro, true);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500, "El archivo PDF generado debe contener bytes de documento válidos");
    }

    @Test
    @Order(7)
    @DisplayName("7. Seguridad Web MVC: Rutas privadas protegidas contra acceso anónimo")
    void testSeguridadAnonima() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/contribuyentes"))
                .andExpect(status().is3xxRedirection()); // Redirección a /login
    }

    @Test
    @Order(8)
    @DisplayName("8. Seguridad Web MVC: Acceso autorizado al Padrón y Ficha con JWT")
    void testAccesoAutorizadoMVC() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/contribuyentes")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/contribuyentes/list"))
                .andExpect(model().attributeExists("contribuyentes", "stats", "filtro"));

        mockMvc.perform(get("/dashboard/tributaria/contribuyentes/nuevo")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/contribuyentes/form"))
                .andExpect(model().attributeExists("request", "tiposContribuyente"));
    }

    @Test
    @Order(9)
    @DisplayName("9. API REST: Endpoint de verificación de duplicados vía AJAX")
    void testApiRestVerificarDuplicados() throws Exception {
        mockMvc.perform(post("/api/v1/tributaria/contribuyentes/verificar-duplicado")
                        .with(csrf())
                        .param("tipoDocumento", "DNI")
                        .param("numeroDocumento", "99999999")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.existeDuplicadoExacto").value(false));
    }
}
