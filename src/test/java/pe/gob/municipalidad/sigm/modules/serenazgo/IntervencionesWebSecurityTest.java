package pe.gob.municipalidad.sigm.modules.serenazgo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
public class IntervencionesWebSecurityTest {

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
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private String adminToken;
    private String serenoToken;
    private String ciudadanoToken;

    private CustomUserDetails cudAdmin;
    private CustomUserDetails cudSereno;
    private CustomUserDetails cudCiudadano;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseThrow();
        Rol rolSereno = rolRepository.findByCodigo("ROLE_SERENO").orElseThrow();
        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseThrow();

        // 1. Admin
        Persona pAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("77" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Admin").apellidoPaterno("Intervenciones").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uAdmin = usuarioRepository.save(Usuario.builder()
                .persona(pAdmin).username("admin_int_" + ts).passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(rolAdmin)).enabled(true).accountLocked(false).build());

        // 2. Sereno
        Persona pSereno = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("78" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Sereno").apellidoPaterno("Intervenciones").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uSereno = usuarioRepository.save(Usuario.builder()
                .persona(pSereno).username("sereno_int_" + ts).passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(rolSereno)).enabled(true).accountLocked(false).build());

        // 3. Ciudadano
        Persona pCiud = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("79" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Ciudadano").apellidoPaterno("Intervenciones").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uCiud = usuarioRepository.save(Usuario.builder()
                .persona(pCiud).username("ciud_int_" + ts).passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(rolCiudadano)).enabled(true).accountLocked(false).build());

        // CustomUserDetails
        cudAdmin = CustomUserDetails.builder()
                .id(uAdmin.getId()).username(uAdmin.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN"))
                .permisos(Set.of("ROLE_ADMIN", "SERENAZGO_INTERVENCIONES_VER", "SERENAZGO_INTERVENCIONES_CREAR", "SERENAZGO_INTERVENCIONES_EXPORTAR"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("SERENAZGO_INTERVENCIONES_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_INTERVENCIONES_CREAR"),
                        new SimpleGrantedAuthority("SERENAZGO_INTERVENCIONES_EXPORTAR")))
                .build();

        cudSereno = CustomUserDetails.builder()
                .id(uSereno.getId()).username(uSereno.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_SERENO"))
                .permisos(Set.of("ROLE_SERENO", "SERENAZGO_INTERVENCIONES_VER", "SERENAZGO_INTERVENCIONES_CREAR"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_SERENO"),
                        new SimpleGrantedAuthority("SERENAZGO_INTERVENCIONES_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_INTERVENCIONES_CREAR")))
                .build();

        cudCiudadano = CustomUserDetails.builder()
                .id(uCiud.getId()).username(uCiud.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_CIUDADANO"))
                .permisos(Set.of("ROLE_CIUDADANO"))
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CIUDADANO")))
                .build();

        adminToken = jwtTokenProvider.generateAccessToken(cudAdmin);
        serenoToken = jwtTokenProvider.generateAccessToken(cudSereno);
        ciudadanoToken = jwtTokenProvider.generateAccessToken(cudCiudadano);
    }

    @Test
    @DisplayName("GET /dashboard/serenazgo/intervenciones sin autenticación redirige a login")
    void testBandejaIntervenciones_SinAutenticacion_RedirigeLogin() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/intervenciones"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("GET /dashboard/serenazgo/intervenciones con rol ADMIN accede con 200 OK")
    void testBandejaIntervenciones_ConRolAdmin_AccesoPermitido() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/intervenciones").with(user(cudAdmin)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/intervenciones/index"))
                .andExpect(model().attributeExists("intervenciones"))
                .andExpect(model().attributeExists("kpis"));
    }

    @Test
    @DisplayName("GET /dashboard/serenazgo/intervenciones con rol SERENO accede con 200 OK")
    void testBandejaIntervenciones_ConRolSereno_AccesoPermitido() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/intervenciones").with(user(cudSereno)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/intervenciones/index"));
    }

    @Test
    @DisplayName("GET /dashboard/serenazgo/intervenciones con rol CIUDADANO retorna 403 Forbidden")
    void testBandejaIntervenciones_ConRolCiudadano_Forbidden() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/intervenciones").with(user(cudCiudadano)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /dashboard/serenazgo/intervenciones/nueva con rol ADMIN accede con 200 OK")
    void testNuevaIntervencion_ConRolAdmin_AccesoPermitido() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/intervenciones/nueva").with(user(cudAdmin)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/intervenciones/nueva"))
                .andExpect(model().attributeExists("nuevaIntervencion"));
    }

    @Test
    @DisplayName("GET /dashboard/serenazgo/intervenciones/exportar/pdf con permiso exportar retorna 200 OK y PDF")
    void testExportarPdf_ConPermiso_RetornaPdf() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/intervenciones/exportar/pdf").with(user(cudAdmin)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    @DisplayName("API REST GET /api/v1/serenazgo/intervenciones con JWT de Sereno retorna 200 OK")
    void testApiRestListar_ConTokenSereno_Retorna200() throws Exception {
        mockMvc.perform(get("/api/v1/serenazgo/intervenciones")
                        .header("Authorization", "Bearer " + serenoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("API REST GET /api/v1/serenazgo/intervenciones con JWT de Ciudadano retorna 403 Forbidden")
    void testApiRestListar_ConTokenCiudadano_Retorna403() throws Exception {
        mockMvc.perform(get("/api/v1/serenazgo/intervenciones")
                        .header("Authorization", "Bearer " + ciudadanoToken))
                .andExpect(status().isForbidden());
    }
}
