package pe.gob.municipalidad.sigm.modules.mesapartes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.EsquemaTramiteResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.impl.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfiguracionMpvUnitTest {

    @Mock
    private TipoTramiteRepository tipoTramiteRepository;
    @Mock
    private TramiteVersionRepository tramiteVersionRepository;
    @Mock
    private RequisitoDocumentalRepository requisitoRepository;
    @Mock
    private FormularioTramiteRepository formularioRepository;
    @Mock
    private FormularioVersionRepository versionRepository;
    @Mock
    private FormularioCampoRepository campoRepository;
    @Mock
    private ExpedienteRepository expedienteRepository;
    @Mock
    private OficinaRepository oficinaRepository;
    @Mock
    private CalendarioAdministrativoRepository calendarioRepository;
    @Mock
    private PlantillaDocumentalRepository plantillaRepository;
    @Mock
    private CorrelativoExpedienteRepository correlativoRepository;
    @Mock
    private ConfiguracionHistorialRepository historialRepository;

    private ConfiguracionHistorialService historialService;
    private CatalogoTramitesService catalogoService;
    private RequisitosTramiteService requisitosService;
    private AdministracionFormulariosService formulariosService;
    private ConfiguracionPlazosService plazosService;
    private CalendarioAdministrativoService calendarioService;
    private PlantillasDocumentalesService plantillasService;
    private ConfiguracionNumeracionService numeracionService;

    private Oficina oficinaActiva;
    private Oficina oficinaInactiva;
    private TipoTramite tramiteBorrador;
    private TipoTramite tramitePublicado;

    @BeforeEach
    void setUp() {
        historialService = new ConfiguracionHistorialServiceImpl(historialRepository);

        catalogoService = new CatalogoTramitesServiceImpl(
                tipoTramiteRepository,
                tramiteVersionRepository,
                requisitoRepository,
                formularioRepository,
                expedienteRepository,
                oficinaRepository,
                historialService
        );

        requisitosService = new RequisitosTramiteServiceImpl(
                requisitoRepository,
                tipoTramiteRepository,
                historialService
        );

        formulariosService = new AdministracionFormulariosServiceImpl(
                formularioRepository,
                versionRepository,
                campoRepository,
                tipoTramiteRepository,
                requisitoRepository,
                historialService
        );

        plazosService = new ConfiguracionPlazosServiceImpl(
                tipoTramiteRepository,
                catalogoService,
                historialService
        );

        calendarioService = new CalendarioAdministrativoServiceImpl(
                calendarioRepository,
                historialService
        );

        plantillasService = new PlantillasDocumentalesServiceImpl(
                plantillaRepository,
                historialService
        );

        numeracionService = new ConfiguracionNumeracionServiceImpl(
                correlativoRepository
        );

        oficinaActiva = Oficina.builder()
                .id(1L)
                .codigo("OF-URB")
                .nombre("Gerencia de Desarrollo Urbano")
                .estado("ACTIVO")
                .build();

        oficinaInactiva = Oficina.builder()
                .id(2L)
                .codigo("OF-OLD")
                .nombre("Oficina Desactivada")
                .estado("INACTIVO")
                .build();

        tramiteBorrador = TipoTramite.builder()
                .id(10L)
                .codigo("TR-BORR-01")
                .nombre("Licencia de Edificación")
                .denominacionOficial("Licencia de Edificación Modalidad A")
                .oficinaResponsable(oficinaActiva)
                .plazoDias(30)
                .unidadPlazo(UnidadPlazo.DIAS_HABILES)
                .versionActual(1)
                .estadoCatalogo(EstadoCatalogoTramite.BORRADOR)
                .requiereDocumentos(true)
                .permitePresentacionVirtual(true)
                .permitePresencial(true)
                .estado("ACTIVO")
                .createdBy("admin")
                .build();

        tramitePublicado = TipoTramite.builder()
                .id(20L)
                .codigo("TR-PUB-01")
                .nombre("Certificado de Parámetros Urbanísticos")
                .oficinaResponsable(oficinaActiva)
                .plazoDias(5)
                .unidadPlazo(UnidadPlazo.DIAS_HABILES)
                .versionActual(1)
                .estadoCatalogo(EstadoCatalogoTramite.PUBLICADO)
                .estado("ACTIVO")
                .createdBy("admin")
                .build();
    }

    // =========================================================================
    // 1. PRUEBAS DE CATÁLOGO DE TRÁMITES
    // =========================================================================

    @Test
    @DisplayName("1. Debe crear un trámite en estado BORRADOR con código único y oficina activa")
    void testCrearTramiteBorradorExitoso() {
        GuardarTramiteRequest req = GuardarTramiteRequest.builder()
                .codigo("TR-NUEVO-01")
                .nombre("Constancia de Posesión")
                .oficinaResponsableId(1L)
                .plazoDias(15)
                .unidadPlazo(UnidadPlazo.DIAS_HABILES)
                .categoria("URBANISMO")
                .requiereTasa(true)
                .montoTasa(new BigDecimal("35.50"))
                .build();

        when(tipoTramiteRepository.existsByCodigoIgnoreCase("TR-NUEVO-01")).thenReturn(false);
        when(oficinaRepository.findById(1L)).thenReturn(Optional.of(oficinaActiva));
        when(tipoTramiteRepository.save(any(TipoTramite.class))).thenAnswer(i -> {
            TipoTramite t = i.getArgument(0);
            t.setId(100L);
            return t;
        });

        TramiteAdminResponse resp = catalogoService.crearBorrador(req, "operador", "127.0.0.1");

        assertThat(resp).isNotNull();
        assertThat(resp.getCodigo()).isEqualTo("TR-NUEVO-01");
        assertThat(resp.getEstadoCatalogo()).isEqualTo(EstadoCatalogoTramite.BORRADOR);
        assertThat(resp.getVersionActual()).isEqualTo(1);
    }

    @Test
    @DisplayName("2. Debe rechazar creación de trámite con código duplicado")
    void testRechazarCodigoDuplicado() {
        GuardarTramiteRequest req = GuardarTramiteRequest.builder()
                .codigo("TR-DUPLICADO")
                .nombre("Trámite X")
                .oficinaResponsableId(1L)
                .plazoDias(10)
                .build();

        when(tipoTramiteRepository.existsByCodigoIgnoreCase("TR-DUPLICADO")).thenReturn(true);

        assertThatThrownBy(() -> catalogoService.crearBorrador(req, "admin", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un trámite registrado con el código");
    }

    @Test
    @DisplayName("3. Debe rechazar asociación a una oficina inactiva")
    void testRechazarOficinaInactiva() {
        GuardarTramiteRequest req = GuardarTramiteRequest.builder()
                .codigo("TR-OF-INACTIVA")
                .nombre("Trámite Inválido")
                .oficinaResponsableId(2L)
                .plazoDias(10)
                .build();

        when(tipoTramiteRepository.existsByCodigoIgnoreCase("TR-OF-INACTIVA")).thenReturn(false);
        when(oficinaRepository.findById(2L)).thenReturn(Optional.of(oficinaInactiva));

        assertThatThrownBy(() -> catalogoService.crearBorrador(req, "admin", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("estado inactivo");
    }

    @Test
    @DisplayName("4. Debe rechazar la edición directa de un trámite en estado PUBLICADO")
    void testRechazarEdicionDirectaTramitePublicado() {
        GuardarTramiteRequest req = GuardarTramiteRequest.builder()
                .codigo("TR-PUB-01")
                .nombre("Cambio Ilegal")
                .oficinaResponsableId(1L)
                .plazoDias(10)
                .build();

        when(tipoTramiteRepository.findById(20L)).thenReturn(Optional.of(tramitePublicado));

        assertThatThrownBy(() -> catalogoService.editarBorrador(20L, req, "admin", "127.0.0.1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se puede editar directamente un procedimiento en estado PUBLICADO");
    }

    @Test
    @DisplayName("5. Flujo de ciclo de vida: BORRADOR -> EN_REVISION -> APROBADO -> PUBLICADO")
    void testCicloDeVidaTramiteCompleto() {
        when(tipoTramiteRepository.findById(10L)).thenReturn(Optional.of(tramiteBorrador));
        when(tipoTramiteRepository.save(any(TipoTramite.class))).thenAnswer(i -> i.getArgument(0));
        when(requisitoRepository.countByTipoTramiteIdAndObligatorioTrueAndEstado(10L, "ACTIVO")).thenReturn(2L);

        // 1. Enviar a revisión
        TramiteAdminResponse rev = catalogoService.enviarARevision(10L, new AccionTransicionRequest("Listo para revisión"), "creador", "127.0.0.1");
        assertThat(rev.getEstadoCatalogo()).isEqualTo(EstadoCatalogoTramite.EN_REVISION);

        // 2. Aprobar
        TramiteAdminResponse apr = catalogoService.aprobar(10L, new AccionTransicionRequest("Visto bueno legal"), "jefe_oficina", "127.0.0.1");
        assertThat(apr.getEstadoCatalogo()).isEqualTo(EstadoCatalogoTramite.APROBADO);
        assertThat(apr.getAprobadoPor()).isEqualTo("jefe_oficina");

        // 3. Publicar (crea versión inmutable)
        TramiteAdminResponse pub = catalogoService.publicar(10L, new AccionTransicionRequest("Publicación oficial"), "superadmin", "127.0.0.1");
        assertThat(pub.getEstadoCatalogo()).isEqualTo(EstadoCatalogoTramite.PUBLICADO);
        assertThat(pub.getPublicadoPor()).isEqualTo("superadmin");

        verify(tramiteVersionRepository, times(1)).save(any(TramiteVersion.class));
    }

    @Test
    @DisplayName("6. Debe suspender trámite en estado PUBLICADO")
    void testSuspenderTramitePublicado() {
        when(tipoTramiteRepository.findById(20L)).thenReturn(Optional.of(tramitePublicado));
        when(tipoTramiteRepository.save(any(TipoTramite.class))).thenAnswer(i -> i.getArgument(0));

        TramiteAdminResponse susp = catalogoService.suspender(20L, new AccionTransicionRequest("Mantenimiento TUPA"), "admin", "127.0.0.1");

        assertThat(susp.getEstadoCatalogo()).isEqualTo(EstadoCatalogoTramite.SUSPENDIDO);
    }

    @Test
    @DisplayName("7. Debe rechazar desactivación de trámite con expedientes activos en curso")
    void testRechazarDesactivacionConExpedientesActivos() {
        when(tipoTramiteRepository.findById(20L)).thenReturn(Optional.of(tramitePublicado));
        when(expedienteRepository.countByTipoTramiteIdAndEstadoActualNotIn(eq(20L), any())).thenReturn(5L);

        assertThatThrownBy(() -> catalogoService.desactivar(20L, new AccionTransicionRequest("Baja"), "admin", "127.0.0.1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tiene 5 expedientes en curso");
    }

    // =========================================================================
    // 2. PRUEBAS DE REQUISITOS DOCUMENTALES
    // =========================================================================

    @Test
    @DisplayName("8. Debe guardar requisito documental con orden y formatos válidos")
    void testGuardarRequisitoExitoso() {
        GuardarRequisitoRequest req = GuardarRequisitoRequest.builder()
                .tipoTramiteId(10L)
                .codigo("REQ-DNI-01")
                .nombre("Copia simple de DNI")
                .tamanoMaximoMb(15)
                .orden(1)
                .obligatorio(true)
                .build();

        when(tipoTramiteRepository.findById(10L)).thenReturn(Optional.of(tramiteBorrador));
        when(requisitoRepository.findByTipoTramiteIdAndCodigo(10L, "REQ-DNI-01")).thenReturn(Optional.empty());
        when(requisitoRepository.save(any(RequisitoDocumental.class))).thenAnswer(i -> {
            RequisitoDocumental r = i.getArgument(0);
            r.setId(50L);
            return r;
        });

        RequisitoAdminResponse resp = requisitosService.guardarRequisito(req, "admin", "127.0.0.1");

        assertThat(resp).isNotNull();
        assertThat(resp.getCodigo()).isEqualTo("REQ-DNI-01");
        assertThat(resp.getObligatorio()).isTrue();
    }

    @Test
    @DisplayName("9. Debe rechazar requisito con código duplicado en el mismo trámite")
    void testRechazarRequisitoDuplicado() {
        GuardarRequisitoRequest req = GuardarRequisitoRequest.builder()
                .tipoTramiteId(10L)
                .codigo("REQ-EXISTENTE")
                .nombre("Plano")
                .build();

        when(tipoTramiteRepository.findById(10L)).thenReturn(Optional.of(tramiteBorrador));
        when(requisitoRepository.findByTipoTramiteIdAndCodigo(10L, "REQ-EXISTENTE"))
                .thenReturn(Optional.of(new RequisitoDocumental()));

        assertThatThrownBy(() -> requisitosService.guardarRequisito(req, "admin", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un requisito con el código");
    }

    // =========================================================================
    // 3. PRUEBAS DE FORMULARIOS DINÁMICOS Y ANTI-XSS
    // =========================================================================

    @Test
    @DisplayName("10. Anti-XSS: Debe rechazar scripts maliciosos en etiquetas de formulario")
    void testRechazarXssEnFormulario() {
        GuardarFormularioRequest req = GuardarFormularioRequest.builder()
                .tipoTramiteId(10L)
                .codigo("FORM-XSS")
                .nombre("Formulario <script>alert('hack')</script>")
                .campos(List.of())
                .build();

        when(tipoTramiteRepository.findById(10L)).thenReturn(Optional.of(tramiteBorrador));

        assertThatThrownBy(() -> formulariosService.guardarFormulario(req, "attacker", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expresiones no permitidas por razones de seguridad");
    }

    @Test
    @DisplayName("11. Previsualización: Debe generar esquema de formulario con marca de vista previa")
    void testGenerarVistaPreviaFormulario() {
        FormularioTramite form = FormularioTramite.builder()
                .id(100L)
                .tipoTramite(tramiteBorrador)
                .codigo("FORM-TR-10")
                .nombre("Solicitud de Licencia")
                .activo(true)
                .build();

        FormularioVersion fv = FormularioVersion.builder()
                .id(200L)
                .formulario(form)
                .version(1)
                .campos(new ArrayList<>(List.of(
                        FormularioCampo.builder()
                                .id(1L)
                                .nombreCampo("area_terreno")
                                .etiqueta("Área del Terreno (m²)")
                                .tipoCampo(TipoCampoFormulario.NUMERO)
                                .obligatorio(true)
                                .build()
                )))
                .build();

        when(formularioRepository.findById(100L)).thenReturn(Optional.of(form));
        when(versionRepository.findFirstByFormularioIdOrderByVersionDesc(100L)).thenReturn(Optional.of(fv));
        when(requisitoRepository.findByTipoTramiteIdOrderByOrdenAsc(10L)).thenReturn(List.of());

        EsquemaTramiteResponse prev = formulariosService.generarVistaPrevia(100L);

        assertThat(prev).isNotNull();
        assertThat(prev.getNombre()).contains("[PREVISUALIZACIÓN]");
        assertThat(prev.getFormulario().getCampos()).hasSize(1);
        assertThat(prev.getFormulario().getCampos().get(0).getNombreCampo()).isEqualTo("area_terreno");
    }

    // =========================================================================
    // 4. PRUEBAS DE PLAZOS Y CALENDARIO
    // =========================================================================

    @Test
    @DisplayName("12. Debe actualizar plazo administrativo y validar límite positivo")
    void testActualizarPlazoExitoso() {
        when(tipoTramiteRepository.findById(10L)).thenReturn(Optional.of(tramiteBorrador));
        when(tipoTramiteRepository.save(any(TipoTramite.class))).thenAnswer(i -> i.getArgument(0));

        plazosService.actualizarPlazo(10L, 20, UnidadPlazo.DIAS_HABILES, "Ley 27444", "Ajuste normativo", "admin", "127.0.0.1");

        assertThat(tramiteBorrador.getPlazoDias()).isEqualTo(20);
        assertThat(tramiteBorrador.getUnidadPlazo()).isEqualTo(UnidadPlazo.DIAS_HABILES);
    }

    @Test
    @DisplayName("13. Debe rechazar plazos no positivos o mayores a 365 días")
    void testRechazarPlazoInvalido() {
        assertThatThrownBy(() -> plazosService.actualizarPlazo(10L, 0, UnidadPlazo.DIAS_HABILES, null, null, "admin", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("número entero positivo");

        assertThatThrownBy(() -> plazosService.actualizarPlazo(10L, 400, UnidadPlazo.DIAS_HABILES, null, null, "admin", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no puede exceder 365 días");
    }

    @Test
    @DisplayName("14. Calendario: Debe registrar y reconocer feriado en Quinua")
    void testCalendarioFeriadoQuinua() {
        LocalDate fechaBatalla = LocalDate.of(2026, 12, 9);
        GuardarCalendarioRequest req = GuardarCalendarioRequest.builder()
                .fecha(fechaBatalla)
                .nombre("Batalla de Ayacucho - Pampa de Quinua")
                .tipo(TipoCalendarioFeriado.FERIADO_NACIONAL)
                .esLaborable(false)
                .activo(true)
                .build();

        CalendarioAdministrativo cal = CalendarioAdministrativo.builder()
                .id(1L)
                .fecha(fechaBatalla)
                .nombre(req.getNombre())
                .tipo(req.getTipo())
                .esLaborable(false)
                .activo(true)
                .build();

        when(calendarioRepository.findByFecha(fechaBatalla)).thenReturn(Optional.empty());
        when(calendarioRepository.save(any(CalendarioAdministrativo.class))).thenReturn(cal);

        CalendarioAdminResponse resp = calendarioService.guardarFeriado(req, "admin", "127.0.0.1");

        assertThat(resp).isNotNull();
        assertThat(resp.getFecha()).isEqualTo(fechaBatalla);
        assertThat(resp.getEsLaborable()).isFalse();

        when(calendarioRepository.findByFecha(fechaBatalla)).thenReturn(Optional.of(cal));
        assertThat(calendarioService.esDiaNoLaborable(fechaBatalla)).isTrue();
    }

    // =========================================================================
    // 5. PRUEBAS DE PLANTILLAS DOCUMENTALES
    // =========================================================================

    @Test
    @DisplayName("15. Plantillas: Debe sustituir variables autorizadas sin permitir inyección")
    void testSustitucionVariablesPlantilla() {
        PlantillaDocumental plant = PlantillaDocumental.builder()
                .id(1L)
                .codigo("PLANT-CARGO-TEST")
                .nombre("Cargo de Prueba")
                .tipoDocumental(TipoDocumentalPlantilla.CARGO_RECEPCION)
                .cuerpoPlantilla("MUNICIPALIDAD DE QUINUA\nExpediente: {{numero_expediente}}\nSolicitante: {{solicitante_nombre}}\nResultado: {{resultado_final}}")
                .version(1)
                .estado("PUBLICADO")
                .build();

        when(plantillaRepository.findById(1L)).thenReturn(Optional.of(plant));

        Map<String, String> variables = Map.of(
                "numero_expediente", "MPV-2026-000001",
                "solicitante_nombre", "CIRILO SULCA",
                "resultado_final", "PROCEDENTE"
        );

        String render = plantillasService.previsualizarContenido(1L, variables);

        assertThat(render).contains("MPV-2026-000001");
        assertThat(render).contains("CIRILO SULCA");
        assertThat(render).contains("PROCEDENTE");
        assertThat(render).doesNotContain("{{numero_expediente}}");
    }

    // =========================================================================
    // 6. PRUEBAS DE NUMERACIÓN Y AUDITORÍA
    // =========================================================================

    @Test
    @DisplayName("16. Numeración: Debe consultar correlativo actual y formatear siguiente secuencia")
    void testConsultaNumeracion() {
        CorrelativoExpediente corr = CorrelativoExpediente.builder()
                .id(1L)
                .prefijo("MPV")
                .anio(2026)
                .ultimoNumero(45)
                .build();

        when(correlativoRepository.findByPrefijoAndAnio("MPV", 2026)).thenReturn(Optional.of(corr));

        NumeracionResponse resp = numeracionService.obtenerCorrelativoActual(2026);

        assertThat(resp).isNotNull();
        assertThat(resp.getUltimoNumero()).isEqualTo(45L);
        assertThat(resp.getFormatoSugerido()).isEqualTo("MPV-2026-000046");
    }

    @Test
    @DisplayName("17. Auditoría: Debe registrar eventos de configuración")
    void testAuditoriaConfiguracion() {
        historialService.registrarCambio(
                TipoEntidadConfiguracion.TRAMITE,
                10L,
                "EXP-001",
                TipoCambioConfiguracion.CREACION,
                null,
                1,
                "Creación de prueba",
                null,
                1L,
                "admin",
                "127.0.0.1"
        );

        verify(historialRepository, times(1)).save(any(ConfiguracionHistorial.class));
    }
}
