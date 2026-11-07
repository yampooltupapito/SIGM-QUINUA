package pe.gob.municipalidad.sigm.common.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.model.Cargo;
import pe.gob.municipalidad.sigm.common.model.Municipalidad;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.PersonaOficina;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.TipoActor;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EmailRequestDto;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EnviarNotificacionOficinaRequest;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EnviarNotificacionRequest;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.NotificacionItemDto;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.NotificacionPreferenciasDto;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.ProgramarNotificacionRequest;
import pe.gob.municipalidad.sigm.common.notification.entity.NotificacionEnvio;
import pe.gob.municipalidad.sigm.common.notification.entity.NotificacionProgramacion;
import pe.gob.municipalidad.sigm.common.notification.provider.MockMailProvider;
import pe.gob.municipalidad.sigm.common.notification.repository.NotificacionEnvioRepository;
import pe.gob.municipalidad.sigm.common.notification.repository.NotificacionIntentoRepository;
import pe.gob.municipalidad.sigm.common.notification.repository.NotificacionPreferenciasRepository;
import pe.gob.municipalidad.sigm.common.notification.repository.NotificacionProgramacionRepository;
import pe.gob.municipalidad.sigm.common.notification.service.CentralNotificationService;
import pe.gob.municipalidad.sigm.common.notification.service.EmailService;
import pe.gob.municipalidad.sigm.common.repository.CargoRepository;
import pe.gob.municipalidad.sigm.common.repository.MunicipalidadRepository;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.TipoActorRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.repository.AuditoriaAdministrativaRepository;
import pe.gob.municipalidad.sigm.modules.notificaciones.model.Notificacion;
import pe.gob.municipalidad.sigm.modules.notificaciones.repository.NotificacionRepository;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NotificacionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    @Autowired
    private PersonaOficinaRepository personaOficinaRepository;

    @Autowired
    private TipoActorRepository tipoActorRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private MunicipalidadRepository municipalidadRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private NotificacionEnvioRepository envioRepository;

    @Autowired
    private NotificacionIntentoRepository intentoRepository;

    @Autowired
    private NotificacionProgramacionRepository programacionRepository;

    @Autowired
    private NotificacionPreferenciasRepository preferenciasRepository;

    @Autowired
    private AuditoriaAdministrativaRepository auditoriaRepository;

    @Autowired
    private CentralNotificationService centralNotificationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private MockMailProvider mockMailProvider;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private Usuario adminUser;
    private Usuario regularUser;
    private Cookie adminCookie;
    private Cookie regularCookie;
    private Oficina oficinaCentral;

    @BeforeEach
    void setUp() {
        mockMailProvider.limpiarBandejaSimulada();
        mockMailProvider.setSimularFallo(false);

        Municipalidad muni = municipalidadRepository.findAll().stream().findFirst().orElseGet(() ->
                municipalidadRepository.save(Municipalidad.builder()
                        .nombre("Municipalidad Distrital de Quinua")
                        .nombreCorto("MDQ")
                        .ubigeo("050108")
                        .departamento("Ayacucho")
                        .provincia("Huamanga")
                        .distrito("Quinua")
                        .build()));

        oficinaCentral = oficinaRepository.findByCodigo("SEC-GEN").orElseGet(() ->
                oficinaRepository.save(Oficina.builder()
                        .municipalidad(muni)
                        .codigo("SEC-GEN")
                        .nombre("Secretaría General")
                        .sigla("SG")
                        .nivelJerarquico(2)
                        .estado("ACTIVO")
                        .build()));

        TipoActor tipoServidor = tipoActorRepository.findByCodigo("SERVIDOR_PUBLICO").orElseGet(() ->
                tipoActorRepository.save(TipoActor.builder()
                        .codigo("SERVIDOR_PUBLICO")
                        .nombre("Servidor Público")
                        .estado("ACTIVO")
                        .build()));

        Cargo cargo = cargoRepository.findByCodigo("ESP_NOTIF").orElseGet(() ->
                cargoRepository.save(Cargo.builder()
                        .codigo("ESP_NOTIF")
                        .nombre("Especialista en Notificaciones")
                        .estado("ACTIVO")
                        .build()));

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("ADMINISTRADOR").estado("ACTIVO").build()));

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("CIUDADANO").estado("ACTIVO").build()));

        // Usuario Administrador
        String adminDoc = "7" + UUID.randomUUID().toString().replaceAll("\\D", "").substring(0, 7);
        Persona pAdmin = personaRepository.save(Persona.builder()
                .numeroDocumento(adminDoc)
                .tipoDocumento("DNI")
                .nombres("Carlos")
                .apellidoPaterno("Alvarez")
                .apellidoMaterno("Quispe")
                .correo("admin.notif@muniquinua.gob.pe")
                .estado("ACTIVO")
                .build());

        String adminUsername = "admin_" + UUID.randomUUID().toString().substring(0, 8);
        adminUser = usuarioRepository.save(Usuario.builder()
                .persona(pAdmin)
                .username(adminUsername)
                .passwordHash(passwordEncoder.encode("Password123*"))
                .roles(Set.of(rolAdmin))
                .enabled(true)
                .accountLocked(false)
                .build());

        // Asignación de oficina para admin
        personaOficinaRepository.save(PersonaOficina.builder()
                .persona(pAdmin)
                .oficina(oficinaCentral)
                .cargo(cargo)
                .tipoActor(tipoServidor)
                .fechaInicio(LocalDate.now().minusMonths(6))
                .principal(true)
                .estado("ACTIVO")
                .build());

        // Usuario Regular
        String regDoc = "8" + UUID.randomUUID().toString().replaceAll("\\D", "").substring(0, 7);
        Persona pReg = personaRepository.save(Persona.builder()
                .numeroDocumento(regDoc)
                .tipoDocumento("DNI")
                .nombres("Rosa")
                .apellidoPaterno("Mendoza")
                .apellidoMaterno("Flores")
                .correo("rosa.mendoza@gmail.com")
                .estado("ACTIVO")
                .build());

        String regUsername = "user_" + UUID.randomUUID().toString().substring(0, 8);
        regularUser = usuarioRepository.save(Usuario.builder()
                .persona(pReg)
                .username(regUsername)
                .passwordHash(passwordEncoder.encode("Password123*"))
                .roles(Set.of(rolCiudadano))
                .enabled(true)
                .accountLocked(false)
                .build());

        // Tokens JWT
        CustomUserDetails adminDetails = CustomUserDetails.builder()
                .id(adminUser.getId())
                .username(adminUser.getUsername())
                .password(adminUser.getPasswordHash())
                .enabled(true)
                .accountNonLocked(true)
                .authorities(List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"),
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ADMINISTRACION_MANAGE"),
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("NOTIFICACION_MANAGE")
                ))
                .build();
        adminCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, jwtTokenProvider.generateAccessToken(adminDetails));
        adminCookie.setPath("/");

        CustomUserDetails regDetails = CustomUserDetails.builder()
                .id(regularUser.getId())
                .username(regularUser.getUsername())
                .password(regularUser.getPasswordHash())
                .enabled(true)
                .accountNonLocked(true)
                .authorities(List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CIUDADANO")
                ))
                .build();
        regularCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, jwtTokenProvider.generateAccessToken(regDetails));
        regularCookie.setPath("/");
    }

    @Test
    @DisplayName("1 & 6. Crear notificación interna y calcular contador de no leídas")
    void testCrearNotificacionYContadorNoLeidas() {
        EnviarNotificacionRequest req = EnviarNotificacionRequest.builder()
                .usuarioId(regularUser.getId())
                .titulo("Expediente N° 2026-0089 Ingresado")
                .mensaje("Su trámite de licencia ha sido ingresado correctamente en Mesa de Partes.")
                .tipo("TRAMITE")
                .urlDestino("/tramites/2026-0089")
                .enviarCorreo(false)
                .build();

        NotificacionItemDto creado = centralNotificationService.enviarNotificacion(req, "MESA_DE_PARTES");

        assertThat(creado).isNotNull();
        assertThat(creado.getId()).isNotNull();
        assertThat(creado.getTitulo()).isEqualTo("Expediente N° 2026-0089 Ingresado");
        assertThat(creado.isLeida()).isFalse();

        long noLeidas = centralNotificationService.contarNoLeidas(regularUser.getUsername());
        assertThat(noLeidas).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("2, 7 & 8. Consultar notificaciones propias vía REST con filtros y paginación")
    void testListarNotificacionesPropiasFiltrosYPaginacion() throws Exception {
        // Crear 3 notificaciones para regularUser
        for (int i = 1; i <= 3; i++) {
            notificacionRepository.save(Notificacion.builder()
                    .usuario(regularUser)
                    .titulo("Aviso de Trámite " + i)
                    .mensaje("Mensaje descriptivo del trámite " + i)
                    .tipo(i == 1 ? "TRAMITE" : (i == 2 ? "SEGURIDAD" : "PAGO"))
                    .leida(i == 3)
                    .fechaCreacion(OffsetDateTime.now().minusMinutes(i))
                    .build());
        }

        // Listar página 0 con size 2
        mockMvc.perform(get("/api/notificaciones")
                        .param("page", "0")
                        .param("size", "2")
                        .cookie(regularCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notificaciones", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.data.noLeidas", greaterThanOrEqualTo(2)));

        // Filtrar por categoría TRAMITE
        mockMvc.perform(get("/api/notificaciones")
                        .param("tipo", "TRAMITE")
                        .cookie(regularCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notificaciones[0].tipo").value("TRAMITE"));

        // Filtrar por término de búsqueda
        mockMvc.perform(get("/api/notificaciones")
                        .param("search", "Trámite 1")
                        .cookie(regularCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notificaciones", hasSize(1)))
                .andExpect(jsonPath("$.data.notificaciones[0].titulo", containsString("Trámite 1")));
    }

    @Test
    @DisplayName("3. Rechazar acceso y manipulación de notificaciones ajenas (Prevención IDOR)")
    void testRechazarAccesoNotificacionesAjenas() throws Exception {
        Notificacion notifAdmin = notificacionRepository.save(Notificacion.builder()
                .usuario(adminUser)
                .titulo("Confidencial Alcaldía")
                .mensaje("Resolución reservada de la Municipalidad de Quinua.")
                .tipo("SEGURIDAD")
                .leida(false)
                .fechaCreacion(OffsetDateTime.now())
                .build());

        // RegularUser intenta consultar la notificación de adminUser vía GET
        mockMvc.perform(get("/api/notificaciones/" + notifAdmin.getId())
                        .cookie(regularCookie))
                .andExpect(status().isForbidden());

        // RegularUser intenta marcar como leída la notificación de adminUser vía PATCH
        mockMvc.perform(patch("/api/notificaciones/" + notifAdmin.getId() + "/leer")
                        .with(csrf())
                        .cookie(regularCookie))
                .andExpect(status().isForbidden());

        // En servicio directo debe lanzar AccessDeniedException
        assertThatThrownBy(() -> centralNotificationService.obtenerNotificacionPorId(notifAdmin.getId(), regularUser.getUsername()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("4 & 5. Marcar notificación como leída y marcar todas como leídas")
    void testMarcarLeidaYMarcarTodasLeidas() throws Exception {
        Notificacion notif = notificacionRepository.save(Notificacion.builder()
                .usuario(regularUser)
                .titulo("Notificación de Lectura")
                .mensaje("Prueba de actualización de estado")
                .tipo("INFORMACION")
                .leida(false)
                .fechaCreacion(OffsetDateTime.now())
                .build());

        // Marcar individual vía PATCH
        mockMvc.perform(patch("/api/notificaciones/" + notif.getId() + "/leer")
                        .with(csrf())
                        .cookie(regularCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Notificacion actualizada = notificacionRepository.findById(notif.getId()).orElseThrow();
        assertThat(actualizada.isLeida()).isTrue();
        assertThat(actualizada.getFechaLectura()).isNotNull();

        // Crear dos más no leídas y marcar todas
        notificacionRepository.save(Notificacion.builder().usuario(regularUser).titulo("N1").mensaje("M1").tipo("INFO").leida(false).fechaCreacion(OffsetDateTime.now()).build());
        notificacionRepository.save(Notificacion.builder().usuario(regularUser).titulo("N2").mensaje("M2").tipo("INFO").leida(false).fechaCreacion(OffsetDateTime.now()).build());

        mockMvc.perform(patch("/api/notificaciones/leer-todas")
                        .with(csrf())
                        .cookie(regularCookie))
                .andExpect(status().isOk());

        long pendientes = centralNotificationService.contarNoLeidas(regularUser.getUsername());
        assertThat(pendientes).isEqualTo(0L);
    }

    @Test
    @DisplayName("9. Preferencias de usuario: Actualización y prohibición de desactivar alertas de seguridad")
    void testPreferenciasYProteccionAlertasSeguridad() throws Exception {
        // Consultar preferencias iniciales
        mockMvc.perform(get("/api/notificaciones/preferencias").cookie(regularCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alertasSeguridad").value(true))
                .andExpect(jsonPath("$.data.canalInterno").value(true));

        // Intento malicioso de desactivar alertasSeguridad
        NotificacionPreferenciasDto updateReq = NotificacionPreferenciasDto.builder()
                .canalInterno(true)
                .canalEmail(false)
                .alertasSeguridad(false) // Intento de desactivar seguridad
                .avisosTramites(false)
                .avisosDocumentos(true)
                .avisosPagos(false)
                .comunicacionesGenerales(false)
                .build();

        mockMvc.perform(patch("/api/notificaciones/preferencias")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq))
                        .cookie(regularCookie))
                .andExpect(status().isOk())
                // La regla institucional DEBE mantener alertasSeguridad en TRUE
                .andExpect(jsonPath("$.data.alertasSeguridad").value(true))
                .andExpect(jsonPath("$.data.canalEmail").value(false))
                .andExpect(jsonPath("$.data.avisosTramites").value(false));

        NotificacionPreferenciasDto dbPrefs = centralNotificationService.obtenerOInicializarPreferencias(regularUser.getUsername());
        assertThat(dbPrefs.getAlertasSeguridad()).isTrue();
        assertThat(dbPrefs.getCanalEmail()).isFalse();
    }

    @Test
    @DisplayName("11 & 15. Encolado de correo en Outbox y control estricto de Idempotencia")
    void testEncoladoOutboxEIdempotencia() {
        String idemKey = "pago-recibo-" + UUID.randomUUID();
        Map<String, Object> vars = new HashMap<>();
        vars.put("nombreDestinatario", "Rosa Mendoza");
        vars.put("mensaje", "Su pago de arbitrios ha sido registrado.");
        vars.put("enlaceAccion", "/pagos/recibo-100");

        EmailRequestDto req = EmailRequestDto.builder()
                .destinatarioCorreo(regularUser.getPersona().getCorreo())
                .destinatarioNombre("Rosa Mendoza")
                .usuarioId(regularUser.getId())
                .plantillaCodigo("NUEVA_NOTIFICACION")
                .asuntoPersonalizado("Recibo de Pago Confirmado")
                .variables(vars)
                .idempotencyKey(idemKey)
                .tipo("PAGO")
                .build();

        NotificacionEnvio primero = emailService.encolarCorreo(req);
        assertThat(primero).isNotNull();
        assertThat(primero.getId()).isNotNull();
        assertThat(primero.getIdempotencyKey()).isEqualTo(idemKey);

        // Segundo intento con la misma clave de idempotencia
        NotificacionEnvio segundo = emailService.encolarCorreo(req);
        assertThat(segundo.getId()).isEqualTo(primero.getId());

        long count = envioRepository.count();
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("12, 13 & 14. Procesamiento de correo Outbox, fallo simulado, reintentos y límite de intentos")
    void testProcesamientoFalloReintentoYLimiteIntentos() {
        // Encolar correo
        EmailRequestDto req = EmailRequestDto.builder()
                .destinatarioCorreo("test.fallo@muniquinua.gob.pe")
                .destinatarioNombre("Contribuyente Quinua")
                .plantillaCodigo("AVISO_INSTITUCIONAL")
                .asuntoPersonalizado("Comunicado Oficial")
                .variables(Map.of("nombreDestinatario", "Contribuyente", "mensaje", "Aviso cívico"))
                .idempotencyKey("fallo-test-" + UUID.randomUUID())
                .build();

        // Simular fallo en proveedor de correo
        mockMailProvider.setSimularFallo(true);

        NotificacionEnvio envio = emailService.encolarCorreo(req);

        // Despacho 1: Falla y queda PENDIENTE con backoff
        emailService.despacharEnvioDirecto(envio.getId());
        entityManager.flush();
        entityManager.clear();

        NotificacionEnvio intento1 = envioRepository.findById(envio.getId()).orElseThrow();
        assertThat(intento1.getEstado()).isEqualTo("PENDIENTE");
        assertThat(intento1.getIntentos()).isEqualTo(1);
        assertThat(intento1.getUltimoError()).contains("Fallo simulado");
        assertThat(intento1.getProximoIntento()).isAfter(OffsetDateTime.now());

        // Forzar segundo intento
        intento1.setProximoIntento(OffsetDateTime.now().minusMinutes(1));
        envioRepository.saveAndFlush(intento1);
        emailService.despacharEnvioDirecto(intento1.getId());
        entityManager.clear();

        NotificacionEnvio intento2 = envioRepository.findById(envio.getId()).orElseThrow();
        assertThat(intento2.getIntentos()).isEqualTo(2);

        // Forzar tercer intento (alcanza maxIntentos = 3) -> Estado FALLIDO
        intento2.setProximoIntento(OffsetDateTime.now().minusMinutes(1));
        envioRepository.saveAndFlush(intento2);
        emailService.despacharEnvioDirecto(intento2.getId());
        entityManager.clear();

        NotificacionEnvio intentoFinal = envioRepository.findById(envio.getId()).orElseThrow();
        assertThat(intentoFinal.getIntentos()).isEqualTo(3);
        assertThat(intentoFinal.getEstado()).isEqualTo("FALLIDO");

        // Reintento manual restablece a PENDIENTE
        emailService.reintentarEnvio(intentoFinal.getId());
        entityManager.clear();
        NotificacionEnvio reintentado = envioRepository.findById(envio.getId()).orElseThrow();
        assertThat(reintentado.getEstado()).isEqualTo("PENDIENTE");
        assertThat(reintentado.getIntentos()).isEqualTo(0);
    }

    @Test
    @DisplayName("16 & 17. Programación y cancelación de notificaciones diferidas")
    void testProgramacionYCancelacionNotificacion() throws Exception {
        ProgramarNotificacionRequest progReq = ProgramarNotificacionRequest.builder()
                .usuarioId(regularUser.getId())
                .titulo("Recordatorio de Pago de Arbitrios")
                .mensaje("Estimado vecino, recuerde que sus arbitrios vencen pronto.")
                .tipo("PAGO")
                .canal("INTERNO")
                .fechaProgramada(OffsetDateTime.now().plusDays(2))
                .build();

        String jsonResp = mockMvc.perform(post("/api/notificaciones/programar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(progReq))
                        .cookie(adminCookie))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn().getResponse().getContentAsString();

        Long progId = objectMapper.readTree(jsonResp).path("data").path("id").asLong();

        // Cancelar programación
        mockMvc.perform(delete("/api/notificaciones/programadas/" + progId)
                        .with(csrf())
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        NotificacionProgramacion cancelada = programacionRepository.findById(progId).orElseThrow();
        assertThat(cancelada.getCancelada()).isTrue();
    }

    @Test
    @DisplayName("18. Notificación masiva por oficina a usuarios activos")
    void testNotificacionPorOficina() throws Exception {
        EnviarNotificacionOficinaRequest oficinaReq = EnviarNotificacionOficinaRequest.builder()
                .oficinaId(oficinaCentral.getId())
                .titulo("Convocatoria a Reunión Extraordinaria")
                .mensaje("Se cita a todo el personal de la oficina a reunión técnica.")
                .tipo("INFORMACION")
                .canal("INTERNO")
                .build();

        mockMvc.perform(post("/api/notificaciones/oficina")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oficinaReq))
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.destinatariosNotificados", greaterThanOrEqualTo(1)));

        long notifsAdmin = notificacionRepository.countByUsuarioIdAndLeidaFalse(adminUser.getId());
        assertThat(notifsAdmin).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("19 & 22. Control de permisos y seguridad: Usuario sin rol administrativo rechazado")
    void testControlDePermisosEndpointsAdministrativos() throws Exception {
        EnviarNotificacionOficinaRequest oficinaReq = EnviarNotificacionOficinaRequest.builder()
                .oficinaId(oficinaCentral.getId())
                .titulo("Aviso no autorizado")
                .mensaje("Texto")
                .build();

        // RegularUser intenta emitir aviso de oficina -> 403 Forbidden
        mockMvc.perform(post("/api/notificaciones/oficina")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oficinaReq))
                        .cookie(regularCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("21. Auditoría administrativa permanente registrada en acciones clave")
    void testRegistroAuditoriaAdministrativa() {
        EnviarNotificacionRequest req = EnviarNotificacionRequest.builder()
                .usuarioId(regularUser.getId())
                .titulo("Aviso con Auditoría")
                .mensaje("Notificación de prueba")
                .tipo("INFORMACION")
                .build();

        centralNotificationService.enviarNotificacion(req, "AUDIT_TEST");

        assertThat(auditoriaRepository.findAll().stream()
                .anyMatch(a -> "CREAR_NOTIFICACION".equals(a.getAccion())))
                .isTrue();
    }

    @Test
    @DisplayName("23 & 24. Renderizado Thymeleaf del Centro de Notificaciones (/notificaciones)")
    void testRenderizadoCentroNotificacionesWeb() throws Exception {
        // Acceso anónimo denegado
        mockMvc.perform(get("/notificaciones"))
                .andExpect(status().is3xxRedirection());

        // Acceso autenticado retorna HTTP 200 con vista Thymeleaf renderizada
        mockMvc.perform(get("/notificaciones").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Centro de Notificaciones y Alertas")))
                .andExpect(content().string(containsString("Municipalidad Distrital de Quinua")))
                .andExpect(content().string(containsString("Bandeja Oficial SIGM-QUINUA")));
    }
}
