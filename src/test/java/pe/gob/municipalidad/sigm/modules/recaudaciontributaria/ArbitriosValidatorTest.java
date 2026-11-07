package pe.gob.municipalidad.sigm.modules.recaudaciontributaria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PeriodoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PredioTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPeriodoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoPredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validation.ArbitrioValidator;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ArbitriosValidatorTest {

    private final ArbitrioValidator validator = new ArbitrioValidator(null, null, null, null, null);

    @Test
    @DisplayName("1. Validación Predio: Rechaza predios nulos o inactivos")
    void testValidarPredioInactivo() {
        assertThrows(BusinessException.class, () -> validator.validarPredioParaCalculo(null));

        PredioTributario predioInactivo = PredioTributario.builder()
                .estado(EstadoPredio.INACTIVO)
                .tipoPredio(TipoPredio.URBANO)
                .build();
        assertThrows(BusinessException.class, () -> validator.validarPredioParaCalculo(predioInactivo));
    }

    @Test
    @DisplayName("2. Validación Predio: Acepta predios urbanos con datos indispensables")
    void testValidarPredioValido() {
        PredioTributario predioValido = PredioTributario.builder()
                .estado(EstadoPredio.ACTIVO)
                .tipoPredio(TipoPredio.URBANO)
                .areaTerrenoM2(new BigDecimal("120.00"))
                .build();
        assertDoesNotThrow(() -> validator.validarPredioParaCalculo(predioValido));
    }

    @Test
    @DisplayName("3. Validación Porcentaje Condómino: Rechaza valores fuera del rango (0.01 - 100.00)")
    void testValidarPorcentajeCondomino() {
        assertThrows(BusinessException.class, () -> validator.validarPorcentajeCondomino(null));
        assertThrows(BusinessException.class, () -> validator.validarPorcentajeCondomino(BigDecimal.ZERO));
        assertThrows(BusinessException.class, () -> validator.validarPorcentajeCondomino(new BigDecimal("-5.00")));
        assertThrows(BusinessException.class, () -> validator.validarPorcentajeCondomino(new BigDecimal("100.01")));

        assertDoesNotThrow(() -> validator.validarPorcentajeCondomino(new BigDecimal("50.00")));
        assertDoesNotThrow(() -> validator.validarPorcentajeCondomino(new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("4. Validación Período: Rechaza períodos cerrados o históricos")
    void testValidarPeriodoCerrado() {
        PeriodoTributario cerrado = PeriodoTributario.builder()
                .anio(2020)
                .estado(EstadoPeriodoTributario.CERRADO)
                .build();

        assertThrows(BusinessException.class, () -> validator.validarPeriodoParaCalculo(cerrado));

        PeriodoTributario abierto = PeriodoTributario.builder()
                .anio(2026)
                .estado(EstadoPeriodoTributario.APERTURADO)
                .build();

        assertDoesNotThrow(() -> validator.validarPeriodoParaCalculo(abierto));
    }

    @Test
    @DisplayName("5. Validación Transición de Estados: Permite flujo reglamentario")
    void testTransicionValida() {
        assertDoesNotThrow(() -> validator.validarTransicionEstado(EstadoCalculoArbitrio.PRELIMINAR, EstadoCalculoArbitrio.EN_REVISION));
        assertDoesNotThrow(() -> validator.validarTransicionEstado(EstadoCalculoArbitrio.EN_REVISION, EstadoCalculoArbitrio.OBSERVADO));
        assertDoesNotThrow(() -> validator.validarTransicionEstado(EstadoCalculoArbitrio.OBSERVADO, EstadoCalculoArbitrio.EN_REVISION));
        assertDoesNotThrow(() -> validator.validarTransicionEstado(EstadoCalculoArbitrio.EN_REVISION, EstadoCalculoArbitrio.APROBADO));
        assertDoesNotThrow(() -> validator.validarTransicionEstado(EstadoCalculoArbitrio.APROBADO, EstadoCalculoArbitrio.ANULADO));
    }

    @Test
    @DisplayName("6. Validación Transición de Estados: Impide regresiones indebidas o modificaciones sobre cálculos anulados")
    void testTransicionInvalida() {
        // No se puede retroceder de APROBADO a PRELIMINAR
        assertThrows(BusinessException.class, () -> validator.validarTransicionEstado(EstadoCalculoArbitrio.APROBADO, EstadoCalculoArbitrio.PRELIMINAR));
        // No se puede reactivar un cálculo ANULADO
        assertThrows(BusinessException.class, () -> validator.validarTransicionEstado(EstadoCalculoArbitrio.ANULADO, EstadoCalculoArbitrio.APROBADO));
        assertThrows(BusinessException.class, () -> validator.validarTransicionEstado(EstadoCalculoArbitrio.ANULADO, EstadoCalculoArbitrio.PRELIMINAR));
    }

    @Test
    @DisplayName("7. Validación Motivo: Exige justificación para observar o anular")
    void testValidarMotivoRequerido() {
        assertThrows(BusinessException.class, () -> validator.validarMotivo(null, "la anulación"));
        assertThrows(BusinessException.class, () -> validator.validarMotivo("   ", "la observación"));
        assertDoesNotThrow(() -> validator.validarMotivo("Error detectado en metros de frente", "la observación"));
    }
}
