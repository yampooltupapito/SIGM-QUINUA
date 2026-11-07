package pe.gob.municipalidad.sigm.modules.mesapartes;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.model.*;
import pe.gob.municipalidad.sigm.common.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("Pruebas de Integración - Atención, Resolución, Notificación y Cierre (Prompt 16)")
class AtencionResolucionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Autowired
    private PermisoRepository permisoRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    @Autowired
    private MunicipalidadRepository municipalidadRepository;

    @Autowired
    private PersonaOficinaRepository personaOficinaRepository;

    @Autowired
    private TipoActorRepository tipoActorRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private TipoTramiteRepository tipoTramiteRepository;

    @Autowired
    private ExpedienteRepository expedienteRepository;

    @Autowired
    private EvaluacionExpedienteRepository evaluacionRepository;

    @Autowired
    private PropuestaDecisionRepository propuestaRepository;

    @Autowired
    private DecisionExpedienteRepository decisionRepository;

    @Autowired
    private NotificacionAdministrativaRepository notificacionRepository;

    @Autowired
    private CierreExpedienteRepository cierreRepository;

    @Autowired
    private ArchivoExpedienteRepository archivoRepository;

    private Usuario operadorTrabajador;
    private Usuario jefeOficina;
    private Usuario ciudadano;
    private Usuario usuarioExterno;

    private Cookie trabajadorCookie;
    private Cookie jefeCookie;
    private Cookie ciudadanoCookie;
    private Cookie externoCookie;

    private Oficina oficinaDesarrolloUrbano;
    private Oficina oficinaRentas;
    private Expediente expedientePrueba;
    private Cargo cargoSubgerente;

    @BeforeEach
    void setUp() {
        Municipalidad muni = municipalidadRepository.findAll().stream().findFirst().orElseGet(() ->
                municipalidadRepository.save(Municipalidad.builder()
                        .nombre("Municipalidad Distrital de Quinua")
                        .nombreCorto("MD Quinua")
                        .ubigeo("050108")
                        .departamento("Ayacucho")
                        .provincia("Huamanga")
                        .distrito("Quinua")
                        .direccion("Plaza de Armas s/n, Quinua, Huamanga, Ayacucho")
                        .telefono("066-812345")
                        .correoInstitucional("mesadepartes@muniquinua.gob.pe")
                        .estado("ACTIVO")
                        .build())
        );

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 6);

        oficinaDesarrolloUrbano = oficinaRepository.save(Oficina.builder()
                .codigo("OFI-SDU-" + uniqueSuffix)
                .nombre("Subgerencia de Desarrollo Urbano")
                .sigla("SDU")
                .nivelJerarquico(2)
                .estado("ACTIVO")
                .municipalidad(muni)
                .build());

        oficinaRentas = oficinaRepository.save(Oficina.builder()
                .codigo("OFI-REN-" + uniqueSuffix)
                .nombre("Oficina de Rentas")
                .sigla("REN")
                .nivelJerarquico(2)
                .estado("ACTIVO")
                .municipalidad(muni)
                .build());

        TipoActor tipoActorServidor = tipoActorRepository.findAll().stream().findFirst().orElseGet(() ->
                tipoActorRepository.save(TipoActor.builder()
                        .codigo("SERVIDOR_PUBLICO")
                        .nombre("Servidor Público")
                        .descripcion("Trabajador municipal")
                        .build())
        );

        cargoSubgerente = cargoRepository.save(Cargo.builder()
                .codigo("CARGO-SGDU-" + uniqueSuffix)
                .nombre("Subgerente de Desarrollo Urbano")
                .nivelJerarquico(2)
                .estado("ACTIVO")
                .build());

        Rol rolOperador = rolRepository.findByCodigo("ROLE_MESA_PARTES_OPERADOR").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_MESA_PARTES_OPERADOR").nombre("Operador MPV").descripcion("Operador").estado("ACTIVO").build())
        );

        Rol rolJefe = rolRepository.findByCodigo("ROLE_JEFE_OFICINA").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_JEFE_OFICINA").nombre("Jefe de Oficina").descripcion("Jefe").estado("ACTIVO").build())
        );

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("Ciudadano").descripcion("Ciudadano").estado("ACTIVO").build())
        );

        String numDocSuffix = String.format("%06d", new Random().nextInt(999999));

        Persona perTrab = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("73" + numDocSuffix)
                .nombres("Especialista").apellidoPaterno("Tecnico").correo("esp." + uniqueSuffix + "@quinua.gob.pe").build());

        Persona perJefe = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("72" + numDocSuffix)
                .nombres("Jefe").apellidoPaterno("Resolutor").correo("jefe." + uniqueSuffix + "@quinua.gob.pe").build());

        Persona perCiu = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("74" + numDocSuffix)
                .nombres("Ciudadano").apellidoPaterno("Solicitante").correo("ciu." + uniqueSuffix + "@gmail.com").build());

        Persona perExt = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("75" + numDocSuffix)
                .nombres("Servidor").apellidoPaterno("Rentas").correo("rentas." + uniqueSuffix + "@quinua.gob.pe").build());

        operadorTrabajador = usuarioRepository.save(Usuario.builder()
                .username("trab_" + uniqueSuffix)
                .passwordHash(passwordEncoder.encode("Password2026!"))
                .persona(perTrab).roles(Set.of(rolOperador)).enabled(true).build());

        jefeOficina = usuarioRepository.save(Usuario.builder()
                .username("jefe_" + uniqueSuffix)
                .passwordHash(passwordEncoder.encode("Password2026!"))
                .persona(perJefe).roles(Set.of(rolJefe)).enabled(true).build());

        ciudadano = usuarioRepository.save(Usuario.builder()
                .username("ciu_" + uniqueSuffix)
                .passwordHash(passwordEncoder.encode("Password2026!"))
                .persona(perCiu).roles(Set.of(rolCiudadano)).enabled(true).build());

        usuarioExterno = usuarioRepository.save(Usuario.builder()
                .username("ext_" + uniqueSuffix)
                .passwordHash(passwordEncoder.encode("Password2026!"))
                .persona(perExt).roles(Set.of(rolOperador)).enabled(true).build());

        personaOficinaRepository.save(PersonaOficina.builder()
                .persona(perTrab).oficina(oficinaDesarrolloUrbano).tipoActor(tipoActorServidor)
                .fechaInicio(LocalDate.now()).principal(true).estado("ACTIVO").build());

        personaOficinaRepository.save(PersonaOficina.builder()
                .persona(perJefe).oficina(oficinaDesarrolloUrbano).cargo(cargoSubgerente).tipoActor(tipoActorServidor)
                .fechaInicio(LocalDate.now()).principal(true).estado("ACTIVO").build());

        personaOficinaRepository.save(PersonaOficina.builder()
                .persona(perExt).oficina(oficinaRentas).tipoActor(tipoActorServidor)
                .fechaInicio(LocalDate.now()).principal(true).estado("ACTIVO").build());

        // Cookies JWT con permisos granulares de Prompt 16
        List<String> permisosTrabajador = List.of(
                "MESAPARTES_ATENCION_VER", "MESAPARTES_ATENCION_INICIAR",
                "MESAPARTES_EVALUACION_CREAR", "MESAPARTES_EVALUACION_FINALIZAR",
                "MESAPARTES_PROPUESTA_CREAR", "MESAPARTES_NOTIFICACION_GESTIONAR",
                "MESAPARTES_EXPEDIENTE_ARCHIVAR"
        );

        List<String> permisosJefe = List.of(
                "MESAPARTES_ATENCION_VER", "MESAPARTES_ATENCION_INICIAR",
                "MESAPARTES_PROPUESTA_REVISAR", "MESAPARTES_PROPUESTA_APROBAR",
                "MESAPARTES_DECISION_EMITIR", "MESAPARTES_DOCUMENTO_GENERAR",
                "MESAPARTES_DOCUMENTO_FORMALIZAR", "MESAPARTES_NOTIFICACION_GESTIONAR",
                "MESAPARTES_EXPEDIENTE_CERRAR", "MESAPARTES_EXPEDIENTE_ARCHIVAR",
                "MESAPARTES_EXPEDIENTE_REABRIR"
        );

        trabajadorCookie = buildJwtCookie(operadorTrabajador, "ROLE_MESA_PARTES_OPERADOR", permisosTrabajador);
        jefeCookie = buildJwtCookie(jefeOficina, "ROLE_JEFE_OFICINA", permisosJefe);
        ciudadanoCookie = buildJwtCookie(ciudadano, "ROLE_CIUDADANO", List.of("MESAPARTES_SOLICITUD_CONSULTAR"));
        externoCookie = buildJwtCookie(usuarioExterno, "ROLE_MESA_PARTES_OPERADOR", permisosTrabajador);

        // Crear expediente en estado DERIVADO asignado al trabajador
        TipoTramite tramite = tipoTramiteRepository.findAll().stream().findFirst().orElseGet(() ->
                tipoTramiteRepository.save(TipoTramite.builder()
                        .codigo("TRAM-LIC-" + uniqueSuffix)
                        .nombre("Licencia de Edificación")
                        .descripcion("Trámite de edificación")
                        .plazoDias(30)
                        .unidadPlazo(UnidadPlazo.DIAS_HABILES)
                        .estado("ACTIVO")
                        .build())
        );

        expedientePrueba = expedienteRepository.save(Expediente.builder()
                .numeroExpediente("EXP-2026-" + String.format("%06d", new Random().nextInt(999999)))
                .anio(2026)
                .asunto("Solicitud de Licencia de Edificación Tipo C")
                .tipoTramite(tramite)
                .solicitante(perCiu)
                .oficinaResponsable(oficinaDesarrolloUrbano)
                .responsableActual(operadorTrabajador)
                .estadoActual(EstadoExpediente.DERIVADO)
                .canalIngreso(CanalIngreso.VIRTUAL)
                .prioridad(PrioridadExpediente.NORMAL)
                .confidencialidad(ConfidencialidadExpediente.PUBLICO)
                .fechaPresentacion(OffsetDateTime.now())
                .fechaRecepcion(OffsetDateTime.now())
                .build());
    }

    private Cookie buildJwtCookie(Usuario usuario, String role, List<String> permissions) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));
        for (String p : permissions) {
            authorities.add(new SimpleGrantedAuthority(p));
        }

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .personaId(usuario.getPersona() != null ? usuario.getPersona().getId() : null)
                .nombreCompleto(usuario.getPersona() != null ? usuario.getPersona().getNombreCompleto() : usuario.getUsername())
                .roles(Set.of(role))
                .permisos(new HashSet<>(permissions))
                .authorities(authorities)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        String token = jwtTokenProvider.generateAccessToken(userDetails);
        Cookie cookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, token);
        cookie.setPath("/");
        return cookie;
    }

    @Test
    @DisplayName("Flujo Completo: Atención -> Evaluación -> Propuesta -> Aprobación -> Decisión -> PDF -> Emisión -> Notificación -> Cierre -> Archivo -> Reapertura")
    void testFlujoCompletoAtencionYCierre() throws Exception {
        Long expId = expedientePrueba.getId();

        // 1. Consultar bandeja de atención HTML y REST
        mockMvc.perform(get("/dashboard/mesa-partes/atencion").cookie(trabajadorCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/atencion"));

        mockMvc.perform(get("/api/mesa-partes/atencion").cookie(trabajadorCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 2. Rechazar inicio de atención para expediente ajeno
        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/iniciar-atencion")
                        .cookie(externoCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(IniciarAtencionRequest.builder().indicaciones("Intento no autorizado").build())))
                .andExpect(status().isForbidden());

        // 3. Iniciar atención por el servidor asignado
        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/iniciar-atencion")
                        .cookie(trabajadorCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(IniciarAtencionRequest.builder().indicaciones("Inicio formal de sustanciación").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.estado").value("EN_ATENCION"));

        // 4. Registrar evaluación técnica
        CrearEvaluacionRequest evalReq = CrearEvaluacionRequest.builder()
                .tipoEvaluacion(TipoEvaluacionExpediente.TECNICA)
                .descripcion("Inspección de planos y cálculo estructural")
                .conclusiones("Cumple los parámetros urbanísticos vigentes")
                .recomendacion("Proceder con informe favorable")
                .build();

        MvcResult evalResult = mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/evaluaciones")
                        .cookie(trabajadorCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evalReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.estado").value("BORRADOR"))
                .andReturn();

        Long evalId = objectMapper.readTree(evalResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // 5. Finalizar evaluación
        mockMvc.perform(post("/api/mesa-partes/evaluaciones/" + evalId + "/finalizar")
                        .cookie(trabajadorCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("FINALIZADA"));

        // 6. Elaborar propuesta de decisión
        CrearPropuestaRequest propReq = CrearPropuestaRequest.builder()
                .tipoDecision(TipoDecisionExpediente.RESOLUCION_SUBGERENCIAL)
                .resultadoPropuesto(ResultadoDecision.FAVORABLE)
                .fundamento("Conforme a evaluación técnica N° 1 y TUPA municipal")
                .descripcion("Otorgar la licencia de edificación solicitada")
                .build();

        MvcResult propResult = mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/propuestas")
                        .cookie(trabajadorCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(propReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.estado").value("BORRADOR"))
                .andReturn();

        Long propId = objectMapper.readTree(propResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // 7. Elevar propuesta a revisión
        mockMvc.perform(post("/api/mesa-partes/propuestas/" + propId + "/enviar-revision")
                        .cookie(trabajadorCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("EN_REVISION"));

        // 8. Validar separación de funciones: el elaborador no puede autoaprobarse su propuesta
        RevisarPropuestaRequest autoAprobarReq = RevisarPropuestaRequest.builder()
                .accion(AccionRevisionPropuesta.APROBADA_INTERNAMENTE)
                .observaciones("Intento autoaprobar mi propuesta")
                .build();

        mockMvc.perform(post("/api/mesa-partes/propuestas/" + propId + "/aprobar")
                        .cookie(trabajadorCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(autoAprobarReq)))
                .andExpect(status().isForbidden()); // Falta permiso de aprobación para trabajador

        // 9. Jefe de oficina aprueba internamente la propuesta
        RevisarPropuestaRequest jefeAprobarReq = RevisarPropuestaRequest.builder()
                .accion(AccionRevisionPropuesta.APROBADA_INTERNAMENTE)
                .observaciones("Revisado y conforme con normatividad urbanística")
                .build();

        mockMvc.perform(post("/api/mesa-partes/propuestas/" + propId + "/aprobar")
                        .cookie(jefeCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jefeAprobarReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("APROBADA_INTERNAMENTE"));

        // 10. Registrar decisión administrativa formal
        RegistrarDecisionRequest decisionReq = RegistrarDecisionRequest.builder()
                .propuestaId(propId)
                .tipoDecision(TipoDecisionExpediente.RESOLUCION_SUBGERENCIAL)
                .resultado(ResultadoDecision.FAVORABLE)
                .fundamento("Se aprueba conforme al Informe Técnico y la propuesta validada")
                .build();

        MvcResult decResult = mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/decisiones")
                        .cookie(jefeCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(decisionReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.estadoEmision").value("BORRADOR"))
                .andReturn();

        Long decisionId = objectMapper.readTree(decResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // 11. Generar documento PDF oficial
        mockMvc.perform(post("/api/mesa-partes/decisiones/" + decisionId + "/generar-documento")
                        .cookie(jefeCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentoId").isNumber());

        // 12. Descargar PDF oficial generado
        mockMvc.perform(get("/dashboard/mesa-partes/decisiones/" + decisionId + "/descargar-pdf")
                        .cookie(jefeCookie))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));

        // 13. Registrar emisión y formalización legal
        FormalizarDecisionRequest formReq = FormalizarDecisionRequest.builder()
                .numeroDocumento("RES-SUBGERENCIAL-0001-2026-MDQ/SDU")
                .evidenciaFormalizacion("Firmado manualmente y sellado en libro de resoluciones N° 14")
                .build();

        mockMvc.perform(post("/api/mesa-partes/decisiones/" + decisionId + "/registrar-emision")
                        .cookie(jefeCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(formReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoEmision").value("EMITIDO"));

        // Verificar que el expediente pasó a RESUELTO
        Expediente expResuelto = expedienteRepository.findById(expId).orElseThrow();
        assertThat(expResuelto.getEstadoActual()).isEqualTo(EstadoExpediente.RESUELTO);
        assertThat(expResuelto.getResultadoFinal()).isEqualTo("FAVORABLE");

        // 14. Registrar cédula de notificación administrativa
        RegistrarNotificacionRequest notifReq = RegistrarNotificacionRequest.builder()
                .decisionId(decisionId)
                .destinatarioNombre(ciudadano.getNombreCompleto())
                .destinatarioDocumento(ciudadano.getPersona().getNumeroDocumento())
                .destinatarioDireccion("Jr. Sucre 450, Quinua")
                .medioNotificacion(MedioNotificacion.CASILLA_ELECTRONICA)
                .observaciones("Notificación electrónica de resolución favorable")
                .build();

        MvcResult notifResult = mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/notificaciones")
                        .cookie(jefeCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notifReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.estadoNotificacion").value("PENDIENTE"))
                .andReturn();

        Long notifId = objectMapper.readTree(notifResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // 15. Acreditar legalmente la notificación con acuse de recibo
        mockMvc.perform(multipart("/api/mesa-partes/notificaciones/" + notifId + "/acreditar")
                        .param("numeroAcuseRecibo", "ACUSE-DIG-2026-0044")
                        .param("observaciones", "Constancia firmada con confirmación de lectura en casilla")
                        .cookie(jefeCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoNotificacion").value("NOTIFICADA_VALIDA"))
                .andExpect(jsonPath("$.data.esLegalmenteValida").value(true));

        // Verificar que el expediente pasó a NOTIFICADO
        Expediente expNotificado = expedienteRepository.findById(expId).orElseThrow();
        assertThat(expNotificado.getEstadoActual()).isEqualTo(EstadoExpediente.NOTIFICADO);

        // 16. Cierre administrativo del expediente
        CerrarExpedienteRequest cierreReq = CerrarExpedienteRequest.builder()
                .tipoCierre(TipoCierreExpediente.ATENCION_CONCLUIDA)
                .motivoCierre("Procedimiento culminado en todos sus extremos conforme a la LPAG Ley 27444.")
                .decisionId(decisionId)
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/cerrar")
                        .cookie(jefeCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cierreReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tipoCierre").value("ATENCION_CONCLUIDA"));

        // 17. Archivo administrativo con código y ubicación física
        ArchivarExpedienteRequest archReq = ArchivarExpedienteRequest.builder()
                .codigoArchivador("ARCH-2026-04")
                .ubicacionFisica("Depósito Central, Estante 2, Nivel 3, Caja 12")
                .tomoFolio("Tomo II, Folio 154")
                .motivoArchivo("Custodia pasiva del expediente administrativo resuelto")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/archivar")
                        .cookie(jefeCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(archReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ubicacionFisica").value("Depósito Central, Estante 2, Nivel 3, Caja 12"));

        Expediente expArchivado = expedienteRepository.findById(expId).orElseThrow();
        assertThat(expArchivado.getEstadoActual()).isEqualTo(EstadoExpediente.ARCHIVADO);

        // 18. Reapertura administrativa justificada
        ReabrirExpedienteRequest reabrirReq = ReabrirExpedienteRequest.builder()
                .motivoLegal("Resolución de Alcaldía dispone revisión excepcional por recurso de reconsideración formal")
                .actoReaperturaReferencia("RA-0042-2026-MDQ/ALC")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/reabrir")
                        .cookie(jefeCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reabrirReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.actoReaperturaReferencia").value("RA-0042-2026-MDQ/ALC"));

        Expediente expReabierto = expedienteRepository.findById(expId).orElseThrow();
        assertThat(expReabierto.getEstadoActual()).isEqualTo(EstadoExpediente.EN_ATENCION);
        assertThat(expReabierto.getEsReabierto()).isTrue();

        // 19. Consultar estadísticas agregadas
        mockMvc.perform(get("/api/mesa-partes/atencion/estadisticas").cookie(jefeCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.expedientesEnAtencion").isNumber())
                .andExpect(jsonPath("$.data.expedientesResueltos").isNumber());

        // 20. Detalle de atención Thymeleaf
        mockMvc.perform(get("/dashboard/mesa-partes/atencion/" + expId).cookie(jefeCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/detalle-atencion"));

        // 21. Bandeja de propuestas Thymeleaf
        mockMvc.perform(get("/dashboard/mesa-partes/propuestas").cookie(jefeCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/propuestas"));
    }
}
