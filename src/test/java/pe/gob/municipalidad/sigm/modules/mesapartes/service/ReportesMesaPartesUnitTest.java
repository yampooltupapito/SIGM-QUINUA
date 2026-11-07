package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import pe.gob.municipalidad.sigm.common.model.*;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ReporteDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.impl.*;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - Reportes, Estadísticas y Supervisión de Mesa de Partes (Prompt 18)")
class ReportesMesaPartesUnitTest {

    @Mock
    private ExpedienteRepository expedienteRepository;
    @Mock
    private DerivacionExpedienteRepository derivacionRepository;
    @Mock
    private ObservacionExpedienteRepository observacionRepository;
    @Mock
    private SubsanacionExpedienteRepository subsanacionRepository;
    @Mock
    private ActuacionExpedienteRepository actuacionRepository;
    @Mock
    private AsignacionExpedienteRepository asignacionRepository;
    @Mock
    private OficinaRepository oficinaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PersonaOficinaRepository personaOficinaRepository;
    @Mock
    private ReporteExportacionRepository exportacionRepository;
    @Mock
    private PlazoAdministrativoService plazoService;
    @Mock
    private ReporteMesaPartesService reporteService;

    // ==================== 1. SANITIZACIÓN DE FÓRMULAS EXCEL ====================
    @Test
    @DisplayName("Debe neutralizar inyecciones de fórmulas maliciosas en celdas de Excel")
    void testSanitizacionFormulaExcel() {
        assertEquals("'=SUM(A1:A10)", ReporteFilterHelper.sanitizarParaExcel("=SUM(A1:A10)"));
        assertEquals("'+cmd|' /C calc'!A0", ReporteFilterHelper.sanitizarParaExcel("+cmd|' /C calc'!A0"));
        assertEquals("'-2+3", ReporteFilterHelper.sanitizarParaExcel("-2+3"));
        assertEquals("'@SUM(1,2)", ReporteFilterHelper.sanitizarParaExcel("@SUM(1,2)"));
        assertEquals("EXP-2026-0001", ReporteFilterHelper.sanitizarParaExcel("EXP-2026-0001"));
        assertEquals("Solicitud de Licencia", ReporteFilterHelper.sanitizarParaExcel("Solicitud de Licencia"));
        assertEquals("", ReporteFilterHelper.sanitizarParaExcel(null));
        assertEquals("", ReporteFilterHelper.sanitizarParaExcel("   "));
    }

    // ==================== 2. CONTROL ANTI-IDOR POR OFICINA ====================
    @Test
    @DisplayName("Debe permitir al Administrador General consultar cualquier oficina o de forma global")
    void testControlAccesoOficina_AdminGlobal() {
        Rol rolAdmin = Rol.builder().codigo("ROLE_ADMIN").nombre("Administrador").build();
        Usuario admin = Usuario.builder()
                .id(1L)
                .username("admin")
                .roles(Set.of(rolAdmin))
                .build();

        Long ofiResult = ReporteFilterHelper.resolverOficinaPermitida(admin, null, personaOficinaRepository);
        assertNull(ofiResult, "El administrador general puede consultar todas las oficinas (null)");

        Long ofiEspecifica = ReporteFilterHelper.resolverOficinaPermitida(admin, 5L, personaOficinaRepository);
        assertEquals(5L, ofiEspecifica, "El administrador general puede filtrar cualquier oficina específica");
    }

    @Test
    @DisplayName("Debe rechazar con AccessDeniedException si un usuario restringido intenta consultar oficina ajena (Anti-IDOR)")
    void testControlAccesoOficina_RechazaOficinaAjena() {
        Persona persona = Persona.builder().id(10L).nombres("Juan").build();
        Rol rolJefe = Rol.builder().codigo("ROLE_JEFE_OFICINA").build();
        Usuario usuario = Usuario.builder()
                .id(2L)
                .username("jefe.obras")
                .persona(persona)
                .roles(Set.of(rolJefe))
                .build();

        Oficina ofiObras = Oficina.builder().id(3L).nombre("Obras").build();
        PersonaOficina asignacion = PersonaOficina.builder()
                .oficina(ofiObras)
                .estado("ACTIVO")
                .build();

        when(personaOficinaRepository.findByPersonaIdAndEstado(10L, "ACTIVO"))
                .thenReturn(List.of(asignacion));

        // Solicita su propia oficina -> Permitido
        Long ofiPropia = ReporteFilterHelper.resolverOficinaPermitida(usuario, 3L, personaOficinaRepository);
        assertEquals(3L, ofiPropia);

        // Solicita null -> Forzado a su oficina propia
        Long ofiDefault = ReporteFilterHelper.resolverOficinaPermitida(usuario, null, personaOficinaRepository);
        assertEquals(3L, ofiDefault);

        // Solicita oficina 8 (Rentabilidad) -> AccessDeniedException
        assertThrows(AccessDeniedException.class, () ->
                ReporteFilterHelper.resolverOficinaPermitida(usuario, 8L, personaOficinaRepository)
        );
    }

    // ==================== 3. RESUMEN ESTADÍSTICO ====================
    @Test
    @DisplayName("Debe consolidar correctamente los estados y calcular el total de pendientes")
    void testResumenEstadisticoConsolidado() {
        ReporteMesaPartesServiceImpl service = new ReporteMesaPartesServiceImpl(
                expedienteRepository, usuarioRepository, personaOficinaRepository, exportacionRepository
        );

        List<Object[]> resultados = List.of(
                new Object[]{EstadoExpediente.PRESENTADO, 10L},
                new Object[]{EstadoExpediente.RECIBIDO, 5L},
                new Object[]{EstadoExpediente.EN_REVISION, 3L},
                new Object[]{EstadoExpediente.OBSERVADO, 2L},
                new Object[]{EstadoExpediente.EN_ATENCION, 4L},
                new Object[]{EstadoExpediente.RESUELTO, 8L},
                new Object[]{EstadoExpediente.NOTIFICADO, 2L},
                new Object[]{EstadoExpediente.ARCHIVADO, 1L}
        );

        when(expedienteRepository.countGroupByEstado(any(), any(), any())).thenReturn(resultados);

        FiltroReporteRequest filtro = FiltroReporteRequest.builder().periodo("MES_ACTUAL").build();
        ResumenEstadisticoDto resumen = service.obtenerResumen(filtro, null);

        assertNotNull(resumen);
        assertEquals(10L, resumen.getTotalPresentados());
        assertEquals(5L, resumen.getTotalRecibidos());
        assertEquals(3L, resumen.getTotalEnRevision());
        assertEquals(2L, resumen.getTotalObservados());
        assertEquals(4L, resumen.getTotalEnAtencion());
        assertEquals(8L, resumen.getTotalResueltos());
        assertEquals(2L, resumen.getTotalCerrados());
        assertEquals(1L, resumen.getTotalArchivados());
        // Pendientes = 10 + 5 + 3 + 2 + 4 = 24
        assertEquals(24L, resumen.getTotalPendientes());
    }

    // ==================== 4. INDICADORES DE EFICIENCIA Y MEDIANA ====================
    @Test
    @DisplayName("Debe calcular promedio, mediana y porcentajes de resolución con datos válidos")
    void testIndicadoresEficienciaConDatos() {
        IndicadoresMesaPartesServiceImpl service = new IndicadoresMesaPartesServiceImpl(
                expedienteRepository, derivacionRepository, reporteService, plazoService
        );

        OffsetDateTime ahora = OffsetDateTime.now();
        Expediente e1 = Expediente.builder()
                .estadoActual(EstadoExpediente.RESUELTO)
                .fechaPresentacion(ahora.minusDays(10))
                .fechaCierre(ahora.minusDays(6)) // 4 días
                .build();
        Expediente e2 = Expediente.builder()
                .estadoActual(EstadoExpediente.RESUELTO)
                .fechaPresentacion(ahora.minusDays(20))
                .fechaCierre(ahora.minusDays(10)) // 10 días
                .build();
        Expediente e3 = Expediente.builder()
                .estadoActual(EstadoExpediente.RESUELTO)
                .fechaPresentacion(ahora.minusDays(15))
                .fechaCierre(ahora.minusDays(9)) // 6 días
                .build();
        Expediente e4 = Expediente.builder()
                .estadoActual(EstadoExpediente.OBSERVADO)
                .fechaPresentacion(ahora.minusDays(5))
                .build();

        when(reporteService.resolverOficinaEfectiva(any(), any())).thenReturn(null);
        when(expedienteRepository.findParaIndicadores(any(), any(), any())).thenReturn(List.of(e1, e2, e3, e4));
        when(derivacionRepository.findRecibidasConTiempos(any(), any(), any())).thenReturn(Collections.emptyList());

        IndicadoresEficienciaDto ef = service.calcularIndicadoresEficiencia(new FiltroReporteRequest(), "admin");

        assertTrue(ef.isDatosSuficientes());
        assertEquals(3, ef.getTotalAtendidosPeriodo());
        // Promedio de 4, 10, 6 = 20 / 3 = 6.67 días
        assertNotNull(ef.getTiempoPromedioAtencionDias());
        assertEquals(6.67, ef.getTiempoPromedioAtencionDias(), 0.05);
        // Mediana de {4, 6, 10} = 6 días
        assertEquals(6.0, ef.getTiempoMedianoAtencionDias());
        // Tasas: total 4 expedientes. 3 resueltos = 75%, 1 observado = 25%
        assertEquals(75.0, ef.getPorcentajeResueltos());
        assertEquals(25.0, ef.getPorcentajeObservados());
    }

    @Test
    @DisplayName("Debe manejar lista vacía en indicadores sin divisiones por cero ni errores")
    void testIndicadoresEficienciaSinDatos() {
        IndicadoresMesaPartesServiceImpl service = new IndicadoresMesaPartesServiceImpl(
                expedienteRepository, derivacionRepository, reporteService, plazoService
        );

        when(reporteService.resolverOficinaEfectiva(any(), any())).thenReturn(null);
        when(expedienteRepository.findParaIndicadores(any(), any(), any())).thenReturn(Collections.emptyList());

        IndicadoresEficienciaDto ef = service.calcularIndicadoresEficiencia(new FiltroReporteRequest(), "admin");

        assertFalse(ef.isDatosSuficientes());
        assertEquals(0, ef.getTotalAtendidosPeriodo());
        assertEquals(0.0, ef.getPorcentajeResueltos());
        assertNull(ef.getTiempoPromedioAtencionDias());
    }

    // ==================== 5. INDICADORES DE PLAZOS LEGALES ====================
    @Test
    @DisplayName("Debe clasificar expedientes en plazo, próximos a vencer, vencidos y suspendidos")
    void testIndicadoresPlazos() {
        IndicadoresMesaPartesServiceImpl service = new IndicadoresMesaPartesServiceImpl(
                expedienteRepository, derivacionRepository, reporteService, plazoService
        );

        OffsetDateTime ahora = OffsetDateTime.now();
        TipoTramite tramiteConPlazo = TipoTramite.builder()
                .plazoDias(30)
                .unidadPlazo(UnidadPlazo.DIAS_HABILES)
                .build();

        // 1. En plazo (faltan 15 días)
        Expediente expEnPlazo = Expediente.builder()
                .estadoActual(EstadoExpediente.EN_ATENCION)
                .tipoTramite(tramiteConPlazo)
                .fechaLimite(ahora.plusDays(15))
                .build();

        // 2. Próximo a vencer (faltan 2 días)
        Expediente expProximo = Expediente.builder()
                .estadoActual(EstadoExpediente.EN_ATENCION)
                .tipoTramite(tramiteConPlazo)
                .fechaLimite(ahora.plusDays(2))
                .build();

        // 3. Vencido (venció hace 3 días)
        Expediente expVencido = Expediente.builder()
                .estadoActual(EstadoExpediente.EN_ATENCION)
                .tipoTramite(tramiteConPlazo)
                .fechaLimite(ahora.minusDays(3))
                .build();

        // 4. Suspendido (Observado)
        Expediente expSuspendido = Expediente.builder()
                .estadoActual(EstadoExpediente.OBSERVADO)
                .tipoTramite(tramiteConPlazo)
                .build();

        // 5. Sin plazo configurado
        Expediente expSinPlazo = Expediente.builder()
                .estadoActual(EstadoExpediente.RECIBIDO)
                .tipoTramite(null)
                .build();

        when(reporteService.resolverOficinaEfectiva(any(), any())).thenReturn(null);
        when(expedienteRepository.findParaIndicadores(any(), any(), any()))
                .thenReturn(List.of(expEnPlazo, expProximo, expVencido, expSuspendido, expSinPlazo));

        IndicadoresPlazosDto p = service.calcularIndicadoresPlazos(new FiltroReporteRequest(), "admin");

        assertEquals(1, p.getEnPlazo());
        assertEquals(1, p.getProximosVencer());
        assertEquals(1, p.getVencidos());
        assertEquals(1, p.getSuspendidos());
        assertEquals(1, p.getSinPlazoConfigurado());
        assertEquals(4, p.getTotalConPlazo());
    }

    // ==================== 6. COMPARATIVA DE PERÍODOS (DIVISIÓN SEGURA) ====================
    @Test
    @DisplayName("Debe calcular variación porcentual manejando períodos base cero sin NaN ni Infinity")
    void testComparacionPeriodosDivisionSegura() {
        EstadisticasExpedienteServiceImpl service = new EstadisticasExpedienteServiceImpl(
                expedienteRepository, reporteService
        );

        when(reporteService.resolverOficinaEfectiva(any(), any())).thenReturn(null);
        // Presentados actual: 15, anterior: 0 (Nuevo)
        when(expedienteRepository.countPresentadosEnRango(any(), any(), any()))
                .thenReturn(15L) // actual
                .thenReturn(0L); // anterior
        // Resueltos actual: 10, anterior: 5 (+100%)
        when(expedienteRepository.countResueltosEnRango(any(), any(), any()))
                .thenReturn(10L) // actual
                .thenReturn(5L); // anterior
        // Observados actual: 0, anterior: 0 (0.0%)
        when(expedienteRepository.countObservadosEnRango(any(), any(), any()))
                .thenReturn(0L) // actual
                .thenReturn(0L); // anterior

        ComparacionPeriodoDto comp = service.compararPeriodos("MES", null, "admin");

        assertEquals("+100.0% (Base 0)", comp.getVariacionPctPresentados());
        assertEquals("+100.0%", comp.getVariacionPctResueltos());
        assertEquals("0.0%", comp.getVariacionPctObservados());
    }

    // ==================== 7. SUPERVISIÓN DE DERIVACIONES ====================
    @Test
    @DisplayName("Debe calcular métricas de derivaciones distinguiendo total de expedientes únicos")
    void testSupervisionDerivaciones() {
        SupervisionMesaPartesServiceImpl service = new SupervisionMesaPartesServiceImpl(
                derivacionRepository, observacionRepository, subsanacionRepository,
                actuacionRepository, asignacionRepository, expedienteRepository,
                oficinaRepository, reporteService
        );

        when(reporteService.resolverOficinaEfectiva(any(), any())).thenReturn(1L);
        when(derivacionRepository.countEnviadas(any(), any(), any())).thenReturn(12L);
        when(derivacionRepository.countRecibidas(any(), any(), any())).thenReturn(10L);
        when(derivacionRepository.countExpedientesUnicosDerivados(any(), any(), any())).thenReturn(7L);
        when(derivacionRepository.countGroupByEstado(any(), any(), any())).thenReturn(List.of(
                new Object[]{EstadoDerivacion.PENDIENTE, 3L},
                new Object[]{EstadoDerivacion.RECHAZADA, 1L},
                new Object[]{EstadoDerivacion.DEVUELTA, 1L}
        ));
        when(derivacionRepository.findRecibidasConTiempos(any(), any(), any())).thenReturn(Collections.emptyList());

        AnalisisDerivacionesDto d = service.analizarDerivaciones(new FiltroReporteRequest(), "admin");

        assertEquals(12, d.getEnviadas());
        assertEquals(10, d.getRecibidas());
        assertEquals(7, d.getExpedientesUnicos(), "Los expedientes únicos derivados deben diferir del total de derivaciones");
        assertEquals(3, d.getPendientes());
        assertEquals(1, d.getRechazadas());
        assertEquals(1, d.getDevueltas());
    }

    // ==================== 8. SUPERVISIÓN DE OBSERVACIONES Y SUBSANACIONES ====================
    @Test
    @DisplayName("Debe calcular observaciones y subsanaciones pendientes correctamente")
    void testSupervisionObservaciones() {
        SupervisionMesaPartesServiceImpl service = new SupervisionMesaPartesServiceImpl(
                derivacionRepository, observacionRepository, subsanacionRepository,
                actuacionRepository, asignacionRepository, expedienteRepository,
                oficinaRepository, reporteService
        );

        when(reporteService.resolverOficinaEfectiva(any(), any())).thenReturn(null);
        when(observacionRepository.countExpedientesObservados(any(), any(), any())).thenReturn(5L);
        when(observacionRepository.countTotalObservaciones(any(), any(), any())).thenReturn(8L);
        when(subsanacionRepository.countSubsanaciones(any(), any(), any())).thenReturn(6L);

        AnalisisObservacionesDto o = service.analizarObservaciones(new FiltroReporteRequest(), "admin");

        assertEquals(5, o.getExpedientesObservados());
        assertEquals(8, o.getObservacionesTotales());
        assertEquals(6, o.getSubsanacionesPresentadas());
        assertEquals(2, o.getSubsanacionesPendientes(), "Pendientes = 8 - 6 = 2");
    }

    // ==================== 9. PRODUCTIVIDAD OPERATIVA AGREGADA ====================
    @Test
    @DisplayName("Debe agregar carga de trabajo operativa sin evaluaciones de desempeño punitivas individuales")
    void testProductividadOperativaAgregada() {
        SupervisionMesaPartesServiceImpl service = new SupervisionMesaPartesServiceImpl(
                derivacionRepository, observacionRepository, subsanacionRepository,
                actuacionRepository, asignacionRepository, expedienteRepository,
                oficinaRepository, reporteService
        );

        Usuario usuario1 = Usuario.builder().id(1L).username("operador1").build();
        Oficina ofi1 = Oficina.builder().id(1L).nombre("Rentas").build();

        Expediente e1 = Expediente.builder()
                .responsableActual(usuario1)
                .oficinaResponsable(ofi1)
                .estadoActual(EstadoExpediente.EN_ATENCION)
                .build();
        Expediente e2 = Expediente.builder()
                .oficinaResponsable(ofi1)
                .estadoActual(EstadoExpediente.RESUELTO)
                .build();

        when(reporteService.resolverOficinaEfectiva(any(), any())).thenReturn(null);
        when(expedienteRepository.findParaIndicadores(any(), any(), any())).thenReturn(List.of(e1, e2));
        when(actuacionRepository.countActuaciones(any(), any(), any())).thenReturn(14L);
        when(oficinaRepository.findByEstado("ACTIVO")).thenReturn(List.of(ofi1));

        ProductividadOperativaDto p = service.obtenerProductividadOperativa(new FiltroReporteRequest(), "admin");

        assertEquals(1, p.getExpedientesAsignados());
        assertEquals(1, p.getExpedientesEnAtencion());
        assertEquals(14, p.getActuacionesRegistradas());
        assertEquals(1, p.getExpedientesFinalizados());
        assertFalse(p.getOficinasPendientes().isEmpty());
    }

    // ==================== 10. GENERACIÓN DE EVOLUCIÓN MENSUAL ====================
    @Test
    @DisplayName("Debe generar exactamente 12 meses para la evolución cronológica del año")
    void testEvolucionMensual12Meses() {
        EstadisticasExpedienteServiceImpl service = new EstadisticasExpedienteServiceImpl(
                expedienteRepository, reporteService
        );

        when(reporteService.resolverOficinaEfectiva(any(), any())).thenReturn(null);
        when(expedienteRepository.countPresentadosEnRango(any(), any(), any())).thenReturn(5L);
        when(expedienteRepository.countResueltosEnRango(any(), any(), any())).thenReturn(3L);

        EvolucionMensualDto evo = service.obtenerEvolucionMensual(2026, null, "admin");

        assertNotNull(evo);
        assertEquals(2026, evo.getAnio());
        assertEquals(12, evo.getMeses().size());
        assertEquals(12, evo.getPresentados().size());
        assertEquals(12, evo.getResueltos().size());
    }
}
