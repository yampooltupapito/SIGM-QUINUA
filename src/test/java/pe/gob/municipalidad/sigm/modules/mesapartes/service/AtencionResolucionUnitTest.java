package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import pe.gob.municipalidad.sigm.common.document.pdf.QrCodeGenerator;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.document.service.DocumentoService;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.*;
import pe.gob.municipalidad.sigm.common.notification.service.CentralNotificationService;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.impl.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.workflow.ExpedienteWorkflowService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - Atención, Resolución, Notificación y Cierre (Prompt 16)")
class AtencionResolucionUnitTest {

    @Mock
    private ExpedienteRepository expedienteRepository;
    @Mock
    private ExpedienteWorkflowService workflowService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PersonaOficinaRepository personaOficinaRepository;
    @Mock
    private CentralNotificationService notificationService;
    @Mock
    private EvaluacionExpedienteService evaluacionService;
    @Mock
    private PropuestaDecisionService propuestaService;
    @Mock
    private DecisionExpedienteService decisionService;
    @Mock
    private NotificacionResultadoService notificacionResultadoService;
    @Mock
    private CierreExpedienteService cierreService;
    @Mock
    private ArchivoExpedienteService archivoService;

    // Services under test
    private AtencionExpedienteServiceImpl atencionExpedienteService;

    @Mock
    private EvaluacionExpedienteRepository evaluacionRepository;
    @Mock
    private EvaluacionDocumentoRepository evaluacionDocumentoRepository;
    @Mock
    private DocumentoRepository documentoRepository;
    @Mock
    private DocumentoService documentoService;
    private EvaluacionExpedienteServiceImpl evaluacionExpedienteService;

    @Mock
    private PropuestaDecisionRepository propuestaRepository;
    private PropuestaDecisionServiceImpl propuestaDecisionService;

    @Mock
    private RevisionPropuestaRepository revisionPropuestaRepository;
    private RevisionDecisionServiceImpl revisionDecisionService;

    @Mock
    private CorrelativoDocumentalRepository correlativoRepository;
    @Mock
    private OficinaRepository oficinaRepository;
    private NumeracionDocumentalServiceImpl numeracionDocumentalService;

    @Mock
    private DecisionExpedienteRepository decisionRepository;
    @Mock
    private QrCodeGenerator qrCodeGenerator;
    private DecisionExpedienteServiceImpl decisionExpedienteService;

    @Mock
    private NotificacionAdministrativaRepository notificacionRepository;
    private NotificacionResultadoServiceImpl notificacionResultadoServiceImpl;

    @Mock
    private CierreExpedienteRepository cierreRepository;
    private CierreExpedienteServiceImpl cierreExpedienteService;

    @Mock
    private ArchivoExpedienteRepository archivoRepository;
    @Mock
    private ReaperturaExpedienteRepository reaperturaRepository;
    private ArchivoExpedienteServiceImpl archivoExpedienteService;

    private Usuario trabajador;
    private Usuario jefe;
    private Usuario alcalde;
    private Oficina oficinaDesarrollo;
    private Oficina oficinaOtra;
    private Cargo cargoJefe;
    private Cargo cargoAlcalde;
    private Expediente expediente;

    @BeforeEach
    void setUp() {
        atencionExpedienteService = new AtencionExpedienteServiceImpl(
                expedienteRepository, workflowService, usuarioRepository,
                personaOficinaRepository, notificationService, evaluacionService,
                propuestaService, decisionService, notificacionResultadoService,
                cierreService, archivoService
        );

        evaluacionExpedienteService = new EvaluacionExpedienteServiceImpl(
                evaluacionRepository, evaluacionDocumentoRepository, expedienteRepository,
                documentoRepository, documentoService, usuarioRepository, personaOficinaRepository
        );

        propuestaDecisionService = new PropuestaDecisionServiceImpl(
                propuestaRepository, expedienteRepository, usuarioRepository,
                personaOficinaRepository, notificationService
        );

        revisionDecisionService = new RevisionDecisionServiceImpl(
                propuestaRepository, revisionPropuestaRepository, propuestaDecisionService,
                usuarioRepository, personaOficinaRepository, notificationService
        );

        numeracionDocumentalService = new NumeracionDocumentalServiceImpl(correlativoRepository, oficinaRepository);

        decisionExpedienteService = new DecisionExpedienteServiceImpl(
                decisionRepository, expedienteRepository, propuestaRepository,
                numeracionDocumentalService, workflowService, documentoRepository,
                documentoService, usuarioRepository, personaOficinaRepository,
                notificationService, qrCodeGenerator
        );

        notificacionResultadoServiceImpl = new NotificacionResultadoServiceImpl(
                notificacionRepository, expedienteRepository, decisionRepository,
                workflowService, documentoRepository, documentoService,
                usuarioRepository, notificationService
        );

        cierreExpedienteService = new CierreExpedienteServiceImpl(
                cierreRepository, expedienteRepository, decisionRepository,
                workflowService, usuarioRepository, personaOficinaRepository
        );

        archivoExpedienteService = new ArchivoExpedienteServiceImpl(
                archivoRepository, reaperturaRepository, expedienteRepository,
                workflowService, usuarioRepository, personaOficinaRepository
        );

        // Model setup
        Persona p1 = Persona.builder().id(10L).nombres("Juan").apellidoPaterno("Pérez").build();
        Persona p2 = Persona.builder().id(20L).nombres("Carlos").apellidoPaterno("Gómez").build();
        Persona p3 = Persona.builder().id(30L).nombres("Alcalde").apellidoPaterno("Quinua").build();

        Rol rolTrabajador = Rol.builder().id(1L).codigo("ROLE_MESA_PARTES_OPERADOR").nombre("Operador").build();
        Rol rolJefe = Rol.builder().id(2L).codigo("ROLE_JEFE_OFICINA").nombre("Jefe").build();
        Rol rolFuncionario = Rol.builder().id(3L).codigo("ROLE_FUNCIONARIO").nombre("Funcionario").build();

        trabajador = Usuario.builder().id(100L).username("jperez").persona(p1).roles(Set.of(rolTrabajador)).build();
        jefe = Usuario.builder().id(200L).username("cgomez").persona(p2).roles(Set.of(rolJefe)).build();
        alcalde = Usuario.builder().id(300L).username("alcalde").persona(p3).roles(Set.of(rolFuncionario)).build();

        oficinaDesarrollo = Oficina.builder().id(50L).codigo("SGDU").sigla("SGDU").nombre("Subgerencia de Desarrollo Urbano").build();
        oficinaOtra = Oficina.builder().id(99L).codigo("OTRA").sigla("OTRA").nombre("Otra Oficina").build();

        cargoJefe = Cargo.builder().id(1L).codigo("JEFE_SGDU").nombre("Subgerente de Desarrollo Urbano").nivelJerarquico(2).build();
        cargoAlcalde = Cargo.builder().id(2L).codigo("ALCALDE").nombre("Alcalde Distrital").nivelJerarquico(1).build();

        expediente = Expediente.builder()
                .id(1000L)
                .numeroExpediente("EXP-2026-000100")
                .asunto("Solicitud de Licencia de Funcionamiento")
                .estadoActual(EstadoExpediente.DERIVADO)
                .oficinaResponsable(oficinaDesarrollo)
                .responsableActual(trabajador)
                .solicitante(p1)
                .build();
    }

    // --- 1. BANDEJA Y ATENCIÓN ---
    @Test
    @DisplayName("Debe consultar bandeja de atención correctamente")
    void testConsultarBandejaAtencion() {
        when(usuarioRepository.findByUsername("jperez")).thenReturn(Optional.of(trabajador));
        when(expedienteRepository.buscarAtencionConFiltros(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(expediente)));

        Page<AtencionItemResponse> page = atencionExpedienteService.listarBandejaAtencion(
                null, null, null, null, PageRequest.of(0, 10), "jperez"
        );

        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
        assertEquals("EXP-2026-000100", page.getContent().get(0).getNumeroExpediente());
    }

    @Test
    @DisplayName("Debe iniciar atención de expediente asignado")
    void testIniciarAtencionExito() {
        when(usuarioRepository.findByUsername("jperez")).thenReturn(Optional.of(trabajador));
        when(expedienteRepository.findByIdWithDetalles(1000L)).thenReturn(Optional.of(expediente));
        when(personaOficinaRepository.findByPersonaIdAndEstado(10L, "ACTIVO"))
                .thenReturn(List.of(PersonaOficina.builder().oficina(oficinaDesarrollo).build()));

        Expediente actualizado = Expediente.builder()
                .id(1000L)
                .numeroExpediente("EXP-2026-000100")
                .asunto(expediente.getAsunto())
                .estadoActual(EstadoExpediente.EN_ATENCION)
                .oficinaResponsable(oficinaDesarrollo)
                .responsableActual(trabajador)
                .fechaInicioAtencion(OffsetDateTime.now())
                .build();

        when(workflowService.transicionar(any(), eq(EstadoExpediente.EN_ATENCION), eq("INICIO_ATENCION"), any(), eq(trabajador), eq(oficinaDesarrollo)))
                .thenReturn(actualizado);

        IniciarAtencionRequest req = IniciarAtencionRequest.builder().indicaciones("Revisión iniciada").build();
        AtencionItemResponse res = atencionExpedienteService.iniciarAtencion(1000L, req, "jperez");

        assertNotNull(res);
        assertEquals(EstadoExpediente.EN_ATENCION, res.getEstado());
        verify(workflowService, times(1)).transicionar(any(), eq(EstadoExpediente.EN_ATENCION), eq("INICIO_ATENCION"), any(), eq(trabajador), eq(oficinaDesarrollo));
    }

    @Test
    @DisplayName("Debe rechazar inicio de atención para expediente ajeno a la oficina")
    void testIniciarAtencionRechazarExpedienteAjeno() {
        when(usuarioRepository.findByUsername("jperez")).thenReturn(Optional.of(trabajador));
        when(expedienteRepository.findByIdWithDetalles(1000L)).thenReturn(Optional.of(expediente));
        // Trabajador solo trabaja en oficinaOtra, pero el expediente es de oficinaDesarrollo
        when(personaOficinaRepository.findByPersonaIdAndEstado(10L, "ACTIVO"))
                .thenReturn(List.of(PersonaOficina.builder().oficina(oficinaOtra).build()));
        expediente.setResponsableActual(null); // No asignado a él

        IniciarAtencionRequest req = IniciarAtencionRequest.builder().build();

        assertThrows(AccessDeniedException.class, () ->
                atencionExpedienteService.iniciarAtencion(1000L, req, "jperez")
        );
    }

    // --- 2. EVALUACIONES ---
    @Test
    @DisplayName("Debe registrar y finalizar evaluación administrativa")
    void testRegistrarYFinalizarEvaluacion() {
        when(usuarioRepository.findByUsername("jperez")).thenReturn(Optional.of(trabajador));
        when(expedienteRepository.findByIdWithDetalles(1000L)).thenReturn(Optional.of(expediente));
        when(personaOficinaRepository.findByPersonaIdAndEstado(10L, "ACTIVO"))
                .thenReturn(List.of(PersonaOficina.builder().oficina(oficinaDesarrollo).build()));

        EvaluacionExpediente eval = EvaluacionExpediente.builder()
                .id(500L)
                .expediente(expediente)
                .usuarioEvaluador(trabajador)
                .oficina(oficinaDesarrollo)
                .tipoEvaluacion(TipoEvaluacionExpediente.TECNICA)
                .descripcion("Verificación en campo")
                .conclusiones("Cumple zonificación")
                .estado(EstadoEvaluacion.BORRADOR)
                .build();

        when(evaluacionRepository.save(any(EvaluacionExpediente.class))).thenReturn(eval);
        when(evaluacionRepository.findById(500L)).thenReturn(Optional.of(eval));

        CrearEvaluacionRequest req = CrearEvaluacionRequest.builder()
                .tipoEvaluacion(TipoEvaluacionExpediente.TECNICA)
                .descripcion("Verificación en campo")
                .conclusiones("Cumple zonificación")
                .build();

        EvaluacionResponse res = evaluacionExpedienteService.crearEvaluacion(1000L, req, "jperez");
        assertNotNull(res);
        assertEquals(EstadoEvaluacion.BORRADOR, res.getEstado());

        // Finalizar evaluación
        EvaluacionResponse resFinalizada = evaluacionExpedienteService.finalizarEvaluacion(500L, "jperez");
        assertNotNull(resFinalizada);
        assertEquals(EstadoEvaluacion.FINALIZADA, resFinalizada.getEstado());
    }

    // --- 3. PROPUESTAS Y REVISIÓN CON SEPARACIÓN DE FUNCIONES ---
    @Test
    @DisplayName("Debe crear propuesta de decisión y elevarla a revisión")
    void testCrearYEvaluarPropuesta() {
        when(usuarioRepository.findByUsername("jperez")).thenReturn(Optional.of(trabajador));
        when(expedienteRepository.findByIdWithDetalles(1000L)).thenReturn(Optional.of(expediente));
        when(personaOficinaRepository.findByPersonaIdAndEstado(10L, "ACTIVO"))
                .thenReturn(List.of(PersonaOficina.builder().oficina(oficinaDesarrollo).build()));

        PropuestaDecision propuesta = PropuestaDecision.builder()
                .id(700L)
                .expediente(expediente)
                .tipoDecision(TipoDecisionExpediente.OFICIO_RESPUESTA)
                .resultadoPropuesto(ResultadoDecision.FAVORABLE)
                .fundamento("Cumple con TUPA")
                .descripcion("Propuesta de emisión favorable")
                .usuarioElaborador(trabajador)
                .oficina(oficinaDesarrollo)
                .estado(EstadoPropuesta.BORRADOR)
                .build();

        when(propuestaRepository.save(any(PropuestaDecision.class))).thenReturn(propuesta);
        when(propuestaRepository.findById(700L)).thenReturn(Optional.of(propuesta));

        CrearPropuestaRequest req = CrearPropuestaRequest.builder()
                .tipoDecision(TipoDecisionExpediente.OFICIO_RESPUESTA)
                .resultadoPropuesto(ResultadoDecision.FAVORABLE)
                .fundamento("Cumple con TUPA")
                .descripcion("Propuesta de emisión favorable")
                .build();

        PropuestaResponse res = propuestaDecisionService.crearPropuesta(1000L, req, "jperez");
        assertNotNull(res);
        assertEquals(EstadoPropuesta.BORRADOR, res.getEstado());

        // Enviar a revisión
        PropuestaResponse resEnRevision = propuestaDecisionService.enviarARevision(700L, "jperez");
        assertNotNull(resEnRevision);
        assertEquals(EstadoPropuesta.EN_REVISION, resEnRevision.getEstado());
    }

    @Test
    @DisplayName("Debe impedir que el mismo elaborador apruebe internamente su propuesta (Separación de Funciones)")
    void testSeparacionFuncionesEnRevisionPropuesta() {
        when(usuarioRepository.findByUsername("jperez")).thenReturn(Optional.of(trabajador));

        PropuestaDecision propuesta = PropuestaDecision.builder()
                .id(700L)
                .expediente(expediente)
                .usuarioElaborador(trabajador) // Elaborador = jperez
                .oficina(oficinaDesarrollo)
                .estado(EstadoPropuesta.EN_REVISION)
                .build();

        when(propuestaRepository.findById(700L)).thenReturn(Optional.of(propuesta));

        RevisarPropuestaRequest req = RevisarPropuestaRequest.builder()
                .accion(AccionRevisionPropuesta.APROBADA_INTERNAMENTE)
                .observaciones("Me la autoapruebo indebidamente")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                revisionDecisionService.revisarPropuesta(700L, req, "jperez")
        );
        assertTrue(ex.getMessage().contains("segregación de funciones"));
    }

    @Test
    @DisplayName("Jefe de oficina debe poder aprobar internamente la propuesta")
    void testAprobarPropuestaPorJefe() {
        when(usuarioRepository.findByUsername("cgomez")).thenReturn(Optional.of(jefe));

        PropuestaDecision propuesta = PropuestaDecision.builder()
                .id(700L)
                .expediente(expediente)
                .usuarioElaborador(trabajador)
                .oficina(oficinaDesarrollo)
                .estado(EstadoPropuesta.EN_REVISION)
                .build();

        when(propuestaRepository.findById(700L)).thenReturn(Optional.of(propuesta));
        when(personaOficinaRepository.findByPersonaIdAndEstado(20L, "ACTIVO"))
                .thenReturn(List.of(PersonaOficina.builder().oficina(oficinaDesarrollo).cargo(cargoJefe).build()));
        when(revisionPropuestaRepository.save(any(RevisionPropuesta.class)))
                .thenReturn(RevisionPropuesta.builder().id(1L).build());
        when(propuestaRepository.save(any(PropuestaDecision.class))).thenReturn(propuesta);

        RevisarPropuestaRequest req = RevisarPropuestaRequest.builder()
                .accion(AccionRevisionPropuesta.APROBADA_INTERNAMENTE)
                .observaciones("Revisado conforme a norma técnica municipal")
                .build();

        PropuestaResponse res = revisionDecisionService.revisarPropuesta(700L, req, "cgomez");
        assertNotNull(res);
        assertEquals(EstadoPropuesta.APROBADA_INTERNAMENTE, propuesta.getEstado());
    }

    // --- 4. NUMERACIÓN DOCUMENTAL ---
    @Test
    @DisplayName("Debe generar número correlativo documental sin colisiones")
    void testGenerarNumeroDocumental() {
        int anio = LocalDate.now().getYear();
        CorrelativoDocumental corr = CorrelativoDocumental.builder()
                .id(1L)
                .tipoDocumento("RESOLUCION")
                .oficina(oficinaDesarrollo)
                .anio(anio)
                .ultimoNumero(5L)
                .formato("{TIPO}-{NUM}-{ANIO}-MDQ/{OFICINA}")
                .build();

        when(oficinaRepository.findById(50L)).thenReturn(Optional.of(oficinaDesarrollo));
        when(correlativoRepository.findForUpdate("RESOLUCION", 50L, anio))
                .thenReturn(Optional.of(corr));
        when(correlativoRepository.saveAndFlush(any(CorrelativoDocumental.class))).thenReturn(corr);

        String num = numeracionDocumentalService.generarNumeroDocumento("RESOLUCION", 50L, anio);
        assertEquals("RESOLUCION-0006-" + anio + "-MDQ/SGDU", num);
        assertEquals(6L, corr.getUltimoNumero());
    }

    // --- 5. DECISIÓN Y FORMALIZACIÓN ---
    @Test
    @DisplayName("Debe registrar decisión solo si funcionario tiene competencia institucional")
    void testRegistrarDecisionConCompetencia() {
        when(usuarioRepository.findByUsername("alcalde")).thenReturn(Optional.of(alcalde));
        when(expedienteRepository.findByIdWithDetalles(1000L)).thenReturn(Optional.of(expediente));
        when(personaOficinaRepository.findByPersonaIdAndEstado(30L, "ACTIVO"))
                .thenReturn(List.of(PersonaOficina.builder().oficina(oficinaDesarrollo).cargo(cargoAlcalde).build()));

        DecisionExpediente dec = DecisionExpediente.builder()
                .id(800L)
                .expediente(expediente)
                .tipoDecision(TipoDecisionExpediente.RESOLUCION_SUBGERENCIAL)
                .resultado(ResultadoDecision.FAVORABLE)
                .fundamento("Fundamento legal verificado")
                .numeroDocumento("RES-0001-2026-MDQ/SGDU")
                .autoridadCompetente(alcalde)
                .oficinaEmisora(oficinaDesarrollo)
                .estadoEmision(EstadoEmisionDocumento.PENDIENTE_FIRMA)
                .build();

        when(decisionRepository.save(any(DecisionExpediente.class))).thenReturn(dec);

        RegistrarDecisionRequest req = RegistrarDecisionRequest.builder()
                .tipoDecision(TipoDecisionExpediente.RESOLUCION_SUBGERENCIAL)
                .resultado(ResultadoDecision.FAVORABLE)
                .fundamento("Fundamento legal verificado")
                .numeroDocumento("RES-0001-2026-MDQ/SGDU")
                .build();

        DecisionResponse res = decisionExpedienteService.registrarDecision(1000L, req, "alcalde");
        assertNotNull(res);
        assertEquals(EstadoEmisionDocumento.PENDIENTE_FIRMA, res.getEstadoEmision());
    }

    @Test
    @DisplayName("Debe rechazar emisión formal si no se ha generado previamente el documento PDF")
    void testRechazarFormalizacionSinPdf() {
        when(usuarioRepository.findByUsername("alcalde")).thenReturn(Optional.of(alcalde));
        when(personaOficinaRepository.findByPersonaIdAndEstado(30L, "ACTIVO"))
                .thenReturn(List.of(PersonaOficina.builder().oficina(oficinaDesarrollo).cargo(cargoAlcalde).build()));

        DecisionExpediente dec = DecisionExpediente.builder()
                .id(800L)
                .expediente(expediente)
                .oficinaEmisora(oficinaDesarrollo)
                .documento(null) // SIN DOCUMENTO PDF
                .build();

        when(decisionRepository.findById(800L)).thenReturn(Optional.of(dec));

        FormalizarDecisionRequest req = FormalizarDecisionRequest.builder()
                .numeroDocumento("RES-001-2026")
                .evidenciaFormalizacion("Firmado en físico")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                decisionExpedienteService.registrarFormalizacionEmision(800L, req, "alcalde")
        );
        assertTrue(ex.getMessage().contains("previamente el documento PDF"));
    }

    // --- 6. NOTIFICACIONES ADMINISTRATIVAS ---
    @Test
    @DisplayName("Debe distinguir entre registro de cédula y práctica legal de notificación con acuse")
    void testRegistrarYAcreditarNotificacion() {
        when(usuarioRepository.findByUsername("jperez")).thenReturn(Optional.of(trabajador));
        when(expedienteRepository.findByIdWithDetalles(1000L)).thenReturn(Optional.of(expediente));

        expediente.setEstadoActual(EstadoExpediente.RESUELTO);
        DecisionExpediente decision = DecisionExpediente.builder()
                .id(800L)
                .numeroDocumento("RES-001-2026")
                .build();
        when(decisionRepository.findById(800L)).thenReturn(Optional.of(decision));

        NotificacionAdministrativa notif = NotificacionAdministrativa.builder()
                .id(900L)
                .expediente(expediente)
                .decision(decision)
                .destinatarioNombre("Administrado Ciudadano")
                .destinatarioDireccion("Jr. Sucre 123, Quinua")
                .medioNotificacion(MedioNotificacion.CASILLA_ELECTRONICA)
                .estadoNotificacion(EstadoNotificacion.PENDIENTE)
                .usuarioResponsable(trabajador)
                .build();

        when(notificacionRepository.save(any(NotificacionAdministrativa.class))).thenReturn(notif);
        when(notificacionRepository.findById(900L)).thenReturn(Optional.of(notif));

        RegistrarNotificacionRequest req = RegistrarNotificacionRequest.builder()
                .decisionId(800L)
                .destinatarioNombre("Administrado Ciudadano")
                .destinatarioDireccion("Jr. Sucre 123, Quinua")
                .medioNotificacion(MedioNotificacion.CASILLA_ELECTRONICA)
                .build();

        NotificacionResponse res = notificacionResultadoServiceImpl.registrarNotificacion(1000L, req, "jperez");
        assertNotNull(res);
        assertEquals(EstadoNotificacion.PENDIENTE, res.getEstadoNotificacion());

        // Acreditar legalmente con acuse de recibo y fecha
        AcreditarNotificacionRequest reqAcreditar = AcreditarNotificacionRequest.builder()
                .numeroAcuseRecibo("ACUSE-2026-99")
                .observaciones("Acuse firmado por el administrado con DNI")
                .build();

        NotificacionResponse resAcreditada = notificacionResultadoServiceImpl.acreditarNotificacionValida(900L, reqAcreditar, null, "jperez");
        assertNotNull(resAcreditada);
        assertEquals(EstadoNotificacion.NOTIFICADA_VALIDA, resAcreditada.getEstadoNotificacion());
        verify(workflowService, times(1)).transicionar(eq(expediente), eq(EstadoExpediente.NOTIFICADO), eq("ACREDITACION_NOTIFICACION"), any(), any(), any());
    }

    // --- 7. CIERRE Y ARCHIVO ---
    @Test
    @DisplayName("Debe rechazar cierre si el estado no permite cierre por atención concluida")
    void testRechazarCierreInvalido() {
        when(usuarioRepository.findByUsername("cgomez")).thenReturn(Optional.of(jefe));
        expediente.setEstadoActual(EstadoExpediente.DERIVADO); // Estado no resuelto
        when(expedienteRepository.findByIdWithDetalles(1000L)).thenReturn(Optional.of(expediente));
        when(personaOficinaRepository.findByPersonaIdAndEstado(20L, "ACTIVO"))
                .thenReturn(List.of(PersonaOficina.builder().oficina(oficinaDesarrollo).cargo(cargoJefe).build()));

        CerrarExpedienteRequest req = CerrarExpedienteRequest.builder()
                .tipoCierre(TipoCierreExpediente.ATENCION_CONCLUIDA)
                .motivoCierre("Trámite finalizado pero aún en derivado")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                cierreExpedienteService.cerrarExpediente(1000L, req, "cgomez")
        );
        assertTrue(ex.getMessage().contains("RESUELTO o NOTIFICADO"));
    }

    @Test
    @DisplayName("Debe cerrar expediente válidamente cuando cumple requisitos")
    void testCerrarExpedienteValido() {
        when(usuarioRepository.findByUsername("cgomez")).thenReturn(Optional.of(jefe));
        expediente.setEstadoActual(EstadoExpediente.NOTIFICADO);
        when(expedienteRepository.findByIdWithDetalles(1000L)).thenReturn(Optional.of(expediente));
        when(personaOficinaRepository.findByPersonaIdAndEstado(20L, "ACTIVO"))
                .thenReturn(List.of(PersonaOficina.builder().oficina(oficinaDesarrollo).cargo(cargoJefe).build()));

        CierreExpediente cierre = CierreExpediente.builder()
                .id(1L)
                .expediente(expediente)
                .tipoCierre(TipoCierreExpediente.ATENCION_CONCLUIDA)
                .motivoCierre("Trámite regular culminado")
                .usuarioCierre(jefe)
                .oficinaCierre(oficinaDesarrollo)
                .build();
        when(cierreRepository.save(any(CierreExpediente.class))).thenReturn(cierre);

        CerrarExpedienteRequest req = CerrarExpedienteRequest.builder()
                .tipoCierre(TipoCierreExpediente.ATENCION_CONCLUIDA)
                .motivoCierre("Trámite regular culminado")
                .build();

        CierreResponse res = cierreExpedienteService.cerrarExpediente(1000L, req, "cgomez");
        assertNotNull(res);
        assertNotNull(expediente.getFechaCierre());
        verify(expedienteRepository, times(1)).save(expediente);
    }

    @Test
    @DisplayName("Debe archivar expediente con referencia de ubicación")
    void testArchivarExpedienteFinalizado() {
        when(usuarioRepository.findByUsername("cgomez")).thenReturn(Optional.of(jefe));
        expediente.setEstadoActual(EstadoExpediente.RESUELTO);
        when(expedienteRepository.findByIdWithDetalles(1000L)).thenReturn(Optional.of(expediente));

        ArchivoExpediente arch = ArchivoExpediente.builder()
                .id(1L)
                .expediente(expediente)
                .ubicacionFisica("Estante 3, Balda 2, Caja 14")
                .motivoArchivo("Archivado conforme a ley")
                .usuarioArchivo(jefe)
                .oficinaArchivo(oficinaDesarrollo)
                .build();
        when(archivoRepository.save(any(ArchivoExpediente.class))).thenReturn(arch);

        ArchivarExpedienteRequest req = ArchivarExpedienteRequest.builder()
                .ubicacionFisica("Estante 3, Balda 2, Caja 14")
                .motivoArchivo("Archivado conforme a ley")
                .build();

        ArchivoResponse res = archivoExpedienteService.archivarExpediente(1000L, req, "cgomez");
        assertNotNull(res);
        assertEquals("Estante 3, Balda 2, Caja 14", res.getUbicacionFisica());
        verify(workflowService, times(1)).transicionar(eq(expediente), eq(EstadoExpediente.ARCHIVADO), eq("ENVIO_ARCHIVO_ADMINISTRATIVO"), any(), any(), any());
    }

    @Test
    @DisplayName("Debe permitir reapertura justificada legalmente con control y auditoría")
    void testReaperturaControlada() {
        when(usuarioRepository.findByUsername("cgomez")).thenReturn(Optional.of(jefe));
        expediente.setEstadoActual(EstadoExpediente.ARCHIVADO);
        when(expedienteRepository.findByIdWithDetalles(1000L)).thenReturn(Optional.of(expediente));

        ReaperturaExpediente reap = ReaperturaExpediente.builder()
                .id(1L)
                .expediente(expediente)
                .motivoLegal("Mandato judicial de reposición de procedimiento")
                .usuarioReapertura(jefe)
                .build();
        when(reaperturaRepository.save(any(ReaperturaExpediente.class))).thenReturn(reap);

        ReabrirExpedienteRequest req = ReabrirExpedienteRequest.builder()
                .motivoLegal("Mandato judicial de reposición de procedimiento")
                .build();

        ReaperturaResponse res = archivoExpedienteService.reabrirExpediente(1000L, req, "cgomez");
        assertNotNull(res);
        assertTrue(expediente.getEsReabierto());
        verify(workflowService, times(1)).transicionar(eq(expediente), eq(EstadoExpediente.EN_ATENCION), eq("REAPERTURA_ADMINISTRATIVA"), any(), any(), any());
    }
}
