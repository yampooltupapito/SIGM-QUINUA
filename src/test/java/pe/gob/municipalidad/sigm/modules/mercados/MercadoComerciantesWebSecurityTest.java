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
import pe.gob.municipalidad.sigm.modules.mercados.dto.ComercianteNaturalCreateRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ComercianteResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ComercianteRepository;
import pe.gob.municipalidad.sigm.modules.mercados.service.ComercianteService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MercadoComerciantesWebSecurityTest {

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
    private ComercianteRepository comercianteRepository;

    @Autowired
    private ComercianteService comercianteService;

    private Comerciante comerciante1;
    private Cookie adminCookie;
    private Cookie sinPermisoCookie;

    @BeforeEach
    void setUp() {
        // Rol ADMIN
        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder()
                        .codigo("ROLE_ADMIN")
                        .nombre("Administrador")
                        .estado("ACTIVO")
                        .build())
        );

        // Rol CIUDADANO
        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder()
                        .codigo("ROLE_CIUDADANO")
                        .nombre("Ciudadano")
                        .estado("ACTIVO")
                        .build())
        );

        // Usuario Admin
        Persona personaAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("4455" + (System.currentTimeMillis() % 10000))
                .nombres("Admin")
                .apellidoPaterno("Comerciantes")
                .correo("admin.comerciantes" + System.currentTimeMillis() + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        Usuario adminUser = usuarioRepository.save(Usuario.builder()
                .username("admin_comerciantes_" + System.currentTimeMillis())
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
                        "MERCADOS_COMERCIANTE_VER",
                        "MERCADOS_COMERCIANTE_GESTIONAR",
                        "MERCADOS_COMERCIANTE_CREAR",
                        "MERCADOS_COMERCIANTE_EDITAR",
                        "MERCADOS_COMERCIANTE_CAMBIAR_ESTADO",
                        "MERCADOS_COMERCIANTE_DOCUMENTOS",
                        "MERCADOS_COMERCIANTE_DATOS_SENSIBLES",
                        "MERCADOS_COMERCIANTE_EXPORTAR",
                        "MERCADOS_COMERCIANTE_HISTORIAL_VER"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("MERCADOS_MODULO_ACCEDER"),
                        new SimpleGrantedAuthority("MERCADOS_COMERCIANTE_VER"),
                        new SimpleGrantedAuthority("MERCADOS_COMERCIANTE_GESTIONAR"),
                        new SimpleGrantedAuthority("MERCADOS_COMERCIANTE_CREAR"),
                        new SimpleGrantedAuthority("MERCADOS_COMERCIANTE_EDITAR"),
                        new SimpleGrantedAuthority("MERCADOS_COMERCIANTE_CAMBIAR_ESTADO"),
                        new SimpleGrantedAuthority("MERCADOS_COMERCIANTE_DOCUMENTOS"),
                        new SimpleGrantedAuthority("MERCADOS_COMERCIANTE_DATOS_SENSIBLES"),
                        new SimpleGrantedAuthority("MERCADOS_COMERCIANTE_EXPORTAR"),
                        new SimpleGrantedAuthority("MERCADOS_COMERCIANTE_HISTORIAL_VER")
                ))
                .build();

        String adminToken = jwtTokenProvider.generateAccessToken(adminDetails);
        adminCookie = new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken);

        // Usuario Ciudadano sin permisos
        Persona personaCiudadano = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("7788" + (System.currentTimeMillis() % 10000))
                .nombres("Vecino")
                .apellidoPaterno("Quinua")
                .correo("vecino" + System.currentTimeMillis() + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        Usuario ciudadanoUser = usuarioRepository.save(Usuario.builder()
                .username("ciudadano_" + System.currentTimeMillis())
                .persona(personaCiudadano)
                .passwordHash(passwordEncoder.encode("CiudadanoPass123!"))
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

        String ciudadanoToken = jwtTokenProvider.generateAccessToken(ciudadanoDetails);
        sinPermisoCookie = new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, ciudadanoToken);

        // Crear Comerciante de prueba
        Persona titular = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("45678901")
                .nombres("Bartolomé")
                .apellidoPaterno("Huamán")
                .estado("ACTIVO")
                .build());

        comerciante1 = comercianteRepository.save(Comerciante.builder()
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .persona(titular)
                .codigoComerciante("COM-SEC-01")
                .version(0L)
                .build());
    }

    @Test
    @DisplayName("29. Enmascarar datos sensibles para usuarios sin permiso especial")
    void test29_enmascararDatosSensibles() {
        // Consulta sin permisos de datos sensibles: el DNI debe enmascararse
        ComercianteResponse resp = comercianteService.obtenerPorId(comerciante1.getId());
        assertThat(resp.getDocumentoEnmascarado()).isEqualTo("****8901");
    }

    @Test
    @DisplayName("30. Verificar permisos con JWT de operador autorizado (HTTP 200)")
    void test30_verificarPermisosAutorizado() throws Exception {
        mockMvc.perform(get("/api/mercados/comerciantes")
                        .cookie(adminCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("31. Rechazar acceso no autorizado a usuario sin permisos (HTTP 403 Forbidden)")
    void test31_rechazarAccesoNoAutorizado() throws Exception {
        mockMvc.perform(get("/api/mercados/comerciantes")
                        .cookie(sinPermisoCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("32. Prevenir IDOR al consultar comerciante inexistente (HTTP 404 Not Found)")
    void test32_prevenirIdor() throws Exception {
        UUID inexistente = UUID.randomUUID();
        mockMvc.perform(get("/api/mercados/comerciantes/" + inexistente)
                        .cookie(adminCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("33. Protección CSRF: rechazar peticiones POST sin token CSRF")
    void test33_proteccionCsrfRechazaSinToken() throws Exception {
        ComercianteNaturalCreateRequest req = ComercianteNaturalCreateRequest.builder()
                .numeroDocumento("77665544")
                .nombres("Prueba")
                .apellidoPaterno("CSRF")
                .build();

        mockMvc.perform(post("/api/mercados/comerciantes/natural")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("35. Renderizado Thymeleaf de vistas web (HTTP 200 OK)")
    void test35_renderizadoThymeleafVistas() throws Exception {
        // Vista listado
        mockMvc.perform(get("/dashboard/mercados/comerciantes")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Padrón Oficial de Comerciantes")));

        // Vista nuevo
        mockMvc.perform(get("/dashboard/mercados/comerciantes/nuevo")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Empadronamiento de Nuevo Comerciante")));

        // Vista detalle
        mockMvc.perform(get("/dashboard/mercados/comerciantes/" + comerciante1.getId())
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bartolomé Huamán")));
    }
}
