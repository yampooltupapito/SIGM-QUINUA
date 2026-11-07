package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento.CuotaSimuladaDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento.SimulacionFraccionamientoRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento.SimulacionFraccionamientoResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ParametroFraccionamientoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ProgramaFraccionamientoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoProgramaFraccionamiento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ObligacionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ParametroFraccionamientoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ProgramaFraccionamientoTributarioRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimulacionFraccionamientoTest {

    @Mock
    private ProgramaFraccionamientoTributarioRepository programaRepository;

    @Mock
    private ParametroFraccionamientoTributarioRepository parametroRepository;

    @Mock
    private ObligacionTributariaRepository obligacionRepository;

    @InjectMocks
    private SimulacionFraccionamientoService simulacionService;

    private UUID programaId;
    private ProgramaFraccionamientoTributario programa;

    @BeforeEach
    void setUp() {
        programaId = UUID.randomUUID();
        programa = ProgramaFraccionamientoTributario.builder()
                .id(programaId)
                .codigo("PROG-FRAC-2026-01")
                .nombre("Programa General de Fraccionamiento 2026")
                .estado(EstadoProgramaFraccionamiento.ACTIVO)
                .fechaInicioVigencia(LocalDate.of(2026, 1, 1))
                .build();
    }

    @Test
    @DisplayName("Simulación matemática exitosa con cuota inicial y conciliación exacta de capital")
    void testSimulacionExitosaConciliacionExacta() {
        when(programaRepository.findById(programaId)).thenReturn(Optional.of(programa));

        // Parámetros normativos
        when(parametroRepository.findPrimerVigente(eq(programaId), eq("MONTO_MINIMO_ELEGIBLE"), any()))
                .thenReturn(Optional.of(ParametroFraccionamientoTributario.builder().valorNumerico(new BigDecimal("150.00")).build()));
        when(parametroRepository.findPrimerVigente(eq(programaId), eq("CUOTAS_MAXIMAS"), any()))
                .thenReturn(Optional.of(ParametroFraccionamientoTributario.builder().valorNumerico(new BigDecimal("24")).build()));
        when(parametroRepository.findPrimerVigente(eq(programaId), eq("CUOTAS_MINIMAS"), any()))
                .thenReturn(Optional.of(ParametroFraccionamientoTributario.builder().valorNumerico(new BigDecimal("2")).build()));
        when(parametroRepository.findPrimerVigente(eq(programaId), eq("PORCENTAJE_CUOTA_INICIAL_MIN"), any()))
                .thenReturn(Optional.of(ParametroFraccionamientoTributario.builder().valorNumerico(new BigDecimal("10.00")).build()));
        when(parametroRepository.findPrimerVigente(eq(programaId), eq("TASA_INTERES_MENSUAL"), any()))
                .thenReturn(Optional.of(ParametroFraccionamientoTributario.builder().valorNumerico(new BigDecimal("0.7200")).build()));
        when(parametroRepository.findPrimerVigente(eq(programaId), eq("PERIODICIDAD_DIAS"), any()))
                .thenReturn(Optional.of(ParametroFraccionamientoTributario.builder().valorNumerico(new BigDecimal("30")).build()));

        SimulacionFraccionamientoRequest req = SimulacionFraccionamientoRequest.builder()
                .programaId(programaId)
                .importeBase(new BigDecimal("1000.00"))
                .importeCuotaInicial(new BigDecimal("100.00"))
                .numeroCuotas(6)
                .fechaInicio(LocalDate.of(2026, 3, 1))
                .build();

        SimulacionFraccionamientoResponse res = simulacionService.simular(req);

        assertThat(res).isNotNull();
        assertThat(res.getImporteBase()).isEqualByComparingTo("1000.00");
        assertThat(res.getCuotaInicial()).isEqualByComparingTo("100.00");
        assertThat(res.getImporteFinanciado()).isEqualByComparingTo("900.00");
        assertThat(res.getNumeroCuotas()).isEqualTo(6);
        assertThat(res.getCuotas()).hasSize(6);

        // Conciliación: La suma de capitales de cada cuota debe ser exactamente igual al monto financiado (900.00)
        BigDecimal sumaCapitales = res.getCuotas().stream()
                .map(CuotaSimuladaDto::getCapital)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(sumaCapitales).isEqualByComparingTo("900.00");

        // El saldo restante de la última cuota debe ser exactamente 0.00
        CuotaSimuladaDto ultimaCuota = res.getCuotas().get(5);
        assertThat(ultimaCuota.getSaldoRestante()).isEqualByComparingTo("0.00");

        // Los intereses deben ser estrictamente positivos
        assertThat(res.getTotalIntereses()).isGreaterThan(BigDecimal.ZERO);
        assertThat(res.getImporteTotalFinanciado()).isEqualByComparingTo(res.getImporteFinanciado().add(res.getTotalIntereses()));
    }

    @Test
    @DisplayName("Bloqueo de simulación si el monto base es menor al mínimo normativo")
    void testBloqueoMontoInferiorAlMinimo() {
        when(programaRepository.findById(programaId)).thenReturn(Optional.of(programa));
        when(parametroRepository.findPrimerVigente(eq(programaId), eq("MONTO_MINIMO_ELEGIBLE"), any()))
                .thenReturn(Optional.of(ParametroFraccionamientoTributario.builder().valorNumerico(new BigDecimal("150.00")).build()));

        SimulacionFraccionamientoRequest req = SimulacionFraccionamientoRequest.builder()
                .programaId(programaId)
                .importeBase(new BigDecimal("80.00"))
                .numeroCuotas(3)
                .build();

        assertThatThrownBy(() -> simulacionService.simular(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no alcanza el monto mínimo normativo");
    }

    @Test
    @DisplayName("Bloqueo de simulación si las cuotas exceden el máximo legal permitido")
    void testBloqueoCuotasExcedenMaximo() {
        when(programaRepository.findById(programaId)).thenReturn(Optional.of(programa));
        when(parametroRepository.findPrimerVigente(eq(programaId), eq("MONTO_MINIMO_ELEGIBLE"), any()))
                .thenReturn(Optional.of(ParametroFraccionamientoTributario.builder().valorNumerico(new BigDecimal("150.00")).build()));
        when(parametroRepository.findPrimerVigente(eq(programaId), eq("CUOTAS_MAXIMAS"), any()))
                .thenReturn(Optional.of(ParametroFraccionamientoTributario.builder().valorNumerico(new BigDecimal("24")).build()));
        when(parametroRepository.findPrimerVigente(eq(programaId), eq("CUOTAS_MINIMAS"), any()))
                .thenReturn(Optional.of(ParametroFraccionamientoTributario.builder().valorNumerico(new BigDecimal("2")).build()));

        SimulacionFraccionamientoRequest req = SimulacionFraccionamientoRequest.builder()
                .programaId(programaId)
                .importeBase(new BigDecimal("500.00"))
                .numeroCuotas(36)
                .build();

        assertThatThrownBy(() -> simulacionService.simular(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("debe estar comprendido entre");
    }
}
