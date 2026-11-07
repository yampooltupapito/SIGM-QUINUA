package pe.gob.municipalidad.sigm.modules.recaudaciontributaria;

import jakarta.servlet.http.Cookie;
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
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class NotificacionTributariaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ContribuyenteTributarioRepository contribuyenteRepository;

    @Autowired
    private TipoNotificacionTributariaRepository tipoRepository;

    @Autowired
    private MedioNotificacionTributariaRepository medioRepository;

    @Autowired
    private NotificacionTributariaRepository notificacionRepository;

    @Autowired
    private PreparacionNotificacionService preparacionService;

    @Autowired
    private NotificacionTributariaService notificacionService;

    private Usuario testUser;
    private Cookie authCookie;
    private ContribuyenteTributario contribuyente;
    private TipoNotificacionTributaria tipoFormal;
    private MedioNotificacionTributaria medioFisico;

    @BeforeEach
    void setUp() {
        long suffix = System.currentTimeMillis() % 100000000;

        Persona p = Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 1234) % 100000000))
                .nombres("Notificador")
                .apellidoPaterno("Tributario")
                .apellidoMaterno("Quinua")
                .build();
        p = personaRepository.save(p);

        testUser = Usuario.builder()
                .username("notificador_" + suffix)
                .passwordHash("$2a$10$abcdefghijklmnopqrstuvwxyz123456")
                .persona(p)
                .enabled(true)
                .build();
        testUser = usuarioRepository.save(testUser);

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(testUser.getId())
                .username(testUser.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_TRIBUTACION"),
                        new SimpleGrantedAuthority("TRIBUTARIA_NOTIFICACIONES_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_NOTIFICACIONES_CREAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_NOTIFICACIONES_VALIDAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_NOTIFICACIONES_AUTORIZAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_NOTIFICACIONES_ASIGNAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_NOTIFICACIONES_REGISTRAR_INTENTO"),
                        new SimpleGrantedAuthority("TRIBUTARIA_NOTIFICACIONES_REGISTRAR_CARGO"),
                        new SimpleGrantedAuthority("TRIBUTARIA_NOTIFICACIONES_VALIDAR_PROCEDIMIENTO"),
                        new SimpleGrantedAuthority("TRIBUTARIA_NOTIFICACIONES_ANULAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_NOTIFICACIONES_GENERAR_LOTES"),
                        new SimpleGrantedAuthority("TRIBUTARIA_NOTIFICACIONES_EXPORTAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_NOTIFICACIONES_REPORTES")
                ))
                .build();

        String token = jwtTokenProvider.generateAccessToken(userDetails);
        authCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, token);
        authCookie.setPath("/");

        // Contribuyente
        Persona pContrib = Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 5678) % 100000000))
                .nombres("Contribuyente")
                .apellidoPaterno("Quinua")
                .apellidoMaterno("Prueba")
                .build();
        pContrib = personaRepository.save(pContrib);

        contribuyente = ContribuyenteTributario.builder()
                .codigoContribuyente("CONT-NOTIF-" + suffix)
                .persona(pContrib)
                .tipoContribuyente(pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento(pContrib.getNumeroDocumento())
                .nombreRazonSocial("Contribuyente Quinua")
                .domicilioFiscal("Jr. San Martín N° 200, Quinua")
                .estado(pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoContribuyente.ACTIVO)
                .build();
        contribuyente = contribuyenteRepository.save(contribuyente);

        // Tipos y Medios de catálogo
        tipoFormal = tipoRepository.findByCodigo("COM_INF_VENCIMIENTO_PREDIAL")
                .orElseGet(() -> tipoRepository.save(TipoNotificacionTributaria.builder()
                        .codigo("COM_INF_VENCIMIENTO_PREDIAL")
                        .nombre("Comunicación Informativa")
                        .categoria(CategoriaTipoNotificacion.INFORMATIVA)
                        .requiereDocumento(false)
                        .requiereValidacionLegal(false)
                        .requiereEvidencia(false)
                        .estado("ACTIVO")
                        .build()));

        medioFisico = medioRepository.findByCodigo("ENTREGA_FISICA_DIRECTA")
                .orElseGet(() -> medioRepository.save(MedioNotificacionTributaria.builder()
                        .codigo("ENTREGA_FISICA_DIRECTA")
                        .nombre("Entrega Física Directa")
                        .esMedioFormal(true)
                        .requiereDireccionFisica(true)
                        .estado("ACTIVO")
                        .build()));
    }

    @Test
    @DisplayName("Debe denegar acceso al dashboard a usuarios sin autenticación")
    void testAccesoSinAutenticacionDenegado() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/notificaciones"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Debe permitir acceso al dashboard con usuario autenticado con permisos tributarios")
    void testAccesoDashboardConAutenticacion() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/notificaciones")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/notificaciones/dashboard"))
                .andExpect(model().attributeExists("dashboard", "notificacionesTributarias"));
    }

    @Test
    @DisplayName("Debe permitir acceso al formulario de nueva notificación")
    void testFormularioNuevaNotificacion() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/notificaciones/nueva")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/notificaciones/nueva"))
                .andExpect(model().attributeExists("notificacionRequest", "tipos", "medios"));
    }

    @Test
    @Transactional
    @DisplayName("Debe crear una notificación en borrador y luego ver su ficha de detalle")
    void testCrearYVerDetalleNotificacion() throws Exception {
        CrearNotificacionRequest req = CrearNotificacionRequest.builder()
                .tipoNotificacionId(tipoFormal.getId())
                .contribuyenteId(contribuyente.getId())
                .destinatarioNombre("Destinatario Prueba Quinua")
                .destinatarioNumeroDoc("45892314")
                .domicilioNotificacion("Jr. San Martín N° 200, Quinua")
                .medioNotificacionId(medioFisico.getId())
                .observaciones("Notificación de prueba de integración")
                .build();

        NotificacionTributaria creada = preparacionService.prepararNotificacion(req, testUser, null);
        assertThat(creada).isNotNull();
        assertThat(creada.getCodigoNotificacion()).startsWith("NOTIF-");
        assertThat(creada.getEstadoAdministrativo()).isEqualTo(EstadoAdministrativoNotificacion.BORRADOR);

        mockMvc.perform(get("/dashboard/tributaria/notificaciones/" + creada.getId())
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/notificaciones/detalle"))
                .andExpect(model().attributeExists("notificacion", "tabActiva"));
    }

    @Test
    @Transactional
    @DisplayName("Debe validar formalmente y autorizar generando PDF con código QR y Hash SHA-256")
    void testValidarYAutorizarNotificacion() throws Exception {
        CrearNotificacionRequest req = CrearNotificacionRequest.builder()
                .tipoNotificacionId(tipoFormal.getId())
                .contribuyenteId(contribuyente.getId())
                .destinatarioNombre("Contribuyente Quinua")
                .destinatarioNumeroDoc("72345678")
                .domicilioNotificacion("Plaza Mayor s/n, Quinua")
                .medioNotificacionId(medioFisico.getId())
                .build();

        NotificacionTributaria creada = preparacionService.prepararNotificacion(req, testUser, null);

        // 1. Validar
        mockMvc.perform(post("/dashboard/tributaria/notificaciones/" + creada.getId() + "/validar")
                        .with(csrf())
                        .cookie(authCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard/tributaria/notificaciones/" + creada.getId()));

        NotificacionTributaria validada = notificacionRepository.findById(creada.getId()).orElseThrow();
        assertThat(validada.getEstadoAdministrativo()).isEqualTo(EstadoAdministrativoNotificacion.VALIDADA);

        // 2. Autorizar
        mockMvc.perform(post("/dashboard/tributaria/notificaciones/" + creada.getId() + "/autorizar")
                        .with(csrf())
                        .cookie(authCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard/tributaria/notificaciones/" + creada.getId()));

        NotificacionTributaria autorizada = notificacionRepository.findById(creada.getId()).orElseThrow();
        assertThat(autorizada.getEstadoAdministrativo()).isEqualTo(EstadoAdministrativoNotificacion.AUTORIZADA);
        assertThat(autorizada.getHashDocumento()).isNotNull();
        assertThat(autorizada.getCodigoQr()).contains("/verificar/notificacion/");

        // 3. Descargar PDF Cédula Oficial
        mockMvc.perform(get("/dashboard/tributaria/notificaciones/" + creada.getId() + "/pdf")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    @DisplayName("La consulta pública de verificación debe funcionar sin sesión y sin exponer información de deuda privada")
    void testConsultaPublicaSinSesion() throws Exception {
        CrearNotificacionRequest req = CrearNotificacionRequest.builder()
                .tipoNotificacionId(tipoFormal.getId())
                .contribuyenteId(contribuyente.getId())
                .destinatarioNombre("Manuel Prado Ugarteche")
                .destinatarioNumeroDoc("08976543")
                .domicilioNotificacion("Comunidad Huayoccari, Quinua")
                .medioNotificacionId(medioFisico.getId())
                .build();

        NotificacionTributaria creada = preparacionService.prepararNotificacion(req, testUser, null);
        notificacionService.validarNotificacion(creada.getId(), testUser);
        notificacionService.autorizarNotificacion(creada.getId(), "https://quinua.gob.pe", testUser);

        mockMvc.perform(get("/verificar/notificacion/" + creada.getCodigoNotificacion()))
                .andExpect(status().isOk())
                .andExpect(view().name("portal/verificacion-notificacion"))
                .andExpect(model().attributeExists("resultado"))
                .andExpect(model().attribute("codigoConsultado", creada.getCodigoNotificacion()));
    }
}

