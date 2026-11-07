package pe.gob.municipalidad.sigm.modules.mesapartes;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.model.*;
import pe.gob.municipalidad.sigm.common.repository.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración de seguridad y renderizado Thymeleaf para el portal y bandejas de Mesa de Partes Virtual.
 * Verifica autenticación, autorización basada en roles (RBAC), control de acceso y renderizado HTML sin errores.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("Pruebas de Seguridad Web y Vistas de Mesa de Partes Virtual - SIGM-QUINUA")
public class MesaPartesWebSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private Cookie adminCookie;
    private Cookie ciudadanoCookie;

    @BeforeEach
    void setUp() {
        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("ADMINISTRADOR").estado("ACTIVO").build()));
        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("CIUDADANO").estado("ACTIVO").build()));

        Usuario adminUser = crearUsuario("admin_mpv_web", "79998877", rolAdmin, "Admin", "Mpv");
        adminCookie = buildJwtCookie(adminUser, "ROLE_ADMIN", List.of(
                "ROLE_ADMIN",
                "MESAPARTES_EXPEDIENTE_VER_TODOS",
                "MESAPARTES_DERIVACION_VER",
                "MESAPARTES_ATENCION_VER",
                "MESAPARTES_REPORTES_VER",
                "MESAPARTES_CONFIG_VER"
        ));

        Usuario ciudadanoUser = crearUsuario("ciudadano_mpv_web", "78887766", rolCiudadano, "Carlos", "Quispe");
        ciudadanoCookie = buildJwtCookie(ciudadanoUser, "ROLE_CIUDADANO", List.of("ROLE_CIUDADANO"));
    }

    private Usuario crearUsuario(String username, String dni, Rol rol, String nombres, String apePaterno) {
        String uniqueDoc = dni + "_" + UUID.randomUUID().toString().substring(0, 4);
        Persona persona = personaRepository.save(Persona.builder()
                .numeroDocumento(uniqueDoc)
                .tipoDocumento("DNI")
                .nombres(nombres)
                .apellidoPaterno(apePaterno)
                .apellidoMaterno("Quinua")
                .correo(username + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        return usuarioRepository.save(Usuario.builder()
                .persona(persona)
                .username(username + "_" + UUID.randomUUID().toString().substring(0, 6))
                .passwordHash(passwordEncoder.encode("Password123*"))
                .roles(Set.of(rol))
                .enabled(true)
                .accountLocked(false)
                .build());
    }

    private Cookie buildJwtCookie(Usuario usuario, String roleName, List<String> permissions) {
        List<SimpleGrantedAuthority> authoritiesList = new ArrayList<>();
        authoritiesList.add(new SimpleGrantedAuthority(roleName));
        for (String p : permissions) {
            authoritiesList.add(new SimpleGrantedAuthority(p));
        }

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .personaId(usuario.getPersona().getId())
                .nombreCompleto(usuario.getPersona().getNombres() + " " + usuario.getPersona().getApellidoPaterno())
                .roles(Set.of(roleName))
                .permisos(new HashSet<>(permissions))
                .authorities(authoritiesList)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        String token = jwtTokenProvider.generateAccessToken(userDetails);
        Cookie cookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

    @Test
    @DisplayName("1. Rutas públicas de Mesa de Partes deben responder 200 OK para usuarios anónimos")
    void testRutasPublicasAnonimas() throws Exception {
        mockMvc.perform(get("/mesa-partes/seguimiento"))
                .andExpect(status().isOk())
                .andExpect(view().name("mesa-partes/seguimiento"));

        mockMvc.perform(get("/verificar-documento"))
                .andExpect(status().isOk())
                .andExpect(view().name("mesa-partes/verificar-documento"));
    }

    @Test
    @DisplayName("2. Rutas ciudadanas protegidas deben redirigir al login si el usuario es anónimo")
    void testRutasCiudadanasRequierenAutenticacion() throws Exception {
        mockMvc.perform(get("/mesa-partes/nueva-solicitud"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/mesa-partes/mis-solicitudes"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("3. Usuario con ROLE_CIUDADANO debe acceder a sus vistas ciudadanas con 200 OK")
    void testAccesoCiudadanoAutenticado() throws Exception {
        mockMvc.perform(get("/mesa-partes/nueva-solicitud").cookie(ciudadanoCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("mesa-partes/nueva-solicitud"));

        mockMvc.perform(get("/mesa-partes/mis-solicitudes").cookie(ciudadanoCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("mesa-partes/mis-solicitudes"));
    }

    @Test
    @DisplayName("4. Usuario con ROLE_CIUDADANO no debe tener acceso a las bandejas administrativas (403 Forbidden)")
    void testCiudadanoBloqueadoDeBandejasAdmin() throws Exception {
        mockMvc.perform(get("/dashboard/mesa-partes/bandeja").cookie(ciudadanoCookie))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/mesa-partes/derivaciones").cookie(ciudadanoCookie))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/mesa-partes/atencion").cookie(ciudadanoCookie))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/mesa-partes/configuracion/tramites").cookie(ciudadanoCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("5. Usuario con ROLE_ADMIN debe acceder a todas las bandejas institucionales de Mesa de Partes (200 OK)")
    void testAdminAccesoBandejasInstitucionales() throws Exception {
        mockMvc.perform(get("/dashboard/mesa-partes/bandeja").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/bandeja"));

        mockMvc.perform(get("/dashboard/mesa-partes/derivaciones").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/derivaciones"));

        mockMvc.perform(get("/dashboard/mesa-partes/derivaciones/recibidas").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/derivaciones-recibidas"));

        mockMvc.perform(get("/dashboard/mesa-partes/mis-expedientes").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/mis-expedientes"));

        mockMvc.perform(get("/dashboard/mesa-partes/atencion").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/atencion"));

        mockMvc.perform(get("/dashboard/mesa-partes/reportes").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/reportes"));
    }

    @Test
    @DisplayName("6. Usuario con ROLE_ADMIN debe acceder a las vistas de configuración de trámites y plazos (200 OK)")
    void testAdminAccesoConfiguracionMpv() throws Exception {
        mockMvc.perform(get("/dashboard/mesa-partes/configuracion/tramites").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/configuracion/tramites"));

        mockMvc.perform(get("/dashboard/mesa-partes/configuracion/calendario").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/configuracion/calendario"));

        mockMvc.perform(get("/dashboard/mesa-partes/configuracion/numeracion").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/configuracion/numeracion"));

        mockMvc.perform(get("/dashboard/mesa-partes/configuracion/plantillas").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/configuracion/plantillas"));

        mockMvc.perform(get("/dashboard/mesa-partes/configuracion/plazos").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/configuracion/plazos"));
    }
}
