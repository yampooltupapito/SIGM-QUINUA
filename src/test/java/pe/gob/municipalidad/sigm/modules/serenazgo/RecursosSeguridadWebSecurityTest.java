package pe.gob.municipalidad.sigm.modules.serenazgo;

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
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class RecursosSeguridadWebSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String serenoToken;
    private String ciudadanoToken;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseThrow();
        Rol rolSereno = rolRepository.findByCodigo("ROLE_SERENO").orElseThrow();
        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseThrow();

        Persona pAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("88" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Admin").apellidoPaterno("Seguridad").apellidoMaterno("Recursos").estado("ACTIVO").build());
        Usuario uAdmin = usuarioRepository.save(Usuario.builder()
                .persona(pAdmin).username("admin_rec_" + ts).passwordHash(passwordEncoder.encode("123456"))
                .roles(Set.of(rolAdmin)).enabled(true).accountLocked(false).build());

        Persona pSereno = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("89" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Sereno").apellidoPaterno("Base").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uSereno = usuarioRepository.save(Usuario.builder()
                .persona(pSereno).username("sereno_rec_" + ts).passwordHash(passwordEncoder.encode("123456"))
                .roles(Set.of(rolSereno)).enabled(true).accountLocked(false).build());

        Persona pCiud = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("90" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Vecino").apellidoPaterno("Ciudadano").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uCiud = usuarioRepository.save(Usuario.builder()
                .persona(pCiud).username("ciud_rec_" + ts).passwordHash(passwordEncoder.encode("123456"))
                .roles(Set.of(rolCiudadano)).enabled(true).accountLocked(false).build());

        CustomUserDetails cudAdmin = CustomUserDetails.builder()
                .id(uAdmin.getId()).username(uAdmin.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN"))
                .permisos(Set.of("ROLE_ADMIN", "SERENAZGO_UNIDADES_VER", "SERENAZGO_UNIDADES_CREAR", "SERENAZGO_EQUIPAMIENTO_VER", "SERENAZGO_EQUIPAMIENTO_CREAR"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("SERENAZGO_UNIDADES_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_UNIDADES_CREAR"),
                        new SimpleGrantedAuthority("SERENAZGO_EQUIPAMIENTO_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_EQUIPAMIENTO_CREAR")))
                .build();
        adminToken = jwtTokenProvider.generateAccessToken(cudAdmin);

        CustomUserDetails cudSereno = CustomUserDetails.builder()
                .id(uSereno.getId()).username(uSereno.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_SERENO"))
                .permisos(Set.of("ROLE_SERENO", "SERENAZGO_UNIDADES_VER", "SERENAZGO_EQUIPAMIENTO_VER"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_SERENO"),
                        new SimpleGrantedAuthority("SERENAZGO_UNIDADES_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_EQUIPAMIENTO_VER")))
                .build();
        serenoToken = jwtTokenProvider.generateAccessToken(cudSereno);

        CustomUserDetails cudCiud = CustomUserDetails.builder()
                .id(uCiud.getId()).username(uCiud.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_CIUDADANO"))
                .permisos(Set.of())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CIUDADANO")))
                .build();
        ciudadanoToken = jwtTokenProvider.generateAccessToken(cudCiud);
    }

    @Test
    @DisplayName("01: Petición anónima a API de recursos rechazada con HTTP 401 Unauthorized")
    void test01_acceso_anonimo_rechazado() throws Exception {
        mockMvc.perform(get("/api/serenazgo/recursos/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("02: Administrador autorizado consulta métricas de flota y equipamiento (HTTP 200)")
    void test02_acceso_admin_dashboard_recursos() throws Exception {
        mockMvc.perform(get("/api/serenazgo/recursos/dashboard")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUnidades").isNumber())
                .andExpect(jsonPath("$.totalEquipos").isNumber());
    }

    @Test
    @DisplayName("03: Efectivo de serenazgo autorizado accede a la vista web de unidades")
    void test03_acceso_vista_unidades_sereno() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/unidades")
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, serenoToken)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/unidades/listado"))
                .andExpect(model().attributeExists("unidades"))
                .andExpect(model().attributeExists("dashboard"));
    }

    @Test
    @DisplayName("04: Efectivo de serenazgo autorizado accede a la vista web de equipamiento")
    void test04_acceso_vista_equipamiento_sereno() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/equipamiento")
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, serenoToken)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/equipamiento/listado"))
                .andExpect(model().attributeExists("equipos"))
                .andExpect(model().attributeExists("dashboard"));
    }

    @Test
    @DisplayName("05: Usuario ciudadano sin privilegios es bloqueado con HTTP 403 Forbidden")
    void test05_ciudadano_sin_permiso_rechazado() throws Exception {
        mockMvc.perform(get("/api/serenazgo/recursos/dashboard")
                        .header("Authorization", "Bearer " + ciudadanoToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
