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
public class AtencionesWebSecurityTest {

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

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseThrow();
        Rol rolSereno = rolRepository.findByCodigo("ROLE_SERENO").orElseThrow();
        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseThrow();

        // 1. Admin
        Persona pAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("87" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Admin").apellidoPaterno("Atenciones").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uAdmin = usuarioRepository.save(Usuario.builder()
                .persona(pAdmin).username("admin_atn_" + ts).passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(rolAdmin)).enabled(true).accountLocked(false).build());

        // 2. Sereno
        Persona pSereno = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("88" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Sereno").apellidoPaterno("Atenciones").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uSereno = usuarioRepository.save(Usuario.builder()
                .persona(pSereno).username("sereno_atn_" + ts).passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(rolSereno)).enabled(true).accountLocked(false).build());

        // 3. Ciudadano
        Persona pCiud = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("89" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Ciudadano").apellidoPaterno("Atenciones").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uCiud = usuarioRepository.save(Usuario.builder()
                .persona(pCiud).username("ciud_atn_" + ts).passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(rolCiudadano)).enabled(true).accountLocked(false).build());

        // CustomUserDetails y Tokens JWT
        CustomUserDetails cudAdmin = CustomUserDetails.builder()
                .id(uAdmin.getId()).username(uAdmin.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN"))
                .permisos(Set.of("ROLE_ADMIN", "SERENAZGO_ATENCIONES_VER", "SERENAZGO_ATENCIONES_CREAR", "SERENAZGO_ATENCIONES_DESPACHAR"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("SERENAZGO_ATENCIONES_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_ATENCIONES_CREAR"),
                        new SimpleGrantedAuthority("SERENAZGO_ATENCIONES_DESPACHAR")))
                .build();
        adminToken = jwtTokenProvider.generateAccessToken(cudAdmin);

        CustomUserDetails cudSereno = CustomUserDetails.builder()
                .id(uSereno.getId()).username(uSereno.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_SERENO"))
                .permisos(Set.of("ROLE_SERENO", "SERENAZGO_ATENCIONES_VER"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_SERENO"),
                        new SimpleGrantedAuthority("SERENAZGO_ATENCIONES_VER")))
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
    @DisplayName("Debe redirigir al login si un usuario anónimo intenta acceder a /dashboard/serenazgo/atenciones")
    void testAccesoAnonimo_BandejaAtenciones_RedirigeLogin() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/atenciones"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Debe permitir acceso a la bandeja de atenciones para un usuario con rol ROLE_ADMIN")
    void testAccesoAdmin_BandejaAtenciones_Permitido() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/atenciones")
                        .with(user("admin").roles("ADMIN").authorities(new SimpleGrantedAuthority("SERENAZGO_ATENCIONES_VER"))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/atenciones/index"))
                .andExpect(model().attributeExists("atenciones", "metricas", "sectores", "estados", "prioridades"));
    }

    @Test
    @DisplayName("Debe denegar acceso a la bandeja de atenciones a un usuario con rol ROLE_CIUDADANO")
    void testAccesoCiudadano_BandejaAtenciones_Prohibido() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/atenciones")
                        .with(user("ciudadano").roles("CIUDADANO")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Debe permitir acceso al formulario de nueva atención a un usuario con permiso SERENAZGO_ATENCIONES_CREAR")
    void testAccesoSupervisor_NuevaAtencion_Permitido() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/atenciones/nueva")
                        .with(user("supervisor").roles("SUPERVISOR_SERENAZGO").authorities(new SimpleGrantedAuthority("SERENAZGO_ATENCIONES_CREAR"))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/atenciones/nueva"))
                .andExpect(model().attributeExists("nuevaAtencion", "incidenciasPendientes", "personalDisponible", "unidadesDisponibles"));
    }

    @Test
    @DisplayName("Debe denegar acceso al formulario de nueva atención a un usuario sin permiso de creación")
    void testAccesoSerenoSinPermiso_NuevaAtencion_Prohibido() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/atenciones/nueva")
                        .with(user("sereno").roles("SERENO").authorities(new SimpleGrantedAuthority("SERENAZGO_ATENCIONES_VER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Debe retornar métricas en formato JSON vía REST API para usuario autenticado")
    void testApiRest_Metricas_PermitidoConToken() throws Exception {
        mockMvc.perform(get("/api/serenazgo/atenciones/metricas")
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendientesAsignacion").exists())
                .andExpect(jsonPath("$.totalAtenciones").exists());
    }

    @Test
    @DisplayName("Debe generar archivo Excel con cabecera de descarga adecuada")
    void testExportarExcel_RetornaBlobBinario() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/atenciones/exportar/excel")
                        .with(user("admin").roles("ADMIN").authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN"),
                                new SimpleGrantedAuthority("SERENAZGO_ATENCIONES_EXPORTAR"))))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("atenciones_serenazgo")))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @DisplayName("Debe generar archivo PDF con cabecera de descarga adecuada")
    void testExportarPdf_RetornaBlobBinario() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/atenciones/exportar/pdf")
                        .with(user("admin").roles("ADMIN").authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN"),
                                new SimpleGrantedAuthority("SERENAZGO_ATENCIONES_EXPORTAR"))))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("atenciones_serenazgo")))
                .andExpect(content().contentType("application/pdf"));
    }
}
