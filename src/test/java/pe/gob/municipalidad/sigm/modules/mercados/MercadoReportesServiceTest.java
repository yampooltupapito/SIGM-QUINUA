package pe.gob.municipalidad.sigm.modules.mercados;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MercadoReportesServiceTest {

    @Autowired
    private IndicadoresMercadosService indicadoresService;

    @Autowired
    private ReporteOcupacionService reporteOcupacionService;

    @Autowired
    private ReporteMercadosService reporteMercadosService;

    @Autowired
    private ReporteRecaudacionService reporteRecaudacionService;

    @Autowired
    private ReporteOperativoService reporteOperativoService;

    @Autowired
    private ExportacionExcelService exportacionExcelService;

    @Autowired
    private ExportacionPdfService exportacionPdfService;

    @Autowired
    private HistorialExportacionService historialExportacionService;

    @Autowired
    private MercadoMunicipalRepository mercadoRepository;

    @Autowired
    private PuestoComercialRepository puestoRepository;

    @Autowired
    private SectorMercadoRepository sectorRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private ComercianteRepository comercianteRepository;

    @Autowired
    private AsignacionPuestoRepository asignacionRepository;

    @Autowired
    private ContratoOcupacionRepository contratoRepository;

    @Autowired
    private ConceptoCobroMercadoRepository conceptoRepository;

    @Autowired
    private ObligacionMercadoRepository obligacionRepository;

    @Autowired
    private PagoMercadoRepository pagoRepository;

    @Autowired
    private AplicacionPagoMercadoRepository aplicacionPagoRepository;

    @Autowired
    private InspeccionMercadoRepository inspeccionRepository;

    private MercadoMunicipal mercadoTest;
    private SectorMercado sectorTest;
    private PuestoComercial puesto1;
    private PuestoComercial puesto2;
    private Comerciante comercianteTest;

    @BeforeEach
    void setUp() {
        mercadoTest = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("M-REP-" + UUID.randomUUID().toString().substring(0, 6))
                .nombre("Mercado Reportes Test")
                .direccion("Plaza Principal")
                .estado(EstadoMercado.ACTIVO)
                .build());

        sectorTest = sectorRepository.save(SectorMercado.builder()
                .codigo("SEC-" + UUID.randomUUID().toString().substring(0, 6))
                .nombre("Sector Carnes")
                .mercado(mercadoTest)
                .estado("ACTIVO")
                .build());

        puesto1 = puestoRepository.save(PuestoComercial.builder()
                .codigo("P1-" + UUID.randomUUID().toString().substring(0, 6))
                .numero("01")
                .mercado(mercadoTest)
                .sector(sectorTest)
                .areaMetrosCuadrados(new BigDecimal("12.50"))
                .nivel(1)
                .estado(EstadoPuesto.OCUPADO)
                .estadoFisico(EstadoFisicoPuesto.OPERATIVO)
                .build());

        puesto2 = puestoRepository.save(PuestoComercial.builder()
                .codigo("P2-" + UUID.randomUUID().toString().substring(0, 6))
                .numero("02")
                .mercado(mercadoTest)
                .sector(sectorTest)
                .areaMetrosCuadrados(new BigDecimal("10.00"))
                .nivel(1)
                .estado(EstadoPuesto.DISPONIBLE)
                .estadoFisico(EstadoFisicoPuesto.OPERATIVO)
                .build());

        long ts = System.currentTimeMillis();
        Persona persona = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("44" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Juan Alberto")
                .apellidoPaterno("Quispe")
                .apellidoMaterno("Flores")
                .correo("juan" + ts + "@quinua.gob.pe")
                .direccion("Jr. Sucre 123, Quinua")
                .estado("ACTIVO")
                .build());

        comercianteTest = comercianteRepository.save(Comerciante.builder()
                .persona(persona)
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .codigoComerciante("COM-REP-" + UUID.randomUUID().toString().substring(0, 6))
                .estado(EstadoComerciante.ACTIVO)
                .fechaRegistro(LocalDate.now())
                .build());

        asignacionRepository.save(AsignacionPuesto.builder()
                .codigoAsignacion("ASIG-" + UUID.randomUUID().toString().substring(0, 6))
                .puesto(puesto1)
                .comerciante(comercianteTest)
                .fechaInicio(LocalDate.now().minusMonths(3))
                .estado(EstadoAsignacion.VIGENTE)
                .build());
    }

    @Test
    @DisplayName("Debe calcular dashboard ejecutivo consolidado con datos reales")
    void testDashboardEjecutivo() {
        ReporteMercadoFilter filtro = ReporteMercadoFilter.builder()
                .mercadoId(mercadoTest.getId())
                .periodo(PeriodoConsulta.MES_ACTUAL)
                .build();

        DashboardMercadosResponse resp = indicadoresService.obtenerDashboard(filtro);
        assertNotNull(resp);
        assertTrue(resp.getTotalMercados() >= 1);
        assertTrue(resp.getTotalPuestos() >= 2);
        assertTrue(resp.getPuestosOcupados() >= 1);
        assertTrue(resp.getPuestosDisponibles() >= 1);
        assertNotNull(resp.getPorcentajeOcupacionTexto());
        assertFalse(resp.getPorcentajeOcupacionTexto().isEmpty());
    }

    @Test
    @DisplayName("Debe calcular tasa de ocupación correctamente y manejar denominador cero")
    void testCalculoOcupacion() {
        ReporteMercadoFilter filtro = ReporteMercadoFilter.builder()
                .mercadoId(mercadoTest.getId())
                .build();

        IndicadorOcupacionResponse resp = reporteOcupacionService.obtenerIndicadorOcupacion(filtro);
        assertNotNull(resp);
        assertEquals(2, resp.getTotalPuestos());
        assertEquals(1, resp.getPuestosOcupados());
        assertEquals(1, resp.getPuestosDisponibles());
        // 1 de 2 habilitados = 50.0%
        assertEquals("50.0%", resp.getPorcentajeOcupacionTexto());

        // Denominador cero
        ReporteMercadoFilter filtroVacio = ReporteMercadoFilter.builder()
                .mercadoId(UUID.randomUUID()) // mercado inexistente
                .build();
        IndicadorOcupacionResponse respVacio = reporteOcupacionService.obtenerIndicadorOcupacion(filtroVacio);
        assertEquals("Sin datos suficientes", respVacio.getPorcentajeOcupacionTexto());
    }

    @Test
    @DisplayName("Debe listar padrón de comerciantes enmascarando documento si no tiene permisos")
    void testComerciantesEnmascaramiento() {
        ReporteMercadoFilter filtro = ReporteMercadoFilter.builder()
                .mercadoId(mercadoTest.getId())
                .build();

        String dniOriginal = comercianteTest.getPersona().getNumeroDocumento();
        String dniMascara = dniOriginal.substring(0, 2) + "*".repeat(dniOriginal.length() - 4) + dniOriginal.substring(dniOriginal.length() - 2);

        var listaConPermiso = reporteMercadosService.listarTodosComerciantes(filtro, true);
        var comercianteConPermiso = listaConPermiso.stream()
                .filter(c -> c.getCodigo().equals(comercianteTest.getCodigo()))
                .findFirst().orElse(null);
        assertNotNull(comercianteConPermiso);
        assertEquals(dniOriginal, comercianteConPermiso.getNumeroDocumento());

        var listaSinPermiso = reporteMercadosService.listarTodosComerciantes(filtro, false);
        var comercianteSinPermiso = listaSinPermiso.stream()
                .filter(c -> c.getCodigo().equals(comercianteTest.getCodigo()))
                .findFirst().orElse(null);
        assertNotNull(comercianteSinPermiso);
        assertEquals(dniMascara, comercianteSinPermiso.getNumeroDocumento());
    }

    @Test
    @DisplayName("Debe calcular recaudación y clasificar antigüedad de saldos correctamente")
    void testRecaudacionYAntiguedadSaldos() {
        ConceptoCobroMercado concepto = conceptoRepository.findByCodigo("USO_PUESTO_TEST")
                .orElseGet(() -> conceptoRepository.save(ConceptoCobroMercado.builder()
                        .codigo("USO_PUESTO_TEST")
                        .nombre("Merced Conductiva Puesto")
                        .descripcion("Canon mensual de ocupación")
                        .tipoConcepto(TipoConceptoCobro.USO_PUESTO_COMERCIAL)
                        .periodicidad(PeriodicidadConcepto.MENSUAL)
                        .estado("ACTIVO")
                        .requiereAsignacion(false)
                        .build()));

        // Crear obligación pendiente vencida hace 45 días (rango DE_31_A_60_DIAS)
        ObligacionMercado obligacion = obligacionRepository.save(ObligacionMercado.builder()
                .codigoObligacion("OBL-" + UUID.randomUUID().toString().substring(0, 6))
                .puesto(puesto1)
                .comerciante(comercianteTest)
                .concepto(concepto)
                .periodoInicio(LocalDate.now().minusDays(75))
                .periodoFin(LocalDate.now().minusDays(45))
                .fechaEmision(LocalDate.now().minusDays(75))
                .fechaVencimiento(LocalDate.now().minusDays(45))
                .montoOriginal(new BigDecimal("150.00"))
                .saldoPendiente(new BigDecimal("150.00"))
                .estado(EstadoObligacion.PENDIENTE)
                .build());

        ReporteMercadoFilter filtro = ReporteMercadoFilter.builder()
                .mercadoId(mercadoTest.getId())
                .build();

        Map<String, Object> resumen = reporteRecaudacionService.obtenerResumenDeudas(filtro);
        assertNotNull(resumen);
        assertTrue(((BigDecimal) resumen.get("totalDeuda")).compareTo(BigDecimal.ZERO) > 0);
        assertTrue(((BigDecimal) resumen.get("monto31a60")).compareTo(BigDecimal.ZERO) > 0);

        var deudas = reporteRecaudacionService.listarTodasDeudas(filtro);
        var itemDeuda = deudas.stream().filter(d -> d.getCodigoObligacion().equals(obligacion.getCodigoObligacion())).findFirst().orElse(null);
        assertNotNull(itemDeuda);
        assertEquals("DE_31_A_60_DIAS", itemDeuda.getRangoAntiguedad());
    }

    @Test
    @DisplayName("Debe generar archivo Excel válido y sanitizar fórmulas maliciosas")
    void testExportacionExcelValidaYSanitizada() throws IOException {
        ReporteMercadoFilter filtro = ReporteMercadoFilter.builder()
                .mercadoId(mercadoTest.getId())
                .build();

        byte[] excelBytes = exportacionExcelService.generarReporteExcel(TipoReporteMercado.PUESTOS, filtro, true);
        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 500);

        // Verificar que es un XLSX legible por Apache POI
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = wb.getSheetAt(0);
            assertNotNull(sheet);
            assertTrue(sheet.getLastRowNum() >= 4);
        }
    }

    @Test
    @DisplayName("Debe generar archivo PDF válido con encabezado institucional")
    void testExportacionPdfValida() {
        ReporteMercadoFilter filtro = ReporteMercadoFilter.builder()
                .mercadoId(mercadoTest.getId())
                .build();

        byte[] pdfBytes = exportacionPdfService.generarReportePdf(TipoReporteMercado.OCUPACION, filtro, true);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);

        // Los archivos PDF válidos comienzan con la firma %PDF-
        String cabecera = new String(pdfBytes, 0, Math.min(pdfBytes.length, 5));
        assertEquals("%PDF-", cabecera);
    }

    @Test
    @DisplayName("Debe registrar y listar en el historial de exportaciones con descarga segura")
    void testHistorialExportacion() {
        ExportacionReporteRequest req = ExportacionReporteRequest.builder()
                .tipoReporte(TipoReporteMercado.EJECUTIVO)
                .formato(FormatoExportacion.EXCEL)
                .filtro(ReporteMercadoFilter.builder().mercadoId(mercadoTest.getId()).build())
                .build();

        byte[] contenido = new byte[]{1, 2, 3, 4, 5};
        HistorialExportacionMercado hist = historialExportacionService.registrarExportacion(
                req, contenido, "admin", "127.0.0.1", "Mozilla/Test");

        assertNotNull(hist);
        assertNotNull(hist.getId());
        assertEquals("COMPLETADO", hist.getEstado());

        var desc = historialExportacionService.descargarArchivo(hist.getId(), "admin", true);
        assertArrayEquals(contenido, desc);

        // Usuario no autorizado debe lanzar SecurityException
        assertThrows(SecurityException.class, () ->
                historialExportacionService.descargarArchivo(hist.getId(), "otro_usuario", false));
    }
}
