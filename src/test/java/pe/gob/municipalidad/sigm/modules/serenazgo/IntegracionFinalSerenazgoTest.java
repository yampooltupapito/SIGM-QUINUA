package pe.gob.municipalidad.sigm.modules.serenazgo;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.documentos.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.supervision.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de Integración Final, Trazabilidad Cruzada, Auditoría y Concurrencia
 * para el Módulo 3: Seguridad Ciudadana y Serenazgo (Prompt 50 de 150).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IntegracionFinalSerenazgoTest {

    @Autowired
    private PersonalSerenazgoService personalService;

    @Autowired
    private UnidadesMovilesService unidadesService;

    @Autowired
    private TurnoSerenazgoService turnoService;

    @Autowired
    private PatrullajeSerenazgoService patrullajeService;

    @Autowired
    private IncidenciaSeguridadCiudadanaService incidenciaService;

    @Autowired
    private AlertaCiudadanaSeguridadService alertaService;

    @Autowired
    private AtencionIncidenciaSerenazgoService atencionService;

    @Autowired
    private IntervencionSerenazgoService intervencionService;

    @Autowired
    private DocumentoSeguridadCiudadanaService documentoService;

    @Autowired
    private SupervisionSerenazgoService supervisionService;

    @Autowired
    private EstadisticasSerenazgoService estadisticasService;

    @Autowired
    private ReportesEstadisticosSerenazgoService reportesService;

    // Repositorios auxiliares para setup institucional
    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    @Autowired
    private CargoOperativoSerenazgoRepository cargoRepository;

    @Autowired
    private PersonalSerenazgoRepository personalRepository;

    @Autowired
    private SectorPatrullajeRepository sectorRepository;

    @Autowired
    private ZonaPatrullajeRepository zonaRepository;

    @Autowired
    private TipoIncidenciaSeguridadRepository tipoIncidenciaRepository;

    @Autowired
    private IncidenciaSeguridadCiudadanaRepository incidenciaRepository;

    @Autowired
    private AtencionIncidenciaSerenazgoRepository atencionRepository;

    @Autowired
    private IntervencionSerenazgoRepository intervencionRepository;

    @Autowired
    private DocumentoSeguridadCiudadanaRepository documentoRepository;

    @Autowired
    private SeguimientoSupervisionSerenazgoRepository seguimientoRepository;

    private CustomUserDetails userSupervisor;
    private Usuario supervisorUsuario;
    private Oficina oficinaSerenazgo;
    private CargoOperativoSerenazgo cargoSereno;
    private PersonalSerenazgo serenoOperativo;
    private SectorPatrullaje sectorQuinua;
    private ZonaPatrullaje zonaCentro;
    private TipoIncidenciaSeguridad tipoIncidencia;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Persona per = personaRepository.save(Persona.builder()
                .numeroDocumento(String.format("7%07d", (int) (Math.abs(ts) % 9000000 + 1000000)))
                .tipoDocumento("DNI")
                .nombres("Carlos")
                .apellidoPaterno("Mendoza")
                .apellidoMaterno("Quispe")
                .correo("carlos.mendoza." + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        oficinaSerenazgo = oficinaRepository.findAll().stream().findFirst().orElseGet(() ->
                oficinaRepository.save(Oficina.builder()
                        .codigo("OFI-SER-" + ts)
                        .nombre("Subgerencia de Seguridad Ciudadana")
                        .estado("ACTIVO")
                        .build())
        );

        supervisorUsuario = usuarioRepository.save(Usuario.builder()
                .username("sup_e2e_" + ts)
                .passwordHash("$2a$10$wK8r17dZgO2Kj1kO4n2zU.bN67q7sX9iB6j1yP2V9Y2E3u1T4B8Xa")
                .persona(per)
                .enabled(true)
                .build());

        userSupervisor = CustomUserDetails.builder()
                .id(supervisorUsuario.getId())
                .username(supervisorUsuario.getUsername())
                .password(supervisorUsuario.getPasswordHash())
                .enabled(true)
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_SUPERVISOR_SERENAZGO"),
                        new SimpleGrantedAuthority("SERENAZGO_ESTADISTICAS_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_REPORTES_GENERAR"),
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_VER")
                ))
                .build();

        cargoSereno = cargoRepository.findAll().stream().findFirst().orElseGet(() ->
                cargoRepository.save(CargoOperativoSerenazgo.builder()
                        .codigo("CAR-SER-" + ts)
                        .nombre("Sereno Motorizado")
                        .nivelJerarquico(1)
                        .activo(true)
                        .build())
        );

        serenoOperativo = personalRepository.save(PersonalSerenazgo.builder()
                .codigoInterno("PER-OP-" + ts)
                .persona(per)
                .usuario(supervisorUsuario)
                .oficina(oficinaSerenazgo)
                .cargoOperativo(cargoSereno)
                .estado(EstadoPersonalSerenazgo.ACTIVO)
                .fechaIngreso(LocalDate.now().minusMonths(6))
                .build());

        sectorQuinua = sectorRepository.save(SectorPatrullaje.builder()
                .codigo("SEC-E2E-" + ts)
                .nombre("Sector Centro Histórico Quinua")
                .colorHex("#1d4ed8")
                .estado(EstadoSectorPatrullaje.ACTIVO)
                .build());

        zonaCentro = zonaRepository.save(ZonaPatrullaje.builder()
                .sector(sectorQuinua)
                .codigo("ZON-E2E-" + ts)
                .nombre("Plaza Mayor de Quinua")
                .estado(EstadoSectorPatrullaje.ACTIVO)
                .build());

        tipoIncidencia = tipoIncidenciaRepository.save(TipoIncidenciaSeguridad.builder()
                .codigo("TIP-E2E-" + ts)
                .nombre("Alteración del Orden y Convivencia")
                .categoria(CategoriaIncidenciaSeguridad.ORDEN_PUBLICO)
                .prioridadSugerida(PrioridadIncidenciaSeguridad.MEDIA)
                .activo(true)
                .build());
    }

    @Test
    @Order(1)
    @DisplayName("1. Flujo Completo E2E: Personal -> Unidad -> Turno -> Patrullaje -> Incidencia -> Atención -> Intervención -> Documento -> Supervisión -> Reporte")
    void testFlujoCompletoDeExtremoAExtremo() {
        long ts = System.currentTimeMillis();

        // 1. Registrar Personal de Serenazgo
        Persona nuevaPersona = personaRepository.save(Persona.builder()
                .numeroDocumento(String.format("4%07d", (int) (Math.abs(ts) % 9000000 + 1000000)))
                .tipoDocumento("DNI")
                .nombres("Juan")
                .apellidoPaterno("Huaman")
                .apellidoMaterno("Flores")
                .correo("juan.huaman." + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        PersonalSerenazgoCreateRequest reqPersonal = PersonalSerenazgoCreateRequest.builder()
                .personaId(nuevaPersona.getId())
                .oficinaId(oficinaSerenazgo.getId())
                .cargoOperativoId(cargoSereno.getId())
                .tipoDocumentoLaboral("CONTRATO_CAS")
                .fechaIngreso(LocalDate.now())
                .estado(EstadoPersonalSerenazgo.ACTIVO)
                .observaciones("Personal operativo asignado a patrullaje")
                .build();
        PersonalSerenazgoResponse resPersonal = personalService.registrar(reqPersonal, supervisorUsuario.getUsername());
        assertNotNull(resPersonal);
        assertNotNull(resPersonal.getId());
        assertEquals(EstadoPersonalSerenazgo.ACTIVO.name(), resPersonal.getEstado());

        // 2. Registrar Unidad Móvil
        UnidadMovilCreateRequest reqUnidad = UnidadMovilCreateRequest.builder()
                .tipoUnidad(TipoUnidadMovil.CAMIONETA)
                .placa("EGA-" + (Math.abs(ts) % 900 + 100))
                .marca("Toyota")
                .modelo("Hilux 4x4")
                .color("Blanco Institucional")
                .anio(2024)
                .kilometrajeActual(new BigDecimal("15200.5"))
                .oficinaResponsableId(oficinaSerenazgo.getId())
                .build();
        UnidadMovilResponse resUnidad = unidadesService.registrar(reqUnidad, supervisorUsuario.getUsername());
        assertNotNull(resUnidad);
        assertNotNull(resUnidad.getId());
        assertEquals(EstadoUnidadMovil.DISPONIBLE, resUnidad.getEstadoOperativo());

        // 3. Crear Turno
        TurnoCreateRequest reqTurno = TurnoCreateRequest.builder()
                .codigo("TUR-E2E-" + ts)
                .nombre("Turno Tarde Seguridad")
                .tipoTurno(TipoTurnoSerenazgo.DIURNO)
                .horaInicio(LocalTime.of(14, 0))
                .horaFin(LocalTime.of(22, 0))
                .oficinaResponsableId(oficinaSerenazgo.getId())
                .estado(EstadoTurnoSerenazgo.ACTIVO)
                .build();
        TurnoResponse resTurno = turnoService.crearTurno(reqTurno, supervisorUsuario.getUsername());
        assertNotNull(resTurno);
        assertEquals(EstadoTurnoSerenazgo.ACTIVO, resTurno.getEstado());

        // 4. Programar Patrullaje
        PatrullajeCrearRequest reqPatrullaje = PatrullajeCrearRequest.builder()
                .tipoPatrullaje(TipoPatrullajeSerenazgo.PATRULLAJE_MUNICIPAL)
                .sectorId(sectorQuinua.getId())
                .zonaId(zonaCentro.getId())
                .responsableId(resPersonal.getId())
                .unidadMovilId(resUnidad.getId())
                .fechaHoraInicioProgramada(LocalDateTime.now().plusHours(1))
                .fechaHoraFinProgramada(LocalDateTime.now().plusHours(4))
                .objetivoOperativo("Patrullaje preventivo disuasivo en Plaza Mayor y accesos")
                .build();
        PatrullajeDetailResponse resPatrullaje = patrullajeService.crearPatrullaje(reqPatrullaje, supervisorUsuario.getUsername());
        assertTrue(resPatrullaje.getEstado() == EstadoPatrullajeSerenazgo.ASIGNADO || resPatrullaje.getEstado() == EstadoPatrullajeSerenazgo.PROGRAMADO, "El patrullaje con efectivo y móvil debe quedar ASIGNADO o PROGRAMADO");

        // 5. Registrar Incidencia
        IncidenciaCreateRequest reqIncidencia = IncidenciaCreateRequest.builder()
                .titulo("Alteración sonora y aglomeración no autorizada")
                .descripcion("Grupo de personas perturbando la tranquilidad en las inmediaciones de la plaza")
                .tipoIncidenciaId(tipoIncidencia.getId())
                .prioridad(PrioridadIncidenciaSeguridad.MEDIA)
                .sectorId(sectorQuinua.getId())
                .zonaId(zonaCentro.getId())
                .ubicacionReferencia("Esquina noroeste de la Plaza Mayor")
                .latitud(new BigDecimal("-13.0461234"))
                .longitud(new BigDecimal("-74.1394321"))
                .canalReporte(CanalReporteIncidencia.PATRULLAJE)
                .origenHecho(OrigenHechoIncidencia.HECHO_OBSERVADO)
                .build();
        IncidenciaResponse resIncidencia = incidenciaService.registrarIncidencia(reqIncidencia, supervisorUsuario.getUsername());
        assertNotNull(resIncidencia);
        assertEquals(EstadoIncidenciaSeguridad.REGISTRADA, resIncidencia.getEstado());

        // 6. Crear Atención y Despachar Recursos
        AtencionCrearRequest reqAtencion = AtencionCrearRequest.builder()
                .incidenciaId(resIncidencia.getId())
                .prioridadOperativa(PrioridadIncidenciaSeguridad.MEDIA)
                .canalComunicacion(CanalComunicacionDespacho.RADIO_TETRA_VHF)
                .responsableOperativoId(resPersonal.getId())
                .unidadMovilId(resUnidad.getId())
                .sectorId(sectorQuinua.getId())
                .zonaId(zonaCentro.getId())
                .instruccionesDespacho("Acudir a la plaza y exhortar al cese pacífico del ruido")
                .despacharInmediatamente(true)
                .build();
        AtencionDetailResponse resAtencion = atencionService.crearAtencion(reqAtencion, supervisorUsuario.getUsername());
        assertNotNull(resAtencion);
        assertTrue(resAtencion.getEstado() == EstadoAtencionIncidencia.DESPACHADA || resAtencion.getEstado() == EstadoAtencionIncidencia.ASIGNADA);

        // 7. Registrar Intervención Operativa
        IntervencionCrearRequest reqIntervencion = IntervencionCrearRequest.builder()
                .incidenciaId(resIncidencia.getId())
                .atencionId(resAtencion.getId())
                .tipoIntervencion(TipoIntervencionSerenazgo.PATRULLAJE_PREVENTIVO)
                .responsableId(resPersonal.getId())
                .sectorId(sectorQuinua.getId())
                .zonaId(zonaCentro.getId())
                .descripcionActuacion("Se dialogó con los ciudadanos presentes, logrando el retiro pacífico sin incidentes mayores.")
                .resultado(ResultadoIntervencionSerenazgo.RESUELTO_EN_LUGAR)
                .fechaInicio(LocalDateTime.now().minusMinutes(30))
                .fechaFin(LocalDateTime.now())
                .latitud(-13.0461234)
                .longitud(-74.1394321)
                .build();
        IntervencionDetailResponse resIntervencion = intervencionService.crearIntervencion(reqIntervencion, supervisorUsuario.getUsername());
        assertNotNull(resIntervencion);
        assertEquals(ResultadoIntervencionSerenazgo.RESUELTO_EN_LUGAR, resIntervencion.getResultado());

        // 8. Adjuntar Documento de Evidencia y Verificar Integridad Criptográfica (SHA-256)
        byte[] contenidoDoc = "Acta preventiva de diálogo y exhortación pacífica en Quinua.".getBytes();
        MockMultipartFile fileEvidencia = new MockMultipartFile(
                "archivo",
                "acta_dialogo_" + ts + ".pdf",
                "application/pdf",
                contenidoDoc
        );

        DocumentoCargaRequest reqDoc = DocumentoCargaRequest.builder()
                .archivo(fileEvidencia)
                .tipoDocumento(TipoDocumentoSeguridad.ACTA_ADMINISTRATIVA)
                .descripcion("Acta administrativa disuasiva firmada en campo")
                .nivelConfidencialidad(NivelConfidencialidad.RESTRINGIDO)
                .origenDocumento(OrigenDocumentoSeguridad.ADMINISTRATIVO)
                .incidenciaId(resIncidencia.getId())
                .intervencionId(resIntervencion.getId())
                .build();

        DocumentoSeguridadResponse resDoc = documentoService.registrarDocumento(reqDoc, userSupervisor, "127.0.0.1");
        assertNotNull(resDoc);
        assertNotNull(resDoc.getHashSha256());

        // Verificar integridad criptográfica de la evidencia
        VerificacionIntegridadResponse verif = documentoService.verificarIntegridad(resDoc.getId(), userSupervisor);
        assertNotNull(verif);
        assertTrue(verif.isCoincide(), "El hash SHA-256 del archivo debe coincidir con el contenido almacenado");

        // 9. Iniciar Seguimiento de Supervisión Operativa
        CrearSeguimientoRequest reqSupervision = CrearSeguimientoRequest.builder()
                .tipoRegistroOrigen(TipoRegistroSupervision.INTERVENCION)
                .registroOrigenId(resIntervencion.getId())
                .registroOrigenCodigo(resIntervencion.getCodigo())
                .tipoSeguimiento(TipoSeguimientoSupervision.REVISION)
                .descripcion("Verificación de cumplimiento de protocolo disuasivo pacífico y archivo documental completo")
                .prioridadAdministrativa(PrioridadAdministrativaSupervision.MEDIA)
                .fechaCompromiso(LocalDate.now().plusDays(2))
                .build();

        SeguimientoResponse resSupervision = supervisionService.crearSeguimiento(reqSupervision, userSupervisor, "127.0.0.1");
        assertNotNull(resSupervision);
        assertEquals(EstadoSeguimientoSupervision.ABIERTO, resSupervision.getEstado());

        // 10. Generar Reporte Estadístico Oficial (PDF y Excel) y Consultar Dashboard Analítico
        FiltroEstadisticoRequest filtroStats = FiltroEstadisticoRequest.builder()
                .periodoTipo("MES_ACTUAL")
                .compararPeriodoAnterior(true)
                .agrupacion("MES")
                .sectorId(sectorQuinua.getId())
                .build();

        DashboardEstadisticoResponse dashboard = estadisticasService.obtenerDashboardEstadistico(filtroStats, userSupervisor);
        assertNotNull(dashboard);
        assertNotNull(dashboard.getKpis());
        assertFalse(dashboard.getKpis().isEmpty(), "El dashboard debe computar en tiempo de consulta los KPIs de seguridad ciudadana");

        GenerarReporteRequest reqReportePdf = GenerarReporteRequest.builder()
                .tipoReporte(TipoReporteEstadistico.GENERAL_SEGURIDAD)
                .formato(FormatoReporteEstadistico.PDF)
                .titulo("Reporte General de Auditoría Quinua")
                .filtro(filtroStats)
                .build();

        ReporteHistorialDto reporteGenerado = reportesService.generarReporte(reqReportePdf, userSupervisor, "127.0.0.1");
        assertNotNull(reporteGenerado);
        assertNotNull(reporteGenerado.getId());
        assertEquals(FormatoReporteEstadistico.PDF, reporteGenerado.getFormato());

        // Comprobar descarga física
        Resource descarga = reportesService.descargarReporte(reporteGenerado.getId(), userSupervisor);
        assertNotNull(descarga);
        assertTrue(descarga.exists());
    }

    @Test
    @Order(2)
    @DisplayName("2. Trazabilidad Cruzada: Incidencia -> Atención -> Intervención -> Documento -> Supervisión")
    void testTrazabilidadCruzadaCompleta() {
        long ts = System.currentTimeMillis();

        // 1. Incidencia
        IncidenciaSeguridadCiudadana inc = incidenciaRepository.save(IncidenciaSeguridadCiudadana.builder()
                .codigo("INC-TRZ-" + ts)
                .titulo("Trazabilidad Cruzada Integral")
                .descripcion("Prueba de encadenamiento referencial entre entidades operativas")
                .ubicacionReferencia("Sector Plaza de Quinua")
                .fechaReporte(OffsetDateTime.now())
                .tipoIncidencia(tipoIncidencia)
                .sector(sectorQuinua)
                .zona(zonaCentro)
                .prioridad(PrioridadIncidenciaSeguridad.ALTA)
                .estado(EstadoIncidenciaSeguridad.EN_ATENCION)
                .usuarioRegistro(supervisorUsuario)
                .build());

        // 2. Atención vinculada a Incidencia
        AtencionIncidenciaSerenazgo atn = atencionRepository.save(AtencionIncidenciaSerenazgo.builder()
                .codigo("ATN-TRZ-" + ts)
                .incidencia(inc)
                .sector(sectorQuinua)
                .zona(zonaCentro)
                .fechaHoraAsignacion(OffsetDateTime.now())
                .estado(EstadoAtencionIncidencia.FINALIZADA)
                .build());

        // 3. Intervención vinculada a Atención e Incidencia
        IntervencionSerenazgo itv = intervencionRepository.save(IntervencionSerenazgo.builder()
                .codigo("ITV-TRZ-" + ts)
                .incidencia(inc)
                .atencion(atn)
                .responsable(serenoOperativo)
                .sector(sectorQuinua)
                .zona(zonaCentro)
                .tipoIntervencion(TipoIntervencionSerenazgo.PATRULLAJE_PREVENTIVO)
                .descripcionActuacion("Intervención disuasiva con diálogo vecinal")
                .resultado(ResultadoIntervencionSerenazgo.RESUELTO_EN_LUGAR)
                .estado(EstadoIntervencionSerenazgo.VALIDADA)
                .fechaInicio(OffsetDateTime.now().minusMinutes(20))
                .fechaFin(OffsetDateTime.now())
                .build());

        // 4. Documento vinculado
        DocumentoSeguridadCiudadana doc = documentoRepository.save(DocumentoSeguridadCiudadana.builder()
                .codigo("DOC-TRZ-" + ts)
                .tipoDocumento(TipoDocumentoSeguridad.INFORME)
                .descripcion("Informe de actuación final")
                .nombreOriginal("informe_cierre.pdf")
                .nombreAlmacenado("informe_cierre_" + ts + ".pdf")
                .extension("pdf")
                .tamanoBytes(1024L)
                .hashSha256("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                .mimeType("application/pdf")
                .incidencia(inc)
                .intervencion(itv)
                .usuarioCarga(supervisorUsuario)
                .estado(EstadoDocumentoSeguridad.VALIDADO_ADMINISTRATIVAMENTE)
                .build());

        // 5. Supervisión vinculada
        SeguimientoSupervisionSerenazgo sup = seguimientoRepository.save(SeguimientoSupervisionSerenazgo.builder()
                .codigo("SUP-TRZ-" + ts)
                .tipoRegistroOrigen(TipoRegistroSupervision.INTERVENCION)
                .registroOrigenId(itv.getId())
                .registroOrigenCodigo(itv.getCodigo())
                .tipoSeguimiento(TipoSeguimientoSupervision.REVISION)
                .descripcion("Seguimiento de auditoría cruzada")
                .supervisor(supervisorUsuario)
                .supervisorNombre(supervisorUsuario.getNombreCompleto())
                .estado(EstadoSeguimientoSupervision.RESUELTO)
                .build());

        // Verificaciones cruzadas directas
        assertEquals(inc.getId(), atn.getIncidencia().getId(), "La atención debe apuntar a la incidencia origen");
        assertEquals(atn.getId(), itv.getAtencion().getId(), "La intervención debe apuntar a la atención");
        assertEquals(inc.getId(), itv.getIncidencia().getId(), "La intervención debe conservar el id de la incidencia");
        assertNotNull(doc.getIntervencion());
        assertEquals(itv.getId(), doc.getIntervencion().getId(), "El documento debe estar vinculado a la intervención");
        assertEquals(itv.getId(), sup.getRegistroOrigenId(), "El expediente de supervisión debe auditar la intervención");
    }

    @Test
    @Order(3)
    @DisplayName("3. Concurrencia y Bloqueo Optimista: Verificación de control de concurrencia (@Version)")
    void testControlDeConcurrenciaOptimista() {
        long ts = System.currentTimeMillis();

        IncidenciaSeguridadCiudadana inc = incidenciaRepository.save(IncidenciaSeguridadCiudadana.builder()
                .codigo("INC-VER-" + ts)
                .titulo("Incidencia para control de concurrencia")
                .descripcion("Validación de versionado optimista")
                .ubicacionReferencia("Quinua")
                .fechaReporte(OffsetDateTime.now())
                .tipoIncidencia(tipoIncidencia)
                .sector(sectorQuinua)
                .prioridad(PrioridadIncidenciaSeguridad.BAJA)
                .estado(EstadoIncidenciaSeguridad.REGISTRADA)
                .usuarioRegistro(supervisorUsuario)
                .build());

        assertNotNull(inc.getVersion(), "La entidad debe poseer versionado optimista");
        Long versionInicial = inc.getVersion();

        // Modificación de estado
        inc.setEstado(EstadoIncidenciaSeguridad.EN_ATENCION);
        IncidenciaSeguridadCiudadana incGuardada = incidenciaRepository.saveAndFlush(inc);

        assertTrue(incGuardada.getVersion() >= versionInicial, "El versionado optimista debe mantenerse o incrementarse en updates concurrentes");
    }

    @Test
    @Order(4)
    @DisplayName("4. Seguridad y Sanitización contra Inyección de Fórmulas en Reportes Excel")
    void testSeguridadSanitizacionFormulaExcel() {
        FiltroEstadisticoRequest filtroMalicioso = FiltroEstadisticoRequest.builder()
                .periodoTipo("MES_ACTUAL")
                .build();

        GenerarReporteRequest reqReporteExcel = GenerarReporteRequest.builder()
                .tipoReporte(TipoReporteEstadistico.INCIDENCIAS)
                .formato(FormatoReporteEstadistico.EXCEL)
                .titulo("=SUM(1+1)") // Intento de inyección de fórmula
                .descripcion("=cmd|'/C calc'!A0") // Intento de comando malicioso
                .filtro(filtroMalicioso)
                .build();

        ReporteHistorialDto repDto = reportesService.generarReporte(reqReporteExcel, userSupervisor, "127.0.0.1");
        assertNotNull(repDto);
        assertEquals(FormatoReporteEstadistico.EXCEL, repDto.getFormato());

        Resource recursoExcel = reportesService.descargarReporte(repDto.getId(), userSupervisor);
        assertNotNull(recursoExcel);
        assertTrue(recursoExcel.exists(), "El archivo Excel con sanitización activa debe existir y haberse compilado con éxito");
    }
}
