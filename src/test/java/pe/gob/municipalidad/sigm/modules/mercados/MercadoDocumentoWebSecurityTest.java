package pe.gob.municipalidad.sigm.modules.mercados;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ContratoCreateRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ContratoEstadoRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ContratoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.ContratoOcupacionService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MercadoDocumentoWebSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MercadoMunicipalRepository mercadoRepository;

    @Autowired
    private PuestoComercialRepository puestoRepository;

    @Autowired
    private ComercianteRepository comercianteRepository;

    @Autowired
    private AsignacionPuestoRepository asignacionRepository;

    @Autowired
    private ContratoOcupacionService contratoService;

    private String adminToken;
    private String ciudadanoToken;
    private MercadoMunicipal mercado;
    private PuestoComercial puesto;
    private Comerciante comerciante;
    private AsignacionPuesto asignacion;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("Administrador").estado("ACTIVO").build()));

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("Ciudadano").estado("ACTIVO").build()));

        Persona personaAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("48" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Admin")
                .apellidoPaterno("Mercados")
                .correo("admin" + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        Usuario adminUser = usuarioRepository.save(Usuario.builder()
                .username("admin_doc_" + ts)
                .persona(personaAdmin)
                .passwordHash(passwordEncoder.encode("AdminPass123!"))
                .enabled(true)
                .accountLocked(false)
                .roles(Set.of(rolAdmin))
                .build());

        CustomUserDetails adminDetails = CustomUserDetails.builder()
                .id(adminUser.getId())
                .username(adminUser.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN"))
                .permisos(Set.of(
                        "ROLE_ADMIN",
                        "MERCADOS_MODULO_ACCEDER",
                        "MERCADOS_CONTRATO_VER",
                        "MERCADOS_CONTRATO_CREAR",
                        "MERCADOS_CONTRATO_EDITAR",
                        "MERCADOS_CONTRATO_REVISAR",
                        "MERCADOS_CONTRATO_APROBAR",
                        "MERCADOS_CONTRATO_FORMALIZAR",
                        "MERCADOS_CONTRATO_SUSPENDER",
                        "MERCADOS_CONTRATO_TERMINAR",
                        "MERCADOS_AUTORIZACION_VER",
                        "MERCADOS_AUTORIZACION_CREAR",
                        "MERCADOS_AUTORIZACION_EVALUAR",
                        "MERCADOS_AUTORIZACION_APROBAR",
                        "MERCADOS_AUTORIZACION_EMITIR",
                        "MERCADOS_AUTORIZACION_SUSPENDER",
                        "MERCADOS_AUTORIZACION_REVOCAR",
                        "MERCADOS_DOCUMENTO_VERIFICAR",
                        "MERCADOS_DOCUMENTO_EXPORTAR"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("MERCADOS_MODULO_ACCEDER"),
                        new SimpleGrantedAuthority("MERCADOS_CONTRATO_VER"),
                        new SimpleGrantedAuthority("MERCADOS_CONTRATO_CREAR"),
                        new SimpleGrantedAuthority("MERCADOS_CONTRATO_EDITAR"),
                        new SimpleGrantedAuthority("MERCADOS_CONTRATO_REVISAR"),
                        new SimpleGrantedAuthority("MERCADOS_CONTRATO_APROBAR"),
                        new SimpleGrantedAuthority("MERCADOS_CONTRATO_FORMALIZAR"),
                        new SimpleGrantedAuthority("MERCADOS_CONTRATO_SUSPENDER"),
                        new SimpleGrantedAuthority("MERCADOS_CONTRATO_TERMINAR"),
                        new SimpleGrantedAuthority("MERCADOS_AUTORIZACION_VER"),
                        new SimpleGrantedAuthority("MERCADOS_AUTORIZACION_CREAR"),
                        new SimpleGrantedAuthority("MERCADOS_AUTORIZACION_EVALUAR"),
                        new SimpleGrantedAuthority("MERCADOS_AUTORIZACION_APROBAR"),
                        new SimpleGrantedAuthority("MERCADOS_AUTORIZACION_EMITIR"),
                        new SimpleGrantedAuthority("MERCADOS_AUTORIZACION_SUSPENDER"),
                        new SimpleGrantedAuthority("MERCADOS_AUTORIZACION_REVOCAR"),
                        new SimpleGrantedAuthority("MERCADOS_DOCUMENTO_VERIFICAR"),
                        new SimpleGrantedAuthority("MERCADOS_DOCUMENTO_EXPORTAR")
                ))
                .build();

        adminToken = jwtTokenProvider.generateAccessToken(adminDetails);

        Persona personaCiudadano = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("49" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Ciudadano")
                .apellidoPaterno("Comun")
                .correo("ciudadano" + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        Usuario ciudadanoUser = usuarioRepository.save(Usuario.builder()
                .username("ciudadano_" + ts)
                .persona(personaCiudadano)
                .passwordHash(passwordEncoder.encode("UserPass123!"))
                .enabled(true)
                .accountLocked(false)
                .roles(Set.of(rolCiudadano))
                .build());

        CustomUserDetails ciudadanoDetails = CustomUserDetails.builder()
                .id(ciudadanoUser.getId())
                .username(ciudadanoUser.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_CIUDADANO"))
                .permisos(Set.of("ROLE_CIUDADANO"))
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CIUDADANO")))
                .build();

        ciudadanoToken = jwtTokenProvider.generateAccessToken(ciudadanoDetails);

        mercado = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("MER-SEC-" + (ts % 10000))
                .nombre("Mercado Test Seguridad")
                .direccion("Quinua Centro")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .estado(EstadoMercado.ACTIVO)
                .build());

        puesto = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercado)
                .codigo("PUE-SEC-" + (ts % 10000))
                .numero("D-401")
                .areaMetrosCuadrados(new BigDecimal("12.00"))
                .estado(EstadoPuesto.DISPONIBLE)
                .build());

        comerciante = comercianteRepository.save(Comerciante.builder()
                .persona(personaAdmin)
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .codigoComerciante("COM-SEC-" + (ts % 10000))
                .estado(EstadoComerciante.ACTIVO)
                .fechaRegistro(LocalDate.now())
                .build());

        asignacion = asignacionRepository.save(AsignacionPuesto.builder()
                .comerciante(comerciante)
                .puesto(puesto)
                .tipoAsignacion(TipoAsignacion.TEMPORAL)
                .fechaSolicitud(LocalDate.now().minusDays(2))
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(12))
                .estado(EstadoAsignacion.VIGENTE)
                .build());
    }

    @Test
    @DisplayName("01. Ruta pública de verificación QR accesible sin autenticación")
    void test01_rutaPublicaVerificacionQr_sinAutenticacion() throws Exception {
        // Formalizar contrato para tener un token seguro válido
        ContratoCreateRequest req = ContratoCreateRequest.builder()
                .asignacionId(asignacion.getId())
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(6))
                .build();
        ContratoResponse c = contratoService.crearBorrador(req, "operador");
        contratoService.enviarARevision(c.getId(), "operador");
        contratoService.aprobar(c.getId(), ContratoEstadoRequest.builder().motivo("Ok").build(), "autoridad");
        ContratoResponse formalizado = contratoService.formalizar(c.getId(), ContratoEstadoRequest.builder().motivo("Firmado").build(), "autoridad");

        // Consulta pública Web (sin token, sin cookies) -> Debe ser 200 OK
        mockMvc.perform(get("/verificar/mercados/documento/" + formalizado.getCodigoSeguro()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("MUNICIPALIDAD DISTRITAL DE QUINUA")))
                .andExpect(content().string(containsString(formalizado.getCodigoContrato())));

        // Consulta pública API (sin token, sin cookies) -> Debe ser 200 OK
        mockMvc.perform(get("/api/mercados/documentos/verificar/" + formalizado.getCodigoSeguro()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esValido").value(true))
                .andExpect(jsonPath("$.codigo").value(formalizado.getCodigoContrato()));
    }

    @Test
    @DisplayName("02. Acceso autorizado a listar contratos con permiso MERCADOS_CONTRATO_VER")
    void test02_accesoAutorizadoListarContratos() throws Exception {
        mockMvc.perform(get("/api/mercados/contratos")
                        .header("Authorization", "Bearer " + adminToken)
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("03. Rechazar acceso con 403 Forbidden para usuario sin permiso MERCADOS_CONTRATO_VER")
    void test03_rechazarAccesoSinPermiso() throws Exception {
        mockMvc.perform(get("/api/mercados/contratos")
                        .header("Authorization", "Bearer " + ciudadanoToken)
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, ciudadanoToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("04. Protección CSRF: Rechazar POST a contratos sin token CSRF")
    void test04_proteccionCsrfRechazaPostSinToken() throws Exception {
        ContratoCreateRequest request = ContratoCreateRequest.builder()
                .asignacionId(asignacion.getId())
                .fechaInicio(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/mercados/contratos")
                        .header("Authorization", "Bearer " + adminToken)
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("05. Renderizado Web de Bandeja de Contratos")
    void test05_renderizadoWebBandejaContratos() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/contratos")
                        .header("Authorization", "Bearer " + adminToken)
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Contratos de Ocupación")));
    }

    @Test
    @DisplayName("06. Renderizado Web de Bandeja de Autorizaciones")
    void test06_renderizadoWebBandejaAutorizaciones() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/autorizaciones")
                        .header("Authorization", "Bearer " + adminToken)
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Autorizaciones Comerciales")));
    }

    @Test
    @DisplayName("07. Renderizado Web de Control de Vencimientos")
    void test07_renderizadoWebControlVencimientos() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/documentos/vencimientos")
                        .header("Authorization", "Bearer " + adminToken)
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Control de Vencimientos")));
    }
}
