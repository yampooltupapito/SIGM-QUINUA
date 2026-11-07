package pe.gob.municipalidad.sigm.modules.serenazgo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
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
public class DocumentosWebSecurityTest {

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
    private String ciudadanoToken;

    private CustomUserDetails cudAdmin;
    private CustomUserDetails cudSupervisor;
    private CustomUserDetails cudCiudadano;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN")
                .orElseGet(() -> rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("Administrador").estado("ACTIVO").build()));

        Rol rolSupervisor = rolRepository.findByCodigo("ROLE_SUPERVISOR_SERENAZGO")
                .orElseGet(() -> rolRepository.save(Rol.builder().codigo("ROLE_SUPERVISOR_SERENAZGO").nombre("Supervisor Serenazgo").estado("ACTIVO").build()));

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO")
                .orElseGet(() -> rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("Ciudadano").estado("ACTIVO").build()));

        // 1. Admin
        Persona pAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("48" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Admin").apellidoPaterno("Documentos").estado("ACTIVO").build());

        Usuario uAdmin = usuarioRepository.save(Usuario.builder()
                .username("admin_doc_" + ts)
                .persona(pAdmin)
                .passwordHash(passwordEncoder.encode("Pass123!"))
                .enabled(true)
                .accountLocked(false)
                .roles(Set.of(rolAdmin))
                .build());

        cudAdmin = CustomUserDetails.builder()
                .id(uAdmin.getId())
                .username(uAdmin.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("SERENAZGO_DOCUMENTOS_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_DOCUMENTOS_CREAR"),
                        new SimpleGrantedAuthority("SERENAZGO_DOCUMENTOS_DESCARGAR"),
                        new SimpleGrantedAuthority("SERENAZGO_DOCUMENTOS_EXPORTAR")
                ))
                .build();
        adminToken = jwtTokenProvider.generateAccessToken(cudAdmin);

        // 2. Supervisor
        Persona pSup = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("49" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Supervisor").apellidoPaterno("Seguridad").estado("ACTIVO").build());

        Usuario uSup = usuarioRepository.save(Usuario.builder()
                .username("sup_doc_" + ts)
                .persona(pSup)
                .passwordHash(passwordEncoder.encode("Pass123!"))
                .enabled(true)
                .accountLocked(false)
                .roles(Set.of(rolSupervisor))
                .build());

        cudSupervisor = CustomUserDetails.builder()
                .id(uSup.getId())
                .username(uSup.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_SUPERVISOR_SERENAZGO"),
                        new SimpleGrantedAuthority("SERENAZGO_DOCUMENTOS_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_DOCUMENTOS_DESCARGAR"),
                        new SimpleGrantedAuthority("SERENAZGO_DOCUMENTOS_EXPORTAR")
                ))
                .build();

        // 3. Ciudadano sin permisos
        Persona pCiu = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("50" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Vecino").apellidoPaterno("Comun").estado("ACTIVO").build());

        Usuario uCiu = usuarioRepository.save(Usuario.builder()
                .username("ciu_doc_" + ts)
                .persona(pCiu)
                .passwordHash(passwordEncoder.encode("Pass123!"))
                .enabled(true)
                .accountLocked(false)
                .roles(Set.of(rolCiudadano))
                .build());

        cudCiudadano = CustomUserDetails.builder()
                .id(uCiu.getId())
                .username(uCiu.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CIUDADANO")))
                .build();
        ciudadanoToken = jwtTokenProvider.generateAccessToken(cudCiudadano);
    }

    @Test
    @DisplayName("1. Acceso anónimo a dashboard de documentos es redirigido a login")
    void testAccesoAnonimoRedirigido() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/documentos"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("2. Usuario ciudadano sin permisos recibe 403 Forbidden en dashboard de documentos")
    void testCiudadanoSinPermisoForbidden() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/documentos").with(user(cudCiudadano)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("3. Supervisor y Administrador con permisos acceden con 200 OK")
    void testSupervisorYAdminAccesoOk() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/documentos").with(user(cudSupervisor)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/documentos/index"))
                .andExpect(model().attributeExists("pagina", "stats", "tipos", "estados"));

        mockMvc.perform(get("/dashboard/serenazgo/documentos").with(user(cudAdmin)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/documentos/index"));
    }

    @Test
    @DisplayName("4. API REST: GET /api/v1/serenazgo/documentos con JWT Admin retorna 200 OK")
    void testApiRestConJwtAdminRetornaOk() throws Exception {
        mockMvc.perform(get("/api/v1/serenazgo/documentos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("5. API REST: GET /api/v1/serenazgo/documentos con JWT Ciudadano recibe 403 Forbidden")
    void testApiRestConJwtCiudadanoForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/serenazgo/documentos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ciudadanoToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("6. Formulario de nuevo documento: Admin accede 200 OK, Ciudadano 403")
    void testFormularioNuevoDocumento() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/documentos/nuevo").with(user(cudAdmin)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/documentos/form"))
                .andExpect(model().attributeExists("form", "tipos", "niveles"));

        mockMvc.perform(get("/dashboard/serenazgo/documentos/nuevo").with(user(cudCiudadano)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7. Exportación Excel: Acceso con permiso SERENAZGO_DOCUMENTOS_EXPORTAR retorna 200")
    void testExportarExcelWeb() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/documentos/exportar/excel").with(user(cudSupervisor)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }
}
