package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda.SimulacionInteresRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda.SimulacionInteresResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ParametroInteresTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoInteresTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.UnidadTemporalInteres;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ConceptoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.NormaTributariaConfiguradaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ObligacionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ParametroInteresTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl.InteresMoratorioServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InteresMoratorioTest {

    @Mock
    private ParametroInteresTributarioRepository parametroRepository;

    @Mock
    private ObligacionTributariaRepository obligacionRepository;

    @Mock
    private ConceptoTributarioRepository conceptoRepository;

    @Mock
    private NormaTributariaConfiguradaRepository normaRepository;

    @InjectMocks
    private InteresMoratorioServiceImpl interesService;

    private ParametroInteresTributario paramTim2026;

    @BeforeEach
    void setUp() {
        paramTim2026 = ParametroInteresTributario.builder()
                .id(UUID.randomUUID())
                .codigo("TIM-2026-SUNAT")
                .nombre("Tasa de Interés Moratorio Mensual 2026 (0.90%)")
                .tipoInteres(TipoInteresTributario.TIM_MENSUAL)
                .tasa(new BigDecimal("0.9000"))
                .unidadTemporal(UnidadTemporalInteres.MENSUAL)
                .fechaInicioVigencia(LocalDate.of(2026, 1, 1))
                .baseLegal("R.S. 044-2021/SUNAT y Art. 33 TUO Código Tributario")
                .estado(true)
                .build();
    }

    @Test
    @DisplayName("Cálculo TIM mensual exacto: Capital S/ 100.00, Tasa 0.90% mensual, 30 días = Interés S/ 0.90")
    void testCalculoTimTreintaDias() {
        when(parametroRepository.findVigenteParaFechaYConcepto(any(), any()))
                .thenReturn(List.of(paramTim2026));

        LocalDate vencimiento = LocalDate.of(2026, 1, 1);
        LocalDate corte = LocalDate.of(2026, 1, 31); // 30 días de morosidad

        SimulacionInteresRequest req = SimulacionInteresRequest.builder()
                .montoCapital(new BigDecimal("100.00"))
                .fechaVencimiento(vencimiento)
                .fechaCorte(corte)
                .build();

        SimulacionInteresResponse response = interesService.simularInteresGenerico(req);

        assertThat(response.isEsAptoParaCalculo()).isTrue();
        assertThat(response.getDiasMorosidad()).isEqualTo(30);
        // Factor diario = (0.90 / 100) / 30 = 0.00030000
        assertThat(response.getFactorDiario()).isEqualByComparingTo(new BigDecimal("0.00030000"));
        // Interés = 100.00 * 0.00030000 * 30 = 0.90
        assertThat(response.getMontoInteresCalculado()).isEqualByComparingTo(new BigDecimal("0.90"));
        assertThat(response.getMontoTotalSimulado()).isEqualByComparingTo(new BigDecimal("100.90"));
    }

    @Test
    @DisplayName("Obligación no vencida: días de morosidad 0, interés S/ 0.00")
    void testObligacionNoVencida() {
        LocalDate vencimiento = LocalDate.of(2026, 3, 31);
        LocalDate corte = LocalDate.of(2026, 2, 15); // Antes del vencimiento

        SimulacionInteresRequest req = SimulacionInteresRequest.builder()
                .montoCapital(new BigDecimal("250.00"))
                .fechaVencimiento(vencimiento)
                .fechaCorte(corte)
                .build();

        SimulacionInteresResponse response = interesService.simularInteresGenerico(req);

        assertThat(response.getDiasMorosidad()).isEqualTo(0);
        assertThat(response.getMontoInteresCalculado()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getMontoTotalSimulado()).isEqualByComparingTo(new BigDecimal("250.00"));
    }

    @Test
    @DisplayName("Obligación sin saldo pendiente: interés S/ 0.00")
    void testObligacionSaldoCero() {
        LocalDate vencimiento = LocalDate.of(2026, 1, 1);
        LocalDate corte = LocalDate.of(2026, 3, 1);

        SimulacionInteresRequest req = SimulacionInteresRequest.builder()
                .montoCapital(BigDecimal.ZERO)
                .fechaVencimiento(vencimiento)
                .fechaCorte(corte)
                .build();

        SimulacionInteresResponse response = interesService.simularInteresGenerico(req);

        assertThat(response.getMontoInteresCalculado()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getMontoTotalSimulado()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Ausencia de parámetro normativo vigente: bloquea cálculo y genera alerta explicativa")
    void testSinParametroVigente() {
        when(parametroRepository.findVigenteParaFechaYConcepto(any(), any()))
                .thenReturn(Collections.emptyList());

        LocalDate vencimiento = LocalDate.of(2025, 1, 1);
        LocalDate corte = LocalDate.of(2025, 6, 1);

        SimulacionInteresRequest req = SimulacionInteresRequest.builder()
                .montoCapital(new BigDecimal("500.00"))
                .fechaVencimiento(vencimiento)
                .fechaCorte(corte)
                .build();

        SimulacionInteresResponse response = interesService.simularInteresGenerico(req);

        assertThat(response.isEsAptoParaCalculo()).isFalse();
        assertThat(response.getMensajeError()).contains("No existe una tasa de interés moratorio");
        assertThat(response.getMontoInteresCalculado()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Cálculo para Obligación existente desde BD")
    void testCalculoParaObligacionBd() {
        UUID obId = UUID.randomUUID();
        ObligacionTributaria ob = ObligacionTributaria.builder()
                .id(obId)
                .codigoObligacion("OBL-2026-0005")
                .saldo(new BigDecimal("200.00"))
                .fechaVencimiento(LocalDate.of(2026, 1, 1))
                .moneda("PEN")
                .build();

        when(obligacionRepository.findById(obId)).thenReturn(Optional.of(ob));
        when(parametroRepository.findVigenteParaFechaYConcepto(any(), any()))
                .thenReturn(List.of(paramTim2026));

        LocalDate corte = LocalDate.of(2026, 1, 16); // 15 días de mora
        SimulacionInteresResponse response = interesService.simularInteresObligacion(obId, corte);

        assertThat(response.isEsAptoParaCalculo()).isTrue();
        assertThat(response.getDiasMorosidad()).isEqualTo(15);
        // 200.00 * 0.00030000 * 15 = 0.90
        assertThat(response.getMontoInteresCalculado()).isEqualByComparingTo(new BigDecimal("0.90"));
        assertThat(response.getMontoTotalSimulado()).isEqualByComparingTo(new BigDecimal("200.90"));
    }
}
