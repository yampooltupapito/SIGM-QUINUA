package pe.gob.municipalidad.sigm.modules.serenazgo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.supervision.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.SupervisionSerenazgoService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SupervisionSerenazgoServiceTest {

    @Autowired
    private SupervisionSerenazgoService supervisionService;

    @Autowired
    private SeguimientoSupervisionSerenazgoRepository seguimientoRepository;

    @Autowired
    private IncidenciaSeguridadCiudadanaRepository incidenciaRepository;

    @Autowired
    private TipoIncidenciaSeguridadRepository tipoIncidenciaRepository;

    @Autowired
    private SectorPatrullajeRepository sectorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    private CustomUserDetails userSupervisor;
    private CustomUserDetails userResponsable;
    private Usuario supervisorUsuario;
    private Usuario responsableUsuario;
    private IncidenciaSeguridadCiudadana incidenciaTest;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();
        Persona perSup = personaRepository.save(Persona.builder()
                .numeroDocumento(String.format("4%07d", (int)(Math.abs(ts) % 9000000 + 1000000)))
                .tipoDocumento("DNI")
                .nombres("Carlos")
                .apellidoPaterno("Mendoza")
                .apellidoMaterno("Quispe")
                .estado("ACTIVO")
                .build());

        supervisorUsuario = usuarioRepository.save(Usuario.builder()
                .username("supervisor_quinua_" + ts)
                .passwordHash("$2a$10$abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                .persona(perSup)
                .enabled(true)
                .accountLocked(false)
                .build());

        Persona perResp = personaRepository.save(Persona.builder()
                .numeroDocumento(String.format("5%07d", (int)(Math.abs(ts) % 9000000 + 1000000)))
                .tipoDocumento("DNI")
                .nombres("Raúl")
                .apellidoPaterno("Huamán")
                .apellidoMaterno("Cárdenas")
                .estado("ACTIVO")
                .build());

        responsableUsuario = usuarioRepository.save(Usuario.builder()
                .username("sereno_operativo_" + System.currentTimeMillis())
                .passwordHash("$2a$10$abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                .persona(perResp)
                .enabled(true)
                .accountLocked(false)
                .build());

        userSupervisor = CustomUserDetails.builder()
                .id(supervisorUsuario.getId())
                .username(supervisorUsuario.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .authorities(List.of(
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_VER_INDICADORES"),
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_VER_PENDIENTES"),
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_CREAR_SEGUIMIENTO"),
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_ASIGNAR"),
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_OBSERVAR"),
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_SUBSANAR"),
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_REVISAR"),
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_RESOLVER"),
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_CERRAR"),
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_EXPORTAR"),
                        new SimpleGrantedAuthority("ROLE_SUPERVISOR_SERENAZGO")
                ))
                .build();

        userResponsable = CustomUserDetails.builder()
                .id(responsableUsuario.getId())
                .username(responsableUsuario.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .authorities(List.of(
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_SUBSANAR"),
                        new SimpleGrantedAuthority("ROLE_SERENO")
                ))
                .build();

        TipoIncidenciaSeguridad tipoInc = tipoIncidenciaRepository.save(TipoIncidenciaSeguridad.builder()
                .codigo("TIP-SUP-" + ts)
                .nombre("Alteración del Orden en Plaza")
                .categoria(CategoriaIncidenciaSeguridad.ORDEN_PUBLICO)
                .prioridadSugerida(PrioridadIncidenciaSeguridad.MEDIA)
                .activo(true)
                .build());

        SectorPatrullaje sector = sectorRepository.save(SectorPatrullaje.builder()
                .codigo("SEC-SUP-" + ts)
                .nombre("Sector Centro Quinua")
                .estado(EstadoSectorPatrullaje.ACTIVO)
                .build());

        incidenciaTest = incidenciaRepository.save(IncidenciaSeguridadCiudadana.builder()
                .codigo("INC-SUP-" + ts)
                .titulo("Desorden público cerca al Obelisco")
                .descripcion("Reunión ruidosa no autorizada")
                .ubicacionReferencia("Plaza Principal de Quinua")
                .fechaReporte(OffsetDateTime.now())
                .tipoIncidencia(tipoInc)
                .sector(sector)
                .prioridad(PrioridadIncidenciaSeguridad.MEDIA)
                .estado(EstadoIncidenciaSeguridad.REGISTRADA)
                .canalReporte(CanalReporteIncidencia.PATRULLAJE)
                .latitud(new BigDecimal("-13.0450000"))
                .longitud(new BigDecimal("-74.1380000"))
                .nivelConfidencialidad(NivelConfidencialidad.PUBLICO)
                .usuarioRegistro(supervisorUsuario)
                .build());
    }

    @Test
    @Order(1)
    @DisplayName("1. Calcular Indicadores de Supervisión en tiempo de consulta")
    void testObtenerIndicadores() {
        FiltroSupervisionRequest filtro = FiltroSupervisionRequest.builder()
                .periodo("HOY")
                .build();

        IndicadoresSupervisionResponse kpis = supervisionService.obtenerIndicadores(filtro, userSupervisor);

        assertThat(kpis).isNotNull();
        assertThat(kpis.getIncidenciasPendientesRevision()).isGreaterThanOrEqualTo(1);
        assertThat(kpis.getFechaHoraActualizacion()).isNotNull();
        assertThat(kpis.getZonaHoraria()).isEqualTo("America/Lima");
        assertThat(kpis.getPeriodoDescripcion()).contains("Hoy");
    }

    @Test
    @Order(2)
    @DisplayName("2. Obtener Bandeja Centralizada de Pendientes Operativos")
    void testObtenerBandejaPendientes() {
        FiltroSupervisionRequest filtro = new FiltroSupervisionRequest();
        List<BandejaPendienteItemDto> pendientes = supervisionService.obtenerBandejaPendientes(filtro, userSupervisor);

        assertThat(pendientes).isNotNull();
        assertThat(pendientes).isNotEmpty();

        boolean contieneIncidencia = pendientes.stream()
                .anyMatch(p -> p.getCodigo().equals(incidenciaTest.getCodigo())
                        && p.getTipoRegistro() == TipoRegistroSupervision.INCIDENCIA);
        assertThat(contieneIncidencia).isTrue();
    }

    @Test
    @Order(3)
    @DisplayName("3. Generar Mapas de Distribución de Gráficos")
    void testObtenerGraficos() {
        FiltroSupervisionRequest filtro = new FiltroSupervisionRequest();
        GraficosSupervisionDto graficos = supervisionService.obtenerGraficos(filtro, userSupervisor);

        assertThat(graficos).isNotNull();
        assertThat(graficos.getIncidenciasPorEstado()).isNotEmpty();
        assertThat(graficos.getPatrullajesPorEstado()).isNotEmpty();
        assertThat(graficos.getSeguimientosPorEstado()).isNotEmpty();
        assertThat(graficos.getSeguimientosPorPrioridad()).isNotEmpty();
    }

    @Test
    @Order(4)
    @DisplayName("4. Crear Expediente de Seguimiento con código correlativo")
    void testCrearSeguimiento() {
        CrearSeguimientoRequest req = CrearSeguimientoRequest.builder()
                .tipoRegistroOrigen(TipoRegistroSupervision.INCIDENCIA)
                .registroOrigenId(incidenciaTest.getId())
                .registroOrigenCodigo(incidenciaTest.getCodigo())
                .tipoSeguimiento(TipoSeguimientoSupervision.REVISION)
                .descripcion("Revisión administrativa de constatación de hechos y despacho")
                .prioridadAdministrativa(PrioridadAdministrativaSupervision.ALTA)
                .fechaCompromiso(LocalDate.now().plusDays(2))
                .build();

        SeguimientoResponse resp = supervisionService.crearSeguimiento(req, userSupervisor, "192.168.1.50");

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getCodigo()).startsWith("SEG-SUP-");
        assertThat(resp.getEstado()).isEqualTo(EstadoSeguimientoSupervision.ABIERTO);
        assertThat(resp.getTipoRegistroOrigen()).isEqualTo(TipoRegistroSupervision.INCIDENCIA);
        assertThat(resp.getRegistroOrigenCodigo()).isEqualTo(incidenciaTest.getCodigo());
        assertThat(resp.getSupervisorNombre()).contains("Carlos");
    }

    @Test
    @Order(5)
    @DisplayName("5. Asignar Responsable Operativo y verificar transición a EN_REVISION")
    void testAsignarResponsable() {
        CrearSeguimientoRequest req = CrearSeguimientoRequest.builder()
                .tipoRegistroOrigen(TipoRegistroSupervision.INCIDENCIA)
                .registroOrigenId(incidenciaTest.getId())
                .registroOrigenCodigo(incidenciaTest.getCodigo())
                .tipoSeguimiento(TipoSeguimientoSupervision.OBSERVACION)
                .descripcion("Constatación de deficiencia en registro de parte")
                .build();

        SeguimientoResponse resp = supervisionService.crearSeguimiento(req, userSupervisor, "127.0.0.1");

        AsignarResponsableRequest asigReq = AsignarResponsableRequest.builder()
                .responsableId(responsableUsuario.getId())
                .motivo("Asignación para informe de descargo")
                .build();

        SeguimientoResponse actualizado = supervisionService.asignarResponsable(resp.getId(), asigReq, userSupervisor, "127.0.0.1");

        assertThat(actualizado.getEstado()).isEqualTo(EstadoSeguimientoSupervision.EN_REVISION);
        assertThat(actualizado.getResponsableAsignadoId()).isEqualTo(responsableUsuario.getId());
        assertThat(actualizado.getResponsableAsignadoNombre()).contains("Raúl");
    }

    @Test
    @Order(6)
    @DisplayName("6. Ciclo de Vida: Observar -> Subsanar -> Revisar (Conforme)")
    void testCicloObservacionYSubsanacion() {
        CrearSeguimientoRequest req = CrearSeguimientoRequest.builder()
                .tipoRegistroOrigen(TipoRegistroSupervision.INCIDENCIA)
                .registroOrigenId(incidenciaTest.getId())
                .tipoSeguimiento(TipoSeguimientoSupervision.SUBSANACION)
                .descripcion("Seguimiento para aclaración formal")
                .responsableAsignadoId(responsableUsuario.getId())
                .build();

        SeguimientoResponse resp = supervisionService.crearSeguimiento(req, userSupervisor, "127.0.0.1");

        // 1. Observar
        RegistrarObservacionRequest obsReq = RegistrarObservacionRequest.builder()
                .motivo("Falta precisar el número de involucrados")
                .detalle("En el parte preliminar no se especificó la cantidad de personas congregadas en la plaza.")
                .build();
        SeguimientoResponse observado = supervisionService.registrarObservacion(resp.getId(), obsReq, userSupervisor, "127.0.0.1");
        assertThat(observado.getEstado()).isEqualTo(EstadoSeguimientoSupervision.OBSERVADO);

        // 2. Subsanar
        RegistrarSubsanacionRequest subReq = RegistrarSubsanacionRequest.builder()
                .motivo("Informe aclaratorio de campo")
                .detalle("Se verificó la concurrencia de aproximadamente 8 personas quienes se retiraron de forma pacífica.")
                .build();
        SeguimientoResponse subsanado = supervisionService.registrarSubsanacion(resp.getId(), subReq, userResponsable, "127.0.0.1");
        assertThat(subsanado.getEstado()).isEqualTo(EstadoSeguimientoSupervision.EN_SUBSANACION);

        // 3. Revisar Conforme
        RevisarRespuestaRequest revReq = RevisarRespuestaRequest.builder()
                .conforme(true)
                .observaciones("Subsanación satisfactoria y verificada.")
                .build();
        SeguimientoResponse revisado = supervisionService.revisarRespuesta(resp.getId(), revReq, userSupervisor, "127.0.0.1");
        assertThat(revisado.getEstado()).isEqualTo(EstadoSeguimientoSupervision.RESUELTO);
    }

    @Test
    @Order(7)
    @DisplayName("7. Escalamiento, Cierre y Archivo de Expediente")
    void testEscalarYCerrarExpediente() {
        CrearSeguimientoRequest req = CrearSeguimientoRequest.builder()
                .tipoRegistroOrigen(TipoRegistroSupervision.INCIDENCIA)
                .registroOrigenId(incidenciaTest.getId())
                .tipoSeguimiento(TipoSeguimientoSupervision.ESCALAMIENTO)
                .descripcion("Caso que requiere conocimiento de Subgerencia de Fiscalización")
                .build();

        SeguimientoResponse resp = supervisionService.crearSeguimiento(req, userSupervisor, "127.0.0.1");

        // Escalar
        EscalarSeguimientoRequest escReq = EscalarSeguimientoRequest.builder()
                .oficinaDestinataria("Subgerencia de Fiscalización y Control")
                .motivo("Comercio ambulatorio reincidente")
                .observaciones("Se remite copia informativa.")
                .build();
        SeguimientoResponse escalado = supervisionService.escalarSeguimiento(resp.getId(), escReq, userSupervisor, "127.0.0.1");
        assertThat(escalado.getOficinaEscalamiento()).isEqualTo("Subgerencia de Fiscalización y Control");

        // Cerrar
        CerrarSeguimientoRequest cerReq = CerrarSeguimientoRequest.builder()
                .resultado("Expediente derivado formalmente a Fiscalización. Actuación de serenazgo concluida.")
                .observacionesFinales("Archivo en supervisión.")
                .build();
        SeguimientoResponse cerrado = supervisionService.cerrarSeguimiento(resp.getId(), cerReq, userSupervisor, "127.0.0.1");
        assertThat(cerrado.getEstado()).isEqualTo(EstadoSeguimientoSupervision.CERRADO);
        assertThat(cerrado.getFechaCierre()).isNotNull();

        // No se puede modificar expediente cerrado
        assertThrows(BusinessException.class, () ->
                supervisionService.registrarObservacion(resp.getId(),
                        RegistrarObservacionRequest.builder().motivo("Test").detalle("Error").build(),
                        userSupervisor, "127.0.0.1"));
    }

    @Test
    @Order(8)
    @DisplayName("8. Exportaciones de Supervisión: Excel y PDF con Disclaimer Legal")
    void testExportacionesExcelYPdf() {
        FiltroSupervisionRequest filtro = new FiltroSupervisionRequest();

        Resource excel = supervisionService.exportarExcel(filtro, userSupervisor);
        assertThat(excel).isNotNull();
        assertThat(excel.exists()).isTrue();

        Resource pdf = supervisionService.exportarPdf(filtro, userSupervisor);
        assertThat(pdf).isNotNull();
        assertThat(pdf.exists()).isTrue();
    }

    @Test
    @Order(9)
    @DisplayName("9. Listado paginado de seguimientos con filtros")
    void testListarSeguimientos() {
        FiltroSupervisionRequest filtro = FiltroSupervisionRequest.builder()
                .tipoRegistro(TipoRegistroSupervision.INCIDENCIA)
                .build();

        Page<SeguimientoResponse> pagina = supervisionService.listarSeguimientos(filtro, PageRequest.of(0, 10), userSupervisor);
        assertThat(pagina).isNotNull();
        assertThat(pagina.getContent()).isNotNull();
    }
}
