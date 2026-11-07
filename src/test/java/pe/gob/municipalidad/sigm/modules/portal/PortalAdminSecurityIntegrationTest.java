package pe.gob.municipalidad.sigm.modules.portal;

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
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalContactoDtos;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalFaqDtos;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalNoticiaDtos;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalServicioDtos;
import pe.gob.municipalidad.sigm.modules.portal.entity.PortalContacto;
import pe.gob.municipalidad.sigm.modules.portal.entity.PortalNoticia;
import pe.gob.municipalidad.sigm.modules.portal.repository.PortalContactoRepository;
import pe.gob.municipalidad.sigm.modules.portal.repository.PortalNoticiaRepository;
import pe.gob.municipalidad.sigm.modules.portal.repository.PortalServicioRepository;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class PortalAdminSecurityIntegrationTest {

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
    private PortalNoticiaRepository noticiaRepository;

    @Autowired
    private PortalContactoRepository contactoRepository;

    @Autowired
    private PortalServicioRepository servicioRepository;

    private Cookie adminAuthCookie;
    private Cookie ciudadanoAuthCookie;

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
                .numeroDocumento("20304050")
                .nombres("Admin")
                .apellidoPaterno("Portal")
                .correo("admin.portal@quinua.gob.pe")
                .build());

        Usuario adminUser = usuarioRepository.save(Usuario.builder()
                .username("admin.portal")
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
                .permisos(Set.of("PORTAL_MANAGE", "ADMINISTRACION_MANAGE"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("PORTAL_MANAGE")
                ))
                .build();

        String adminJwt = jwtTokenProvider.generateAccessToken(adminDetails);
        adminAuthCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, adminJwt);
        adminAuthCookie.setPath("/");

        Persona personaCiudadano = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("87654321")
                .nombres("Juan")
                .apellidoPaterno("Perez")
                .correo("juan.ciudadano@gmail.com")
                .build());

        Usuario ciudadanoUser = usuarioRepository.save(Usuario.builder()
                .username("ciudadano.test")
                .persona(personaCiudadano)
                .passwordHash(passwordEncoder.encode("Ciudadano2026!"))
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
    }

    // 1. CONTROL DE ACCESO PARA USUARIOS NO AUTENTICADOS (ANÓNIMOS)
    @Test
    @DisplayName("Acceso anónimo a /admin/portal/noticias debe redirigir a /login")
    void testAccesoAnonimoNoticiasAdmin() throws Exception {
        mockMvc.perform(get("/admin/portal/noticias"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Acceso anónimo a /admin/portal/faq debe redirigir a /login")
    void testAccesoAnonimoFaqAdmin() throws Exception {
        mockMvc.perform(get("/admin/portal/faq"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Acceso anónimo a /admin/portal/contactos debe redirigir a /login")
    void testAccesoAnonimoContactosAdmin() throws Exception {
        mockMvc.perform(get("/admin/portal/contactos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Acceso anónimo a /api/admin/portal/noticias debe retornar HTTP 401 Unauthorized")
    void testAccesoAnonimoApiAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/portal/noticias/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // 2. PREVENCIÓN DE ESCALAMIENTO DE PRIVILEGIOS: ROLE_CIUDADANO NO PUEDE ENTRAR A ADMIN
    @Test
    @DisplayName("Usuario con ROLE_CIUDADANO debe recibir HTTP 403 Forbidden al intentar acceder a rutas administrativas")
    void testCiudadanoProhibidoEnAdmin() throws Exception {
        mockMvc.perform(get("/admin/portal/noticias")
                        .cookie(ciudadanoAuthCookie))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/admin/portal/contactos")
                        .cookie(ciudadanoAuthCookie))
                .andExpect(status().isForbidden());
    }

    // 3. ACCESO PERMITIDO CON ROLE_ADMIN
    @Test
    @DisplayName("Administrador con ROLE_ADMIN debe acceder correctamente a las vistas del portal")
    void testAdminAutorizadoVistas() throws Exception {
        mockMvc.perform(get("/admin/portal/noticias")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Gestión de Noticias y Comunicados")));

        mockMvc.perform(get("/admin/portal/faq")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Preguntas Frecuentes (FAQ)")));

        mockMvc.perform(get("/admin/portal/contactos")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bandeja de Consultas Ciudadanas")));

        mockMvc.perform(get("/admin/portal/servicios")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Catálogo de Servicios Visibles")));
    }

    // 4. CRUD ADMINISTRATIVO VÍA REST API (NOTICIAS)
    @Test
    @DisplayName("POST /api/admin/portal/noticias debe crear publicación institucional")
    void testCrearNoticiaAdminApi() throws Exception {
        PortalNoticiaDtos.NoticiaCreateRequest request = PortalNoticiaDtos.NoticiaCreateRequest.builder()
                .titulo("Convocatoria Pública para Mantenimiento de Plazas 2026")
                .resumen("Se convoca a proveedores locales para el mantenimiento de áreas verdes distritales.")
                .contenido("Detalle completo de las bases administrativas y términos de referencia.")
                .tipo("CONVOCATORIA")
                .categoria("OBRAS_PUBLICAS")
                .autor("Subgerencia de Servicios Públicos")
                .destacada(false)
                .publicarInmediatamente(true)
                .build();

        mockMvc.perform(post("/api/admin/portal/noticias")
                        .with(csrf())
                        .cookie(adminAuthCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.slug").value("convocatoria-publica-para-mantenimiento-de-plazas-2026"))
                .andExpect(jsonPath("$.data.estado").value("PUBLICADO"));
    }

    @Test
    @DisplayName("PATCH /api/admin/portal/noticias/{id}/estado debe actualizar estado a BORRADOR o ARCHIVADO")
    void testCambiarEstadoNoticia() throws Exception {
        PortalNoticia n = noticiaRepository.findAll().get(0);

        mockMvc.perform(patch("/api/admin/portal/noticias/" + n.getId() + "/estado")
                        .with(csrf())
                        .cookie(adminAuthCookie)
                        .param("estado", "BORRADOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // 5. CRUD ADMINISTRATIVO VÍA REST API (FAQ)
    @Test
    @DisplayName("POST /api/admin/portal/faq debe crear pregunta frecuente institucional")
    void testCrearFaqAdminApi() throws Exception {
        PortalFaqDtos.FaqCreateRequest request = PortalFaqDtos.FaqCreateRequest.builder()
                .pregunta("¿Dónde puedo obtener un certificado domiciliario en Quinua?")
                .respuesta("El certificado domiciliario se tramita de forma presencial en Secretaría General previa verificación.")
                .categoria("TRAMITES")
                .orden(10)
                .estado("ACTIVO")
                .build();

        mockMvc.perform(post("/api/admin/portal/faq")
                        .with(csrf())
                        .cookie(adminAuthCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pregunta").value(request.getPregunta()));
    }

    // 6. ATENCIÓN DE CONSULTA CIUDADANA VÍA REST API
    @Test
    @DisplayName("POST /api/admin/portal/contactos/{id}/atender debe registrar respuesta y cambiar a ATENDIDO")
    void testAtenderContactoAdminApi() throws Exception {
        PortalContacto contacto = PortalContacto.builder()
                .codigo("CNS-2026-99999")
                .remitenteNombre("Vecino Test")
                .remitenteCorreo("vecino@test.pe")
                .asunto("Consulta de prueba")
                .mensaje("Mensaje para prueba de atención.")
                .estado("PENDIENTE")
                .build();
        contacto = contactoRepository.save(contacto);

        PortalContactoDtos.ContactoAtencionRequest atencion = PortalContactoDtos.ContactoAtencionRequest.builder()
                .respuestaInterna("Se derivó la consulta al área de Rentas para contacto telefónico directo.")
                .estado("ATENDIDO")
                .build();

        mockMvc.perform(post("/api/admin/portal/contactos/" + contacto.getId() + "/atender")
                        .with(csrf())
                        .cookie(adminAuthCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atencion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.estado").value("ATENDIDO"))
                .andExpect(jsonPath("$.data.atendidoPor").value("admin.portal"));
    }

    // 7. ACTUALIZACIÓN DE SERVICIO MUNICIPAL
    @Test
    @DisplayName("PUT /api/admin/portal/servicios/{id} debe actualizar disponibilidad del servicio")
    void testActualizarServicioAdminApi() throws Exception {
        var servicios = servicioRepository.findAll();
        var srv = servicios.get(0);

        PortalServicioDtos.ServicioUpdateRequest update = PortalServicioDtos.ServicioUpdateRequest.builder()
                .nombre(srv.getNombre())
                .descripcion(srv.getDescripcion())
                .icono(srv.getIcono())
                .orden(srv.getOrden())
                .estadoDisponibilidad("DISPONIBLE")
                .urlAcceso("/mesa-partes")
                .estado("ACTIVO")
                .build();

        mockMvc.perform(put("/api/admin/portal/servicios/" + srv.getId())
                        .with(csrf())
                        .cookie(adminAuthCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.estadoDisponibilidad").value("DISPONIBLE"));
    }
}
