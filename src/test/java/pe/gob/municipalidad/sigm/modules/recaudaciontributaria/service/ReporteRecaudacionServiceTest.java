package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.ReporteFilterRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.ReporteTributarioDtos.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoAplicacionPago;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MedioPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.AplicacionPagoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.CajaMunicipalRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PagoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.SesionCajaMunicipalRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReporteRecaudacionServiceTest {

    @Mock
    private PagoTributarioRepository pagoRepository;

    @Mock
    private AplicacionPagoTributarioRepository aplicacionRepository;

    @Mock
    private CajaMunicipalRepository cajaRepository;

    @Mock
    private SesionCajaMunicipalRepository sesionCajaRepository;

    private ReporteRecaudacionService reporteRecaudacionService;
    private ExportacionTributariaService exportacionTributariaService;

    @BeforeEach
    void setUp() {
        reporteRecaudacionService = new ReporteRecaudacionService(
                pagoRepository, aplicacionRepository, cajaRepository, sesionCajaRepository);
        exportacionTributariaService = new ExportacionTributariaService(
                null, null, null, null);
    }

    @Test
    @DisplayName("Debe generar 12 periodos mensuales para el ejercicio consultado")
    void testObtenerRecaudacionMensualEjercicio() {
        when(pagoRepository.calcularRecaudacionPeriodo(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(new BigDecimal("1500.50"));

        List<RecaudacionPeriodoItemDto> resultado = reporteRecaudacionService.obtenerRecaudacionMensualEjercicio(2026);

        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(12);
        assertThat(resultado.get(0).getMonto()).isEqualByComparingTo("1500.50");
    }

    @Test
    @DisplayName("Debe calcular recaudación por concepto excluyendo pagos no confirmados")
    void testObtenerRecaudacionPorConcepto_ExcluyeNoConfirmados() {
        ConceptoTributario predial = ConceptoTributario.builder()
                .id(UUID.randomUUID())
                .codigo("PREDIAL")
                .nombre("Impuesto Predial")
                .build();

        ObligacionTributaria obligacion = ObligacionTributaria.builder()
                .concepto(predial)
                .build();

        PagoTributario pagoConfirmado = PagoTributario.builder()
                .estado(EstadoPagoTributario.CONFIRMADO)
                .build();

        PagoTributario pagoAnulado = PagoTributario.builder()
                .estado(EstadoPagoTributario.ANULADO)
                .build();

        AplicacionPagoTributario appValida = AplicacionPagoTributario.builder()
                .pago(pagoConfirmado)
                .obligacion(obligacion)
                .importeAplicado(new BigDecimal("200.00"))
                .build();

        AplicacionPagoTributario appInvalida = AplicacionPagoTributario.builder()
                .pago(pagoAnulado)
                .obligacion(obligacion)
                .importeAplicado(new BigDecimal("500.00"))
                .build();

        when(aplicacionRepository.findByEstado(EstadoAplicacionPago.ACTIVA))
                .thenReturn(List.of(appValida, appInvalida));

        List<RecaudacionConceptoItemDto> conceptos = reporteRecaudacionService.obtenerRecaudacionPorConcepto(new ReporteFilterRequest());

        assertThat(conceptos).hasSize(1);
        assertThat(conceptos.get(0).getConceptoCodigo()).isEqualTo("PREDIAL");
        assertThat(conceptos.get(0).getMontoRecaudado()).isEqualByComparingTo("200.00");
        assertThat(conceptos.get(0).getPorcentaje()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Debe calcular recaudación por medios de pago con división por cero segura")
    void testObtenerRecaudacionPorMedioPago_DivisionPorCeroSegura() {
        when(pagoRepository.findByEstadoIn(any()))
                .thenReturn(List.of());

        List<RecaudacionMedioItemDto> medios = reporteRecaudacionService.obtenerRecaudacionPorMedioPago(new ReporteFilterRequest());

        assertThat(medios).isEmpty();
    }

    @Test
    @DisplayName("Debe sanitizar cadenas de texto contra inyección de fórmulas CSV/Excel")
    void testSanitizarFormulaAntiInyeccion() {
        assertThat(exportacionTributariaService.sanitizarFormula("=SUM(A1:A10)")).isEqualTo("'=SUM(A1:A10)");
        assertThat(exportacionTributariaService.sanitizarFormula("+12345")).isEqualTo("'+12345");
        assertThat(exportacionTributariaService.sanitizarFormula("-cmd|' /C calc'!A0")).isEqualTo("'-cmd|' /C calc'!A0");
        assertThat(exportacionTributariaService.sanitizarFormula("@cmd")).isEqualTo("'@cmd");
        assertThat(exportacionTributariaService.sanitizarFormula("Texto Legítimo Quinua")).isEqualTo("Texto Legítimo Quinua");
        assertThat(exportacionTributariaService.sanitizarFormula(null)).isEqualTo("");
    }
}
