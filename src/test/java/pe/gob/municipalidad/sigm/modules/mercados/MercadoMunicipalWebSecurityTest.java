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
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.MercadoMunicipalService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MercadoMunicipalWebSecurityTest {

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

    @Autowired
    private MercadoMunicipalService mercadoService;

    private Cookie adminAuthCookie;
    private Cookie ciudadanoAuthCookie;
    private MercadoResponse mercadoExistente;

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
                .numeroDocumento("2222" + (System.currentTimeMillis() % 10000))
                .nombres("Admin")
                .apellidoPaterno("Mercados")
                .correo("admin.test" + System.currentTimeMillis() + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        Usuario adminUser = usuarioRepository.save(Usuario.builder()
                .username("admin.test." + System.currentTimeMillis())
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
                .permisos(Set.of(
                        "MERCADOS_MODULO_ACCEDER",
                        "MERCADOS_MERCADO_VER",
                        "MERCADOS_MERCADO_CREAR",
                        "MERCADOS_MERCADO_EDITAR",
                        "MERCADOS_MERCADO_CAMBIAR_ESTADO",
                        "MERCADOS_MERCADO_EXPORTAR",
                        "MERCADOS_MERCADO_DOCUMENTOS",
                        "MERCADOS_MERCADO_HISTORIAL_VER"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("MERCADOS_MODULO_ACCEDER"),
                        new SimpleGrantedAuthority("MERCADOS_MERCADO_VER"),
                        new SimpleGrantedAuthority("MERCADOS_MERCADO_CREAR"),
                        new SimpleGrantedAuthority("MERCADOS_MERCADO_EDITAR"),
                        new SimpleGrantedAuthority("MERCADOS_MERCADO_CAMBIAR_ESTADO"),
                        new SimpleGrantedAuthority("MERCADOS_MERCADO_EXPORTAR"),
                        new SimpleGrantedAuthority("MERCADOS_MERCADO_DOCUMENTOS"),
                        new SimpleGrantedAuthority("MERCADOS_MERCADO_HISTORIAL_VER")
                ))
                .build();

        String adminJwt = jwtTokenProvider.generateAccessToken(adminDetails);
        adminAuthCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, adminJwt);
        adminAuthCookie.setPath("/");

        Persona personaCiudadano = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("3333" + (System.currentTimeMillis() % 10000))
                .nombres("Vecino")
                .apellidoPaterno("Quinua")
                .correo("vecino.test" + System.currentTimeMillis() + "@gmail.com")
                .estado("ACTIVO")
                .build());

        Usuario ciudadanoUser = usuarioRepository.save(Usuario.builder()
                .username("vecino.test." + System.currentTimeMillis())
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

        mercadoExistente = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MER-WEB-01")
                .nombre("Mercado de Prueba Web")
                .direccion("Jr. Web 100")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .build());
    }

    @Test
    @DisplayName("25. Verificar permisos requeridos en endpoints de Mercados")
    void test25_verificarPermisosEnEndpoints() throws Exception {
        // Con usuario admin con permisos -> 200 OK
        mockMvc.perform(get("/api/mercados")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(get("/api/mercados/" + mercadoExistente.getId())
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mercadoExistente.getId().toString()));
    }

    @Test
    @DisplayName("26. Rechazar acceso no autorizado (403 Forbidden)")
    void test26_rechazarAccesoNoAutorizado() throws Exception {
        // Usuario ciudadano sin permisos intenta acceder a la API de mercados
        mockMvc.perform(get("/api/mercados")
                        .cookie(ciudadanoAuthCookie))
                .andExpect(status().isForbidden());

        // Usuario ciudadano intenta crear un mercado
        MercadoRequest req = MercadoRequest.builder()
                .codigo("MER-HACK")
                .nombre("Mercado No Autorizado")
                .direccion("Jr. Falso 123")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .build();

        mockMvc.perform(post("/api/mercados")
                        .with(csrf())
                        .cookie(ciudadanoAuthCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("27. Prevenir IDOR retornando 404 para mercado inexistente")
    void test27_prevenirIdorMercadoInexistente() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(get("/api/mercados/" + randomId)
                        .cookie(adminAuthCookie))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errorCode").value("ERR_RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("28. Protección CSRF en endpoints mutables")
    void test28_proteccionCsrfEnEndpointsMutables() throws Exception {
        MercadoRequest req = MercadoRequest.builder()
                .codigo("MER-NOCSRF")
                .nombre("Mercado Sin CSRF")
                .direccion("Jr. Alcala 10")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .build();

        // Sin with(csrf()) debe ser rechazado con 403 Forbidden por Spring Security CSRF Filter
        mockMvc.perform(post("/api/mercados")
                        .cookie(adminAuthCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("30. Renderizado Thymeleaf de vistas web de administración de mercados")
    void test30_renderizadoThymeleafVistas() throws Exception {
        // Vista listado
        mockMvc.perform(get("/dashboard/mercados/listado")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Padrón de Mercados Municipales")));

        // Vista nuevo mercado
        mockMvc.perform(get("/dashboard/mercados/nuevo")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Registro de Nuevo Mercado Municipal")));

        // Vista detalle del mercado
        mockMvc.perform(get("/dashboard/mercados/" + mercadoExistente.getId())
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Información General")))
                .andExpect(content().string(containsString(mercadoExistente.getNombre())));

        // Vista edición del mercado
        mockMvc.perform(get("/dashboard/mercados/" + mercadoExistente.getId() + "/editar")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Actualización de Mercado Municipal")));
    }
}
