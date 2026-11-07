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
public class PatrullajesWebSecurityTest {

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

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN")
                .orElseGet(() -> rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("Administrador").estado("ACTIVO").build()));
        Rol rolSereno = rolRepository.findByCodigo("ROLE_SERENO")
                .orElseGet(() -> rolRepository.save(Rol.builder().codigo("ROLE_SERENO").nombre("Sereno de Cuadrilla").estado("ACTIVO").build()));
        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO")
                .orElseGet(() -> rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("Ciudadano").estado("ACTIVO").build()));

        // 1. Admin
        Persona pAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("43" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Admin").apellidoPaterno("Patrullaje").estado("ACTIVO").build());
        Usuario uAdmin = usuarioRepository.save(Usuario.builder()
                .username("admin_pat_" + ts).persona(pAdmin).passwordHash(passwordEncoder.encode("Pass123!"))
                .enabled(true).accountLocked(false).roles(Set.of(rolAdmin)).build());

        cudAdmin = CustomUserDetails.builder()
                .id(uAdmin.getId()).username(uAdmin.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("SERENAZGO_PATRULLAJES_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_PATRULLAJES_CREAR"),
                        new SimpleGrantedAuthority("SERENAZGO_PATRULLAJES_EXPORTAR")
                ))
                .build();
        adminToken = jwtTokenProvider.generateAccessToken(cudAdmin);

        // 2. Sereno de campo
        Persona pSereno = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("44" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Sereno").apellidoPaterno("Campo").estado("ACTIVO").build());
        Usuario uSereno = usuarioRepository.save(Usuario.builder()
                .username("sereno_pat_" + ts).persona(pSereno).passwordHash(passwordEncoder.encode("Pass123!"))
                .enabled(true).accountLocked(false).roles(Set.of(rolSereno)).build());

        cudSereno = CustomUserDetails.builder()
                .id(uSereno.getId()).username(uSereno.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_SERENO"),
                        new SimpleGrantedAuthority("SERENAZGO_PATRULLAJES_VER")
                ))
                .build();
        serenoToken = jwtTokenProvider.generateAccessToken(cudSereno);

        // 3. Ciudadano sin permisos
        Persona pCiu = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("45" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Vecino").apellidoPaterno("Quinua").estado("ACTIVO").build());
        Usuario uCiu = usuarioRepository.save(Usuario.builder()
                .username("vecino_pat_" + ts).persona(pCiu).passwordHash(passwordEncoder.encode("Pass123!"))
                .enabled(true).accountLocked(false).roles(Set.of(rolCiudadano)).build());

        cudCiudadano = CustomUserDetails.builder()
                .id(uCiu.getId()).username(uCiu.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CIUDADANO")))
                .build();
        ciudadanoToken = jwtTokenProvider.generateAccessToken(cudCiudadano);
    }

    private static SimpleGrantedAuthority SimpleReportsAuthority(String role) {
        return new SimpleGrantedAuthority(role);
    }

    @Test
    @DisplayName("SEC-01: Usuario anónimo es redirigido a login al intentar acceder a patrullajes")
    void testAnonimoRedirigidoALogin() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/patrullajes"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("SEC-02: Usuario ciudadano sin permisos de patrullaje recibe 403 Forbidden")
    void testCiudadanoAccesoDenegado() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/patrullajes")
                        .with(user(cudCiudadano)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-03: Administrador con permisos accede exitosamente a la bandeja de patrullajes")
    void testAdminAccesoExitosoBandeja() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/patrullajes")
                        .with(user(cudAdmin)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/patrullajes/index"))
                .andExpect(model().attributeExists("patrullajes"))
                .andExpect(model().attributeExists("kpis"));
    }

    @Test
    @DisplayName("SEC-04: Sereno de campo con SERENAZGO_PATRULLAJES_VER accede exitosamente a la bandeja")
    void testSerenoAccesoExitosoBandeja() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/patrullajes")
                        .with(user(cudSereno)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/patrullajes/index"));
    }

    @Test
    @DisplayName("SEC-05: Administrador accede al formulario de planificación de patrullajes")
    void testAdminAccesoFormularioNuevo() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/patrullajes/nuevo")
                        .with(user(cudAdmin)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/patrullajes/nuevo"))
                .andExpect(model().attributeExists("nuevoPatrullaje"));
    }

    @Test
    @DisplayName("SEC-06: Sereno sin permiso SERENAZGO_PATRULLAJES_CREAR recibe 403 al intentar planificar")
    void testSerenoSinCrearRecibeForbidden() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/patrullajes/nuevo")
                        .with(user(cudSereno)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-07: API REST de rutas protegida por token JWT con permisos adecuados")
    void testApiRestRutasConToken() throws Exception {
        mockMvc.perform(get("/api/v1/serenazgo/patrullajes/rutas")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SEC-08: API REST rechaza solicitudes de ciudadanos no autorizados (403)")
    void testApiRestCiudadanoRechazado() throws Exception {
        mockMvc.perform(get("/api/v1/serenazgo/patrullajes/rutas")
                        .header("Authorization", "Bearer " + ciudadanoToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
