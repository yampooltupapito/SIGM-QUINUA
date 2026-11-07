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
import pe.gob.municipalidad.sigm.modules.mercados.dto.MercadoRequest;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MercadosSecurityAndWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Cookie adminAuthCookie;
    private Cookie ciudadanoAuthCookie;

    @BeforeEach
    void setUp() {
        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder()
                        .codigo("ROLE_ADMIN")
                        .nombre("Administrador General")
                        .estado("ACTIVO")
                        .build()));

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder()
                        .codigo("ROLE_CIUDADANO")
                        .nombre("Ciudadano")
                        .estado("ACTIVO")
                        .build()));

        Persona personaAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("2121" + (System.currentTimeMillis() % 10000))
                .nombres("Admin")
                .apellidoPaterno("Mercados")
                .correo("admin.mercados@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        Usuario adminUser = usuarioRepository.save(Usuario.builder()
                .username("admin.mkt." + System.currentTimeMillis())
                .persona(personaAdmin)
                .passwordHash(passwordEncoder.encode("AdminPass2026!"))
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
                .permisos(Set.of("MERCADOS_MODULO_ACCEDER", "MERCADOS_MERCADO_VER", "MERCADOS_MERCADO_CREAR"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("MERCADOS_MODULO_ACCEDER"),
                        new SimpleGrantedAuthority("MERCADOS_MERCADO_VER"),
                        new SimpleGrantedAuthority("MERCADOS_MERCADO_CREAR")
                ))
                .build();

        String adminJwt = jwtTokenProvider.generateAccessToken(adminDetails);
        adminAuthCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, adminJwt);
        adminAuthCookie.setPath("/");

        Persona personaCiudadano = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("3131" + (System.currentTimeMillis() % 10000))
                .nombres("Vecino")
                .apellidoPaterno("Quinua")
                .correo("vecino.quinua@gmail.com")
                .estado("ACTIVO")
                .build());

        Usuario ciudadanoUser = usuarioRepository.save(Usuario.builder()
                .username("vecino." + System.currentTimeMillis())
                .persona(personaCiudadano)
                .passwordHash(passwordEncoder.encode("VecinoPass2026!"))
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
                .permisos(Set.of())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CIUDADANO")))
                .build();

        String ciudadanoJwt = jwtTokenProvider.generateAccessToken(ciudadanoDetails);
        ciudadanoAuthCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, ciudadanoJwt);
        ciudadanoAuthCookie.setPath("/");
    }

    @Test
    @DisplayName("19. Verificar permisos en endpoints y 20. Rechazar usuario no autenticado (401)")
    void testRechazarUsuarioNoAutenticado() throws Exception {
        mockMvc.perform(get("/api/mercados"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("20. Rechazar usuario no autorizado (403 Forbidden)")
    void testRechazarUsuarioSinPermisos() throws Exception {
        mockMvc.perform(get("/api/mercados")
                        .cookie(ciudadanoAuthCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("19. Permitir acceso a usuario con permisos (200 OK)")
    void testPermitirAccesoConPermisos() throws Exception {
        mockMvc.perform(get("/api/mercados")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("21. Prevenir IDOR: consulta de mercado inexistente retorna 404")
    void testPrevenirIdorMercadoInexistente() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(get("/api/mercados/" + randomId)
                        .cookie(adminAuthCookie))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Crear mercado vía POST /api/mercados con token y CSRF")
    void testCrearMercadoApi() throws Exception {
        MercadoRequest req = MercadoRequest.builder()
                .codigo("MKT-API-" + System.currentTimeMillis())
                .nombre("Mercado API Quinua")
                .direccion("Calle Real")
                .estado(EstadoMercado.ACTIVO)
                .build();

        mockMvc.perform(post("/api/mercados")
                        .with(csrf())
                        .cookie(adminAuthCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(req.getCodigo()))
                .andExpect(jsonPath("$.nombre").value("Mercado API Quinua"));
    }

    @Test
    @DisplayName("24. Verificar dashboard web /dashboard/mercados con Thymeleaf")
    void testVerificarDashboardWeb() throws Exception {
        mockMvc.perform(get("/dashboard/mercados")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Gestión de Mercados Municipales")))
                .andExpect(content().string(containsString("Panel Principal")))
                .andExpect(content().string(containsString("Módulo 2 Operativo")));
    }
}
