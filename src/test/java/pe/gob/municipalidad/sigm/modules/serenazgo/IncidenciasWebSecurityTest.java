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
public class IncidenciasWebSecurityTest {

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

        // 1. Admin
        Persona pAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("87" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Admin").apellidoPaterno("Incidencias").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uAdmin = usuarioRepository.save(Usuario.builder()
                .persona(pAdmin).username("admin_inc_" + ts).passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(rolAdmin)).enabled(true).accountLocked(false).build());

        // 2. Sereno
        Persona pSereno = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("88" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Sereno").apellidoPaterno("Incidencias").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uSereno = usuarioRepository.save(Usuario.builder()
                .persona(pSereno).username("sereno_inc_" + ts).passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(rolSereno)).enabled(true).accountLocked(false).build());

        // 3. Ciudadano
        Persona pCiud = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("89" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Ciudadano").apellidoPaterno("Incidencias").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uCiud = usuarioRepository.save(Usuario.builder()
                .persona(pCiud).username("ciud_inc_" + ts).passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(rolCiudadano)).enabled(true).accountLocked(false).build());

        // CustomUserDetails y Tokens JWT
        CustomUserDetails cudAdmin = CustomUserDetails.builder()
                .id(uAdmin.getId()).username(uAdmin.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN"))
                .permisos(Set.of("ROLE_ADMIN", "SERENAZGO_INCIDENCIAS_VER", "SERENAZGO_INCIDENCIAS_CREAR", "SERENAZGO_INCIDENCIAS_EXPORTAR"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("SERENAZGO_INCIDENCIAS_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_INCIDENCIAS_CREAR"),
                        new SimpleGrantedAuthority("SERENAZGO_INCIDENCIAS_EXPORTAR")))
                .build();
        adminToken = jwtTokenProvider.generateAccessToken(cudAdmin);

        CustomUserDetails cudSereno = CustomUserDetails.builder()
                .id(uSereno.getId()).username(uSereno.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_SERENO"))
                .permisos(Set.of("ROLE_SERENO", "SERENAZGO_INCIDENCIAS_VER"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_SERENO"),
                        new SimpleGrantedAuthority("SERENAZGO_INCIDENCIAS_VER")))
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
    @DisplayName("Bandeja Web: Acceso anónimo redirige a login")
    void testAccesoAnonimoBandejaRedirigeLogin() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/incidencias"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Bandeja Web: Admin accede exitosamente (200 OK)")
    void testAdminAccedeBandeja() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/incidencias")
                        .with(user("admin_incidencias_test").authorities(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("SERENAZGO_INCIDENCIAS_VER"))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/incidencias/index"))
                .andExpect(model().attributeExists("incidencias", "kpis", "filtro"));
    }

    @Test
    @DisplayName("Bandeja Web: Sereno con permiso accede exitosamente (200 OK)")
    void testSerenoAccedeBandeja() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/incidencias")
                        .with(user("sereno_incidencias_test").authorities(new SimpleGrantedAuthority("ROLE_SERENO"), new SimpleGrantedAuthority("SERENAZGO_INCIDENCIAS_VER"))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/incidencias/index"));
    }

    @Test
    @DisplayName("Bandeja Web: Usuario sin permiso recibe 403 Forbidden")
    void testCiudadanoAccesoProhibidoBandeja() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/incidencias")
                        .with(user("ciudadano_incidencias_test").authorities(new SimpleGrantedAuthority("ROLE_CIUDADANO"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("API REST: Acceso con JWT válido retorna 200 OK")
    void testApiRestAccesoConJwtValido() throws Exception {
        mockMvc.perform(get("/api/serenazgo/incidencias")
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("API REST: Acceso con JWT sin permisos retorna 403 Forbidden")
    void testApiRestAccesoSinPermisosRetorna403() throws Exception {
        mockMvc.perform(get("/api/serenazgo/incidencias")
                        .header("Authorization", "Bearer " + ciudadanoToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("API REST KPIs: Retorna indicadores de seguridad ciudadana")
    void testApiRestKpis() throws Exception {
        mockMvc.perform(get("/api/serenazgo/incidencias/kpis")
                        .header("Authorization", "Bearer " + serenoToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncidencias").exists());
    }
}
