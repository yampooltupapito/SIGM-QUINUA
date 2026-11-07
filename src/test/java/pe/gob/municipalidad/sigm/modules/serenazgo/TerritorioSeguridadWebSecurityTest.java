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
public class TerritorioSeguridadWebSecurityTest {

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
                .tipoDocumento("DNI").numeroDocumento("91" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Admin").apellidoPaterno("Territorial").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uAdmin = usuarioRepository.save(Usuario.builder()
                .persona(pAdmin).username("admin_ter_" + ts).passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(rolAdmin)).enabled(true).accountLocked(false).build());

        Persona pSereno = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("92" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Sereno").apellidoPaterno("Patrulla").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uSereno = usuarioRepository.save(Usuario.builder()
                .persona(pSereno).username("sereno_ter_" + ts).passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(rolSereno)).enabled(true).accountLocked(false).build());

        Persona pCiud = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("93" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Ciudadano").apellidoPaterno("SinPermiso").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uCiud = usuarioRepository.save(Usuario.builder()
                .persona(pCiud).username("ciud_ter_" + ts).passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(rolCiudadano)).enabled(true).accountLocked(false).build());

        CustomUserDetails cudAdmin = CustomUserDetails.builder()
                .id(uAdmin.getId()).username(uAdmin.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN"))
                .permisos(Set.of("ROLE_ADMIN", "SERENAZGO_SECTORES_VER", "SERENAZGO_SECTORES_CREAR", "SERENAZGO_ZONAS_VER", "SERENAZGO_GEOGRAFIA_EXPORTAR"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("SERENAZGO_SECTORES_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_SECTORES_CREAR"),
                        new SimpleGrantedAuthority("SERENAZGO_ZONAS_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_GEOGRAFIA_EXPORTAR")))
                .build();
        adminToken = jwtTokenProvider.generateAccessToken(cudAdmin);

        CustomUserDetails cudSereno = CustomUserDetails.builder()
                .id(uSereno.getId()).username(uSereno.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_SERENO"))
                .permisos(Set.of("ROLE_SERENO", "SERENAZGO_SECTORES_VER", "SERENAZGO_ZONAS_VER", "SERENAZGO_GEOGRAFIA_EXPORTAR"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_SERENO"),
                        new SimpleGrantedAuthority("SERENAZGO_SECTORES_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_ZONAS_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_GEOGRAFIA_EXPORTAR")))
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
    @DisplayName("01: Petición anónima a API territorial rechazada con HTTP 401 Unauthorized")
    void test01_accesoAnonimoRechazado() throws Exception {
        mockMvc.perform(get("/api/serenazgo/territorio/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("02: Petición web anónima a vista de sectores redirige a login (HTTP 302)")
    void test02_accesoWebAnonimoRedirigeLogin() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/sectores"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("03: Acceso autorizado a la vista principal de sectores con cookie de sesión (HTTP 200)")
    void test03_accesoWebAutorizadoConCookie() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/sectores")
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, serenoToken)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/territorio/index"))
                .andExpect(model().attributeExists("sectores"))
                .andExpect(model().attributeExists("zonas"))
                .andExpect(model().attributeExists("dashboard"));
    }

    @Test
    @DisplayName("04: Consulta de GeoJSON FeatureCollection de mapa territorial por efectivo sereno (HTTP 200)")
    void test04_consultaMapaGeoJsonSereno() throws Exception {
        mockMvc.perform(get("/api/serenazgo/territorio/mapa")
                        .header("Authorization", "Bearer " + serenoToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FeatureCollection"))
                .andExpect(jsonPath("$.features").isArray());
    }

    @Test
    @DisplayName("05: Usuario ciudadano sin privilegios territoriales es bloqueado con HTTP 403 Forbidden")
    void test05_ciudadanoSinPermisoRechazado() throws Exception {
        mockMvc.perform(get("/api/serenazgo/territorio/dashboard")
                        .header("Authorization", "Bearer " + ciudadanoToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("06: Exportación de reporte Excel territorial autorizado (HTTP 200)")
    void test06_exportarExcelTerritorial() throws Exception {
        mockMvc.perform(get("/api/serenazgo/territorio/exportar-excel")
                        .header("Authorization", "Bearer " + serenoToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }
}
