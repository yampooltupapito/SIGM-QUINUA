package pe.gob.municipalidad.sigm.modules.portal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.portal.entity.PortalContacto;
import pe.gob.municipalidad.sigm.modules.portal.entity.PortalNoticia;
import pe.gob.municipalidad.sigm.modules.portal.repository.PortalContactoRepository;
import pe.gob.municipalidad.sigm.modules.portal.repository.PortalNoticiaRepository;
import pe.gob.municipalidad.sigm.modules.portal.service.PortalContenidoService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class PortalWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PortalContactoRepository contactoRepository;

    @Autowired
    private PortalNoticiaRepository noticiaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. PÁGINA PRINCIPAL PÚBLICA
    @Test
    @DisplayName("GET / debe responder HTTP 200 y renderizar el portal institucional con los 4 servicios oficiales")
    void testPaginaPrincipalPublica() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Municipalidad Distrital de Quinua")))
                .andExpect(content().string(containsString("Bienvenido al Portal de Servicios Digitales")))
                .andExpect(content().string(containsString("Mesa de Partes Virtual")))
                .andExpect(content().string(containsString("Mercados Municipales")))
                .andExpect(content().string(containsString("Seguridad Ciudadana")))
                .andExpect(content().string(containsString("Recaudación Tributaria Municipal")))
                .andExpect(content().string(containsString("Catálogo de Servicios Municipales")))
                .andExpect(content().string(containsString("Cuna de la Libertad Americana")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Licencias de Funcionamiento"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Seguimiento de Obras Públicas"))));
    }

    // 2. CATÁLOGO DE SERVICIOS
    @Test
    @DisplayName("GET /servicios debe listar exclusivamente los 4 servicios municipales oficiales")
    void testCatalogoServicios() throws Exception {
        mockMvc.perform(get("/servicios"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Catálogo de Servicios Municipales")))
                .andExpect(content().string(containsString("Mesa de Partes Virtual")))
                .andExpect(content().string(containsString("Mercados Municipales")))
                .andExpect(content().string(containsString("Seguridad Ciudadana")))
                .andExpect(content().string(containsString("Recaudación Tributaria Municipal")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Licencias de Funcionamiento"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Seguimiento de Obras Públicas"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Programas Sociales"))));
    }

    // 3. INFORMACIÓN INSTITUCIONAL
    @Test
    @DisplayName("GET /nosotros debe mostrar la historia, misión, visión y oficinas de Quinua")
    void testInformacionInstitucional() throws Exception {
        mockMvc.perform(get("/nosotros"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nuestra Institución")))
                .andExpect(content().string(containsString("Misión Institucional")))
                .andExpect(content().string(containsString("Visión Institucional")))
                .andExpect(content().string(containsString("Oficinas y Dependencias Municipales")));
    }

    // 4. MESA DE PARTES VIRTUAL (INFORMATIVA)
    @Test
    @DisplayName("GET /mesa-partes debe mostrar el estado real en desarrollo y requisitos TUO Ley 27444")
    void testMesaPartesInformativa() throws Exception {
        mockMvc.perform(get("/mesa-partes"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mesa de Partes Virtual")))
                .andExpect(content().string(containsString("Módulo en Proceso de Integración Técnica")))
                .andExpect(content().string(containsString("TUO Ley N° 27444")));
    }

    // 5. CONSULTA PÚBLICA DE TRÁMITES
    @Test
    @DisplayName("GET /tramites/consulta debe mostrar la interfaz de seguimiento con aviso de seguridad")
    void testConsultaTramitesInformativa() throws Exception {
        mockMvc.perform(get("/tramites/consulta"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Consulta Pública de Estado de Trámite")))
                .andExpect(content().string(containsString("Búsqueda de Expediente Municipal")))
                .andExpect(content().string(containsString("Seguridad y Protección de Datos Personales")));
    }

    // 6. NOTICIAS Y COMUNICADOS
    @Test
    @DisplayName("GET /noticias debe listar publicaciones con filtros y paginación")
    void testListarNoticiasPublicas() throws Exception {
        mockMvc.perform(get("/noticias"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Noticias y Comunicados Oficiales")))
                .andExpect(content().string(containsString("Lanzamiento Oficial del Sistema Integral de Gestión Municipal SIGM-QUINUA")));
    }

    @Test
    @DisplayName("GET /noticias/{slug} debe mostrar el detalle de una publicación existente")
    void testDetalleNoticiaPublica() throws Exception {
        mockMvc.perform(get("/noticias/lanzamiento-oficial-sigm-quinua"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Lanzamiento Oficial del Sistema Integral de Gestión Municipal SIGM-QUINUA")))
                .andExpect(content().string(containsString("Gerencia Municipal")));
    }

    @Test
    @DisplayName("GET /noticias/{slug} con slug inexistente debe redirigir a noticias con error")
    void testDetalleNoticiaInexistente() throws Exception {
        mockMvc.perform(get("/noticias/noticia-que-no-existe-en-el-sistema-12345"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/noticias?error=not_found"));
    }

    // 7. PREGUNTAS FRECUENTES (FAQ)
    @Test
    @DisplayName("GET /faq debe renderizar preguntas frecuentes y buscador")
    void testPreguntasFrecuentes() throws Exception {
        mockMvc.perform(get("/faq"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Preguntas Frecuentes (FAQ)")))
                .andExpect(content().string(containsString("horario y lugar de atención")));
    }

    @Test
    @DisplayName("GET /faq?q=horario debe filtrar por término de búsqueda")
    void testBuscarPreguntasFrecuentes() throws Exception {
        mockMvc.perform(get("/faq").param("q", "horario"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("horario y lugar de atención")));
    }

    // 8. FORMULARIO DE CONTACTO Y PROTECCIÓN CSRF
    @Test
    @DisplayName("GET /contacto debe renderizar el formulario oficial de atención ciudadana")
    void testFormularioContactoGet() throws Exception {
        mockMvc.perform(get("/contacto"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Formulario de Atención al Ciudadano")))
                .andExpect(content().string(containsString("Nombres y Apellidos Completos")));
    }

    @Test
    @DisplayName("POST /contacto con CSRF válido debe guardar consulta con código único y estado PENDIENTE")
    void testEnviarContactoExitoso() throws Exception {
        mockMvc.perform(post("/contacto")
                        .with(csrf())
                        .param("remitenteNombre", "Carmen Rosa Huamán")
                        .param("remitenteCorreo", "carmen.huaman@gmail.com")
                        .param("remitenteTelefono", "966554433")
                        .param("asunto", "Requisitos para trámite de alfarería")
                        .param("mensaje", "Deseo conocer los requisitos vigentes para la feria de artesanos en Quinua.")
                        .param("website", "")) // Honeypot vacío = humano legítimo
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacto?exito=true"));

        Optional<PortalContacto> guardado = contactoRepository.findAll().stream()
                .filter(c -> "carmen.huaman@gmail.com".equals(c.getRemitenteCorreo()))
                .findFirst();

        assertThat(guardado).isPresent();
        assertThat(guardado.get().getCodigo()).startsWith("CNS-");
        assertThat(guardado.get().getEstado()).isEqualTo("PENDIENTE");
    }

    @Test
    @DisplayName("POST /contacto sin CSRF debe retornar HTTP 403 Forbidden")
    void testEnviarContactoSinCsrf() throws Exception {
        mockMvc.perform(post("/contacto")
                        .param("remitenteNombre", "Intruso Sin Token")
                        .param("remitenteCorreo", "intruso@test.com")
                        .param("asunto", "Intento de ataque CSRF")
                        .param("mensaje", "Mensaje no autorizado."))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /contacto con honeypot completado debe rechazar la solicitud")
    void testEnviarContactoHoneypotBot() throws Exception {
        mockMvc.perform(post("/contacto")
                        .with(csrf())
                        .param("remitenteNombre", "Bot Spam")
                        .param("remitenteCorreo", "spam@bot.com")
                        .param("asunto", "Oferta comercial")
                        .param("mensaje", "Compra productos baratos en este link...")
                        .param("website", "http://spam-link.com")) // Honeypot lleno = robot
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("antispam")));
    }

    // 9. REGISTRO CIUDADANO SEGURO
    @Test
    @DisplayName("GET /registro debe mostrar el formulario de alta ciudadana")
    void testRegistroCiudadanoGet() throws Exception {
        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alta de Cuenta Ciudadana")))
                .andExpect(content().string(containsString("Datos de Identificación y Acceso")));
    }

    @Test
    @DisplayName("POST /registro con datos válidos debe registrar usuario asignando estrictamente ROLE_CIUDADANO")
    void testRegistroCiudadanoExitoso() throws Exception {
        String testDni = "47889911";
        String testUser = "vecino.quinua." + System.currentTimeMillis();

        mockMvc.perform(post("/registro")
                        .with(csrf())
                        .param("tipoDocumento", "DNI")
                        .param("numeroDocumento", testDni)
                        .param("nombres", "Alejandro")
                        .param("apellidoPaterno", "Condori")
                        .param("apellidoMaterno", "Ramos")
                        .param("correo", "alejandro.condori@gmail.com")
                        .param("telefono", "966112233")
                        .param("direccion", "Jr. Independencia s/n, Quinua")
                        .param("username", testUser)
                        .param("password", "PasswordSeguro2026!")
                        .param("confirmPassword", "PasswordSeguro2026!")
                        .param("terminosAceptados", "true")
                        .param("website", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registroExitoso=true"));

        Optional<Usuario> usuarioGuardado = usuarioRepository.findByUsernameWithRolesAndPersona(testUser);
        assertThat(usuarioGuardado).isPresent();
        assertThat(usuarioGuardado.get().getRoles()).hasSize(1);
        assertThat(usuarioGuardado.get().getRoles().iterator().next().getCodigo()).isEqualTo("ROLE_CIUDADANO");
    }

    @Test
    @DisplayName("POST /registro con contraseñas no coincidentes debe retornar error")
    void testRegistroCiudadanoPasswordsNoCoinciden() throws Exception {
        mockMvc.perform(post("/registro")
                        .with(csrf())
                        .param("tipoDocumento", "DNI")
                        .param("numeroDocumento", "45678902")
                        .param("nombres", "Maria")
                        .param("apellidoPaterno", "Suarez")
                        .param("correo", "maria@gmail.com")
                        .param("username", "maria.suarez")
                        .param("password", "Password123!")
                        .param("confirmPassword", "OtraPasswordDiferente!")
                        .param("terminosAceptados", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no coinciden")));
    }

    // 10. SANITIZACIÓN CONTRA XSS EN NOTICIAS
    @Test
    @DisplayName("Sanitización XSS debe eliminar tags <script> peligrosos del contenido")
    void testSanitizacionXss() {
        String inputMalicioso = "Texto legítimo con información municipal.<script>alert('Ataque XSS')</script> Fin del comunicado.";
        String sanitizado = PortalContenidoService.sanitizarContenido(inputMalicioso);

        assertThat(sanitizado).doesNotContain("<script>");
        assertThat(sanitizado).doesNotContain("</script>");
        assertThat(sanitizado).contains("Texto legítimo con información municipal.");
        assertThat(sanitizado).contains("Fin del comunicado.");
    }
}
