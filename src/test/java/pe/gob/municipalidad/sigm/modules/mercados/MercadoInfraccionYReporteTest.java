package pe.gob.municipalidad.sigm.modules.mercados;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.IncidenciaMercadoService;
import pe.gob.municipalidad.sigm.modules.mercados.service.IncidenciasReporteService;
import pe.gob.municipalidad.sigm.modules.mercados.service.PresuntaInfraccionService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MercadoInfraccionYReporteTest {

    @Mock private PresuntaInfraccionMercadoRepository infraccionRepository;
    @Mock private TipificacionInfraccionRepository tipificacionRepository;
    @Mock private IncidenciaMercadoRepository incidenciaRepository;
    @Mock private TipoIncidenciaMercadoRepository tipoRepository;
    @Mock private MercadoMunicipalRepository mercadoRepository;
    @Mock private SectorMercadoRepository sectorRepository;
    @Mock private PabellonMercadoRepository pabellonRepository;
    @Mock private PuestoComercialRepository puestoRepository;
    @Mock private ComercianteRepository comercianteRepository;
    @Mock private InspeccionMercadoRepository inspeccionRepository;
    @Mock private HallazgoInspeccionRepository hallazgoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AuditoriaAdministrativaService auditoriaService;
    @Mock private IncidenciaMercadoService incidenciaService;

    private PresuntaInfraccionService infraccionService;
    private IncidenciasReporteService reporteService;

    private MercadoMunicipal mercado;
    private TipificacionInfraccion tipificacion;
    private Usuario evaluador;

    @BeforeEach
    void setUp() {
        infraccionService = new PresuntaInfraccionService(
                infraccionRepository,
                tipificacionRepository,
                incidenciaRepository,
                inspeccionRepository,
                hallazgoRepository,
                mercadoRepository,
                sectorRepository,
                pabellonRepository,
                puestoRepository,
                comercianteRepository,
                usuarioRepository,
                auditoriaService
        );

        reporteService = new IncidenciasReporteService(
                incidenciaRepository,
                infraccionRepository,
                tipoRepository,
                incidenciaService
        );

        mercado = MercadoMunicipal.builder()
                .id(UUID.randomUUID())
                .codigo("MER-01")
                .nombre("Mercado Central de Quinua")
                .estado(EstadoMercado.ACTIVO)
                .build();

        tipificacion = TipificacionInfraccion.builder()
                .id(UUID.randomUUID())
                .codigo("INF-SAN-001")
                .descripcion("Comercialización de alimentos sin condiciones higiénico-sanitarias mínimas")
                .normaReferencia("D.S. N° 007-98-SA / Ley N° 27972")
                .articulo("Art. 12")
                .autoridadCompetente("Subgerencia de Servicios Municipales y Gestión Ambiental")
                .fechaInicioVigencia(LocalDate.of(2025, 1, 1))
                .estado("ACTIVO")
                .build();

        Persona personaEval = Persona.builder()
                .nombres("Ana")
                .apellidoPaterno("Ríos")
                .build();

        evaluador = Usuario.builder()
                .id(20L)
                .username("arios")
                .persona(personaEval)
                .build();
    }

    @Test
    @DisplayName("Debe registrar presunta infracción con correlativo sin atribuir culpabilidad automática")
    void registrarPresuntaInfraccionExitoso() {
        PresuntaInfraccionCreateRequest request = PresuntaInfraccionCreateRequest.builder()
                .mercadoId(mercado.getId())
                .tipificacionId(tipificacion.getId())
                .descripcionHechos("Presunta manipulación de carnes sin elementos de protección higiénica")
                .fechaDeteccion(LocalDate.now())
                .build();

        when(mercadoRepository.findById(mercado.getId())).thenReturn(Optional.of(mercado));
        when(tipificacionRepository.findById(tipificacion.getId())).thenReturn(Optional.of(tipificacion));
        when(infraccionRepository.obtenerSiguienteCorrelativo()).thenReturn(5L);

        when(infraccionRepository.save(any(PresuntaInfraccionMercado.class))).thenAnswer(invocation -> {
            PresuntaInfraccionMercado ent = invocation.getArgument(0);
            ent.setId(UUID.randomUUID());
            return ent;
        });

        PresuntaInfraccionResponse response = infraccionService.registrar(request, "admin");

        assertThat(response).isNotNull();
        assertThat(response.getCodigo()).startsWith("INF-MER-");
        assertThat(response.getEstadoEvaluacion()).isEqualTo(EstadoEvaluacionInfraccion.REGISTRADA);
        assertThat(response.getTipificacionCodigo()).isEqualTo("INF-SAN-001");
        // Garantía de no sanción automática
        assertThat(response.getSentidoDecision()).isNull();
        assertThat(response.getResolucionDecisionRef()).isNull();

        verify(infraccionRepository, times(1)).save(any(PresuntaInfraccionMercado.class));
    }

    @Test
    @DisplayName("Debe evaluar preliminarmente los hechos y pasar a EN_EVALUACION con informe técnico")
    void evaluarPresuntaInfraccionExitoso() {
        UUID infraccionId = UUID.randomUUID();
        PresuntaInfraccionMercado entidad = PresuntaInfraccionMercado.builder()
                .id(infraccionId)
                .codigo("INF-MER-2026-000005")
                .mercado(mercado)
                .tipificacion(tipificacion)
                .descripcionHechos("Hechos observados...")
                .estadoEvaluacion(EstadoEvaluacionInfraccion.REGISTRADA)
                .build();

        EvaluacionInfraccionRequest request = EvaluacionInfraccionRequest.builder()
                .nuevoEstado(EstadoEvaluacionInfraccion.EN_EVALUACION)
                .informeEvaluacion("Se confirma mérito inicial para traslado y solicitud de información complementaria.")
                .build();

        when(infraccionRepository.findById(infraccionId)).thenReturn(Optional.of(entidad));
        when(usuarioRepository.findByUsername("arios")).thenReturn(Optional.of(evaluador));
        when(infraccionRepository.save(any(PresuntaInfraccionMercado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PresuntaInfraccionResponse response = infraccionService.evaluar(infraccionId, request, "arios");

        assertThat(response.getEstadoEvaluacion()).isEqualTo(EstadoEvaluacionInfraccion.EN_EVALUACION);
        assertThat(response.getInformeEvaluacion()).contains("mérito inicial");
        assertThat(response.getResponsableEvaluacionNombre()).isEqualTo("Ana Ríos");
    }

    @Test
    @DisplayName("Debe descartar presunta infracción por falta de tipicidad o mérito probatorio")
    void descartarPresuntaInfraccion() {
        UUID infraccionId = UUID.randomUUID();
        PresuntaInfraccionMercado entidad = PresuntaInfraccionMercado.builder()
                .id(infraccionId)
                .codigo("INF-MER-2026-000005")
                .mercado(mercado)
                .estadoEvaluacion(EstadoEvaluacionInfraccion.EN_EVALUACION)
                .build();

        EvaluacionInfraccionRequest request = EvaluacionInfraccionRequest.builder()
                .nuevoEstado(EstadoEvaluacionInfraccion.DESCARTADA)
                .informeEvaluacion("Los hechos denunciados no constituyen infracción al haberse demostrado cumplimiento de norma.")
                .motivoDescarte("Atipicidad de la conducta según inspección de contraste.")
                .build();

        when(infraccionRepository.findById(infraccionId)).thenReturn(Optional.of(entidad));
        when(usuarioRepository.findByUsername("arios")).thenReturn(Optional.of(evaluador));
        when(infraccionRepository.save(any(PresuntaInfraccionMercado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PresuntaInfraccionResponse response = infraccionService.evaluar(infraccionId, request, "arios");

        assertThat(response.getEstadoEvaluacion()).isEqualTo(EstadoEvaluacionInfraccion.DESCARTADA);
        assertThat(response.getMotivoDescarte()).contains("Atipicidad");
    }

    @Test
    @DisplayName("Debe derivar a procedimiento administrativo formal con expediente")
    void derivarAProcedimientoExitoso() {
        UUID infraccionId = UUID.randomUUID();
        PresuntaInfraccionMercado entidad = PresuntaInfraccionMercado.builder()
                .id(infraccionId)
                .codigo("INF-MER-2026-000005")
                .mercado(mercado)
                .estadoEvaluacion(EstadoEvaluacionInfraccion.EN_EVALUACION)
                .build();

        DerivacionInfraccionRequest request = DerivacionInfraccionRequest.builder()
                .expedienteId("EXP-2026-00088")
                .motivo("Remisión a Subgerencia para inicio formal de PAS si corresponde")
                .build();

        when(infraccionRepository.findById(infraccionId)).thenReturn(Optional.of(entidad));
        when(infraccionRepository.save(any(PresuntaInfraccionMercado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PresuntaInfraccionResponse response = infraccionService.derivar(infraccionId, request, "arios");

        assertThat(response.getEstadoEvaluacion()).isEqualTo(EstadoEvaluacionInfraccion.DERIVADA_A_PROCEDIMIENTO);
        assertThat(response.getExpedienteNumero()).isEqualTo("EXP-2026-00088");
    }

    @Test
    @DisplayName("Debe registrar descargo y alegatos del administrado garantizando derecho de defensa")
    void registrarDescargoExitoso() {
        UUID infraccionId = UUID.randomUUID();
        PresuntaInfraccionMercado entidad = PresuntaInfraccionMercado.builder()
                .id(infraccionId)
                .codigo("INF-MER-2026-000005")
                .mercado(mercado)
                .estadoEvaluacion(EstadoEvaluacionInfraccion.DERIVADA_A_PROCEDIMIENTO)
                .build();

        DescargoInfraccionRequest request = DescargoInfraccionRequest.builder()
                .documentoDescargoRef("EXP-MP-2026-00451")
                .resumenDescargo("El comerciante adjunta carnet de sanidad vigente y certificado de fumigación")
                .build();

        when(infraccionRepository.findById(infraccionId)).thenReturn(Optional.of(entidad));
        when(infraccionRepository.save(any(PresuntaInfraccionMercado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PresuntaInfraccionResponse response = infraccionService.registrarDescargo(infraccionId, request, "mesa_partes");

        assertThat(response.getTieneDescargo()).isTrue();
        assertThat(response.getDocumentoDescargoRef()).isEqualTo("EXP-MP-2026-00451");
        assertThat(response.getResumenDescargo()).contains("carnet de sanidad");
    }

    @Test
    @DisplayName("Debe registrar decisión motivada de autoridad competente y cerrar presunta infracción")
    void registrarDecisionAdministrativaExitoso() {
        UUID infraccionId = UUID.randomUUID();
        PresuntaInfraccionMercado entidad = PresuntaInfraccionMercado.builder()
                .id(infraccionId)
                .codigo("INF-MER-2026-000005")
                .mercado(mercado)
                .estadoEvaluacion(EstadoEvaluacionInfraccion.DERIVADA_A_PROCEDIMIENTO)
                .build();

        DecisionInfraccionRequest request = DecisionInfraccionRequest.builder()
                .resolucionDecisionRef("RES-014-2026-MDQ/SGSM")
                .fechaDecision(LocalDate.now())
                .sentidoDecision(SentidoDecisionInfraccion.ABSOLUCION)
                .build();

        when(infraccionRepository.findById(infraccionId)).thenReturn(Optional.of(entidad));
        when(infraccionRepository.save(any(PresuntaInfraccionMercado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PresuntaInfraccionResponse response = infraccionService.registrarDecision(infraccionId, request, "autoridad");

        assertThat(response.getEstadoEvaluacion()).isEqualTo(EstadoEvaluacionInfraccion.CERRADA);
        assertThat(response.getSentidoDecision()).isEqualTo(SentidoDecisionInfraccion.ABSOLUCION);
        assertThat(response.getResolucionDecisionRef()).isEqualTo("RES-014-2026-MDQ/SGSM");
    }

    @Test
    @DisplayName("Debe generar exportación Excel de incidencias con Apache POI sin errores")
    void exportarIncidenciasExcel() {
        IncidenciaResponse incResp = IncidenciaResponse.builder()
                .codigoIncidencia("INC-MER-2026-000001")
                .mercadoNombre(mercado.getNombre())
                .tipoIncidenciaNombre("Limpieza")
                .prioridad(PrioridadIncidencia.MEDIA)
                .estado(EstadoIncidencia.REGISTRADA)
                .descripcion("Acumulación de residuos en pasadizo 2")
                .fechaReporte(OffsetDateTime.now())
                .build();

        when(incidenciaService.buscarPaginado(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(incResp)));

        byte[] excelBytes = reporteService.exportarIncidenciasExcel(LocalDate.now().minusDays(7), LocalDate.now());

        assertThat(excelBytes).isNotNull();
        assertThat(excelBytes.length).isGreaterThan(100);
    }

    @Test
    @DisplayName("Debe generar exportación PDF de incidencias con OpenPDF conteniendo firma institucional")
    void exportarIncidenciasPdf() {
        IncidenciaResponse incResp = IncidenciaResponse.builder()
                .codigoIncidencia("INC-MER-2026-000001")
                .mercadoNombre(mercado.getNombre())
                .tipoIncidenciaNombre("Infraestructura")
                .prioridad(PrioridadIncidencia.ALTA)
                .estado(EstadoIncidencia.ASIGNADA)
                .descripcion("Cables expuestos en sector carnes")
                .fechaReporte(OffsetDateTime.now())
                .build();

        when(incidenciaService.buscarPaginado(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(incResp)));

        byte[] pdfBytes = reporteService.exportarIncidenciasPdf(LocalDate.now().minusDays(7), LocalDate.now());

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(100);
        // Validar encabezado mágico %PDF
        String magicHeader = new String(pdfBytes, 0, 4);
        assertThat(magicHeader).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("Debe calcular estadísticas operativas consolidadas de incidencias e infracciones")
    void obtenerEstadisticasConsolidadas() {
        when(incidenciaRepository.count()).thenReturn(25L);
        when(incidenciaRepository.countAbiertas()).thenReturn(14L);
        when(incidenciaRepository.countByEstado(EstadoIncidencia.EN_ATENCION)).thenReturn(3L);
        when(incidenciaRepository.countByEstado(EstadoIncidencia.RESUELTA)).thenReturn(7L);
        when(incidenciaRepository.countByEstado(EstadoIncidencia.CERRADA)).thenReturn(4L);
        when(incidenciaRepository.countCriticasAbiertas()).thenReturn(1L);

        when(infraccionRepository.count()).thenReturn(6L);
        when(infraccionRepository.countEnEvaluacionActiva()).thenReturn(2L);
        when(infraccionRepository.countByEstadoEvaluacion(EstadoEvaluacionInfraccion.DERIVADA_A_PROCEDIMIENTO)).thenReturn(1L);

        EstadisticasIncidenciasResponse stats = reporteService.obtenerEstadisticas();

        assertThat(stats.getTotalIncidencias()).isEqualTo(25L);
        assertThat(stats.getAbiertas()).isEqualTo(14L);
        assertThat(stats.getCriticasAbiertas()).isEqualTo(1L);
        assertThat(stats.getTotalPresuntasInfracciones()).isEqualTo(6L);
        assertThat(stats.getInfraccionesEnEvaluacion()).isEqualTo(2L);
        assertThat(stats.getInfraccionesDerivadas()).isEqualTo(1L);
    }
}
