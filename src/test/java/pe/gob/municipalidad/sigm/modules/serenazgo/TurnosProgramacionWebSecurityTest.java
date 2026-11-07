package pe.gob.municipalidad.sigm.modules.serenazgo;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TurnosProgramacionWebSecurityTest {

    @Autowired
    private MockMvc mockMvc;

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
                .tipoDocumento("DNI").numeroDocumento("81" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Admin").apellidoPaterno("Turnos").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uAdmin = usuarioRepository.save(Usuario.builder()
                .persona(pAdmin).username("admin_tur_" + ts).passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(rolAdmin)).enabled(true).accountLocked(false).build());

        Persona pSereno = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("82" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Sereno").apellidoPaterno("Guardia").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uSereno = usuarioRepository.save(Usuario.builder()
                .persona(pSereno).username("sereno_tur_" + ts).passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(rolSereno)).enabled(true).accountLocked(false).build());

        Persona pCiud = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("83" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Ciudadano").apellidoPaterno("SinAcceso").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uCiud = usuarioRepository.save(Usuario.builder()
                .persona(pCiud).username("ciud_tur_" + ts).passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(rolCiudadano)).enabled(true).accountLocked(false).build());

        CustomUserDetails cudAdmin = CustomUserDetails.builder()
                .id(uAdmin.getId()).username(uAdmin.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN"))
                .permisos(Set.of("ROLE_ADMIN", "SERENAZGO_PROGRAMACION_VER", "SERENAZGO_PROGRAMACION_CREAR", "SERENAZGO_TURNOS_VER", "SERENAZGO_TURNOS_CREAR"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("SERENAZGO_PROGRAMACION_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_PROGRAMACION_CREAR"),
                        new SimpleGrantedAuthority("SERENAZGO_TURNOS_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_TURNOS_CREAR")))
                .build();
        adminToken = jwtTokenProvider.generateAccessToken(cudAdmin);

        CustomUserDetails cudSereno = CustomUserDetails.builder()
                .id(uSereno.getId()).username(uSereno.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_SERENO"))
                .permisos(Set.of("ROLE_SERENO", "SERENAZGO_PROGRAMACION_VER", "SERENAZGO_TURNOS_VER"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_SERENO"),
                        new SimpleGrantedAuthority("SERENAZGO_PROGRAMACION_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_TURNOS_VER")))
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
    @DisplayName("01: Acceso anónimo a dashboard de programación redirige a login")
    void test01_accesoAnonimoDashboardRedirigeLogin() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/programacion"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("02: Acceso anónimo a API REST de calendario devuelve 401 Unauthorized")
    void test02_accesoAnonimoRestDevuelve401() throws Exception {
        mockMvc.perform(get("/api/serenazgo/programacion/calendario")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("03: Usuario autorizado accede a dashboard de programación")
    void test03_usuarioAutorizadoDashboardExitoso() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/programacion")
                        .with(user("admin").authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN"),
                                new SimpleGrantedAuthority("SERENAZGO_PROGRAMACION_VER"))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/programacion/index"))
                .andExpect(model().attributeExists("kpis"))
                .andExpect(model().attributeExists("programaciones"));
    }

    @Test
    @DisplayName("04: Sereno con JWT autorizado accede a eventos de calendario")
    void test04_serenoConJwtAccedeCalendario() throws Exception {
        mockMvc.perform(get("/api/serenazgo/programacion/calendario")
                        .header("Authorization", "Bearer " + serenoToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("05: Ciudadano sin permisos accede a API de calendario y recibe 403 Forbidden")
    void test05_ciudadanoSinPermisosRecibe403() throws Exception {
        mockMvc.perform(get("/api/serenazgo/programacion/calendario")
                        .header("Authorization", "Bearer " + ciudadanoToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
