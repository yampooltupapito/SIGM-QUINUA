package pe.gob.municipalidad.sigm.modules.recaudaciontributaria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoPredialCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.SimulacionPredialRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CalculoImpuestoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PeriodoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoBeneficioPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validation.CalculoPredialValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ImpuestoPredialValidatorTest {

    private CalculoPredialValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CalculoPredialValidator();
    }

    @Test
    @DisplayName("Validación de Simulación: bloquea datos incompletos o UIT no parametrizada")
    void testValidarSimulacionIncompleta() {
        SimulacionPredialRequest req = SimulacionPredialRequest.builder()
                .contribuyenteId(UUID.randomUUID())
                .periodoId(UUID.randomUUID())
                .fechaReferencia(LocalDate.of(2026, 1, 1))
                .beneficio(TipoBeneficioPredial.NINGUNO)
                .build();

        ContribuyenteTributario contribuyente = ContribuyenteTributario.builder()
                .codigoContribuyente("CONT-001")
                .build();

        PeriodoTributario periodoSinUit = PeriodoTributario.builder()
                .anio(2026)
                .uitVigente(BigDecimal.ZERO)
                .build();

        // Error si el periodo no tiene UIT configurada (> 0)
        assertThrows(BusinessException.class, () ->
                validator.validarSimulacion(req, contribuyente, periodoSinUit, List.of("predio1")));

        // Error si el contribuyente no tiene predios relacionados
        PeriodoTributario periodoConUit = PeriodoTributario.builder()
                .anio(2026)
                .uitVigente(new BigDecimal("5150.00"))
                .build();

        assertThrows(BusinessException.class, () ->
                validator.validarSimulacion(req, contribuyente, periodoConUit, Collections.emptyList()));

        // Válido si tiene datos completos
        assertDoesNotThrow(() ->
                validator.validarSimulacion(req, contribuyente, periodoConUit, List.of("predio1")));
    }

    @Test
    @DisplayName("Validación de Registro Preliminar: exige parámetros oficiales")
    void testValidarRegistroCalculo() {
        CalculoPredialCreateRequest req = CalculoPredialCreateRequest.builder()
                .contribuyenteId(UUID.randomUUID())
                .periodoId(UUID.randomUUID())
                .fechaReferencia(LocalDate.of(2026, 1, 1))
                .beneficio(TipoBeneficioPredial.NINGUNO)
                .build();

        ContribuyenteTributario contribuyente = ContribuyenteTributario.builder()
                .codigoContribuyente("CONT-001")
                .build();

        PeriodoTributario periodoConUit = PeriodoTributario.builder()
                .anio(2026)
                .uitVigente(new BigDecimal("5150.00"))
                .build();

        // Sin contribuyente
        assertThrows(BusinessException.class, () ->
                validator.validarRegistroCalculo(req, null, periodoConUit, List.of("predio1")));

        // Sin predios
        assertThrows(BusinessException.class, () ->
                validator.validarRegistroCalculo(req, contribuyente, periodoConUit, Collections.emptyList()));

        // Con todo correcto
        assertDoesNotThrow(() ->
                validator.validarRegistroCalculo(req, contribuyente, periodoConUit, List.of("predio1")));
    }

    @Test
    @DisplayName("Validación para Revisión Administrativa: solo desde PRELIMINAR u OBSERVADO")
    void testValidarParaRevision() {
        CalculoImpuestoPredial preliminar = CalculoImpuestoPredial.builder()
                .estado(EstadoCalculoPredial.PRELIMINAR)
                .build();
        assertDoesNotThrow(() -> validator.validarParaRevision(preliminar));

        CalculoImpuestoPredial observado = CalculoImpuestoPredial.builder()
                .estado(EstadoCalculoPredial.OBSERVADO)
                .build();
        assertDoesNotThrow(() -> validator.validarParaRevision(observado));

        CalculoImpuestoPredial aprobado = CalculoImpuestoPredial.builder()
                .estado(EstadoCalculoPredial.APROBADO)
                .build();
        assertThrows(BusinessException.class, () -> validator.validarParaRevision(aprobado));

        CalculoImpuestoPredial anulado = CalculoImpuestoPredial.builder()
                .estado(EstadoCalculoPredial.ANULADO)
                .build();
        assertThrows(BusinessException.class, () -> validator.validarParaRevision(anulado));
    }

    @Test
    @DisplayName("Validación para Aprobación: requiere resolución formal y estado válido")
    void testValidarParaAprobacion() {
        CalculoImpuestoPredial enRevision = CalculoImpuestoPredial.builder()
                .estado(EstadoCalculoPredial.EN_REVISION)
                .build();

        // Sin resolución formal
        assertThrows(BusinessException.class, () ->
                validator.validarParaAprobacion(enRevision, null));
        assertThrows(BusinessException.class, () ->
                validator.validarParaAprobacion(enRevision, "   "));

        // Con resolución formal
        assertDoesNotThrow(() ->
                validator.validarParaAprobacion(enRevision, "RESOLUCION-001-2026"));

        // Desde estado no permitido (ej. ANULADO)
        CalculoImpuestoPredial anulado = CalculoImpuestoPredial.builder()
                .estado(EstadoCalculoPredial.ANULADO)
                .build();
        assertThrows(BusinessException.class, () ->
                validator.validarParaAprobacion(anulado, "RESOLUCION-001-2026"));
    }

    @Test
    @DisplayName("Validación para Anulación: requiere motivo formal y estado no anulado previamente")
    void testValidarParaAnulacion() {
        CalculoImpuestoPredial c = CalculoImpuestoPredial.builder()
                .estado(EstadoCalculoPredial.PRELIMINAR)
                .build();

        // Sin motivo
        assertThrows(BusinessException.class, () ->
                validator.validarParaAnulacion(c, ""));
        assertThrows(BusinessException.class, () ->
                validator.validarParaAnulacion(c, null));

        // Con motivo
        assertDoesNotThrow(() ->
                validator.validarParaAnulacion(c, "Rectificación de DJ de autovalúo"));

        // Ya anulado
        c.setEstado(EstadoCalculoPredial.ANULADO);
        assertThrows(BusinessException.class, () ->
                validator.validarParaAnulacion(c, "Nuevo motivo"));
    }
}
