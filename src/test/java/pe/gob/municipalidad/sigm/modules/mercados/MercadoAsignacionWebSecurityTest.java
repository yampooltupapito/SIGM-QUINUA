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
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.dto.AsignacionCreateRequest;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MercadoMunicipal;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PuestoComercial;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ComercianteRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.MercadoMunicipalRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.PuestoComercialRepository;
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
class MercadoAsignacionWebSecurityTest {

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

    private String adminToken;
    private String ciudadanoToken;

    private MercadoMunicipal mercado;
    private PuestoComercial puesto;
    private Comerciante comerciante;

    @BeforeEach
    void setUp() {
        long timestamp = System.currentTimeMillis();

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("Administrador").estado("ACTIVO").build()));

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("Ciudadano").estado("ACTIVO").build()));

        Persona personaAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("4700" + String.format("%04d", (int)(timestamp % 10000)))
                .nombres("Admin")
                .apellidoPaterno("Mercados")
                .correo("admin.mercados" + timestamp + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        Usuario adminUser = usuarioRepository.save(Usuario.builder()
                .username("admin_asig_" + timestamp)
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
                        "MERCADOS_ASIGNACION_VER",
                        "MERCADOS_ASIGNACION_GESTIONAR",
                        "MERCADOS_ASIGNACION_CREAR",
                        "MERCADOS_ASIGNACION_EVALUAR",
                        "MERCADOS_ASIGNACION_OBSERVAR",
                        "MERCADOS_ASIGNACION_APROBAR",
                        "MERCADOS_ASIGNACION_RECHAZAR",
                        "MERCADOS_ASIGNACION_ACTIVAR",
                        "MERCADOS_ASIGNACION_SUSPENDER",
                        "MERCADOS_ASIGNACION_FINALIZAR",
                        "MERCADOS_ASIGNACION_DEVOLUCION",
                        "MERCADOS_ASIGNACION_HISTORIAL_VER",
                        "MERCADOS_ASIGNACION_EXPORTAR"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("MERCADOS_MODULO_ACCEDER"),
                        new SimpleGrantedAuthority("MERCADOS_ASIGNACION_VER"),
                        new SimpleGrantedAuthority("MERCADOS_ASIGNACION_GESTIONAR"),
                        new SimpleGrantedAuthority("MERCADOS_ASIGNACION_CREAR"),
                        new SimpleGrantedAuthority("MERCADOS_ASIGNACION_EVALUAR"),
                        new SimpleGrantedAuthority("MERCADOS_ASIGNACION_OBSERVAR"),
                        new SimpleGrantedAuthority("MERCADOS_ASIGNACION_APROBAR"),
                        new SimpleGrantedAuthority("MERCADOS_ASIGNACION_RECHAZAR"),
                        new SimpleGrantedAuthority("MERCADOS_ASIGNACION_ACTIVAR"),
                        new SimpleGrantedAuthority("MERCADOS_ASIGNACION_SUSPENDER"),
                        new SimpleGrantedAuthority("MERCADOS_ASIGNACION_FINALIZAR"),
                        new SimpleGrantedAuthority("MERCADOS_ASIGNACION_DEVOLUCION"),
                        new SimpleGrantedAuthority("MERCADOS_ASIGNACION_HISTORIAL_VER"),
                        new SimpleGrantedAuthority("MERCADOS_ASIGNACION_EXPORTAR")
                ))
                .build();

        adminToken = jwtTokenProvider.generateAccessToken(adminDetails);

        Persona personaCiudadano = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("4799" + String.format("%04d", (int)(timestamp % 10000)))
                .nombres("Ciudadano")
                .apellidoPaterno("SinPermiso")
                .correo("ciudadano" + timestamp + "@gmail.com")
                .estado("ACTIVO")
                .build());

        Usuario ciudadanoUser = usuarioRepository.save(Usuario.builder()
                .username("ciudadano_" + timestamp)
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

        // Semilla base
        mercado = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("MER-SEC-" + (timestamp % 10000))
                .nombre("Mercado Test Seguridad")
                .direccion("Quinua")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .estado(EstadoMercado.ACTIVO)
                .build());

        puesto = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercado)
                .codigo("PUE-SEC-" + (timestamp % 10000))
                .numero("S-01")
                .areaMetrosCuadrados(new BigDecimal("15.00"))
                .estado(EstadoPuesto.DISPONIBLE)
                .build());

        comerciante = comercianteRepository.save(Comerciante.builder()
                .persona(personaAdmin)
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .codigoComerciante("COM-SEC-" + (timestamp % 10000))
                .estado(EstadoComerciante.ACTIVO)
                .fechaRegistro(LocalDate.now())
                .build());
    }

    @Test
    @DisplayName("01. Verificar permisos: Acceso autorizado a listar asignaciones")
    void test01_verificarPermisos_accesoAutorizado() throws Exception {
        mockMvc.perform(get("/api/mercados/asignaciones")
                        .header("Authorization", "Bearer " + adminToken)
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("02. Rechazar acceso no autorizado con 403 Forbidden para usuario sin permiso")
    void test02_rechazarAccesoNoAutorizado() throws Exception {
        mockMvc.perform(get("/api/mercados/asignaciones")
                        .header("Authorization", "Bearer " + ciudadanoToken)
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, ciudadanoToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("03. Prevenir IDOR: Devolver 404 al consultar asignación inexistente")
    void test03_prevenirIdor_asignacionInexistente() throws Exception {
        UUID inexistente = UUID.randomUUID();
        mockMvc.perform(get("/api/mercados/asignaciones/" + inexistente)
                        .header("Authorization", "Bearer " + adminToken)
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("04. Protección CSRF: Rechazar mutación POST sin token CSRF")
    void test04_proteccionCsrf_rechazaMutacionSinToken() throws Exception {
        AsignacionCreateRequest request = AsignacionCreateRequest.builder()
                .puestoId(puesto.getId())
                .comercianteId(comerciante.getId())
                .fechaInicio(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/mercados/asignaciones")
                        .header("Authorization", "Bearer " + adminToken)
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("05. Renderizado Thymeleaf: Listado de asignaciones con layout institucional")
    void test05_renderizarThymeleaf_listadoAsignaciones() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/asignaciones")
                        .header("Authorization", "Bearer " + adminToken)
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Asignación de Puestos y Control de Ocupación")));
    }

    @Test
    @DisplayName("06. Renderizado Thymeleaf: Formulario de nueva solicitud de asignación")
    void test06_renderizarThymeleaf_formularioNueva() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/asignaciones/nueva")
                        .header("Authorization", "Bearer " + adminToken)
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Registro de Nueva Solicitud de Asignación")));
    }

    @Test
    @DisplayName("07. Crear solicitud de asignación vía API con CSRF válido")
    void test07_crearSolicitudViaApi_exitoso() throws Exception {
        AsignacionCreateRequest request = AsignacionCreateRequest.builder()
                .puestoId(puesto.getId())
                .comercianteId(comerciante.getId())
                .fechaInicio(LocalDate.now().plusDays(2))
                .tipoAsignacion(TipoAsignacion.REGULAR)
                .motivo("Trámite formal de solicitud de puesto comercial")
                .presentarInmediatamente(true)
                .build();

        mockMvc.perform(post("/api/mercados/asignaciones")
                        .with(csrf())
                        .header("Authorization", "Bearer " + adminToken)
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoAsignacion").exists())
                .andExpect(jsonPath("$.estado").value("SOLICITADA"));
    }
}
