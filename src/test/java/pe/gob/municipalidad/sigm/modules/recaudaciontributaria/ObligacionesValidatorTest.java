package pe.gob.municipalidad.sigm.modules.recaudaciontributaria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.AccionObligacionRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.GeneracionObligacionRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CalculoArbitrioMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CalculoImpuestoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDeterminacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validation.ObligacionValidator;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias de Validación Normativa de Obligaciones — Prompt 57")
class ObligacionesValidatorTest {

    private ObligacionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ObligacionValidator();
    }

    @Test
    @DisplayName("Debe rechazar solicitud de generación individual con parámetros nulos o cuotas inválidas")
    void testValidarGeneracionIndividualParametrosInvalidos() {
        assertThrows(BusinessException.class, () -> validator.validarGeneracionIndividual(null));

        GeneracionObligacionRequest reqSinTipo = GeneracionObligacionRequest.builder()
                .determinacionId(UUID.randomUUID())
                .cuotas(1)
                .build();
        assertThrows(BusinessException.class, () -> validator.validarGeneracionIndividual(reqSinTipo));

        GeneracionObligacionRequest reqCuotasCero = GeneracionObligacionRequest.builder()
                .tipoDeterminacion(TipoDeterminacionTributaria.IMPUESTO_PREDIAL)
                .determinacionId(UUID.randomUUID())
                .cuotas(0)
                .build();
        assertThrows(BusinessException.class, () -> validator.validarGeneracionIndividual(reqCuotasCero));
    }

    @Test
    @DisplayName("Debe rechazar generación de obligación si la determinación predial no está en estado APROBADO")
    void testRechazarDeterminacionPredialNoAprobada() {
        CalculoImpuestoPredial predialSimulacion = CalculoImpuestoPredial.builder()
                .id(UUID.randomUUID())
                .codigoCalculo("CALC-PRED-SIM")
                .estado(EstadoCalculoPredial.SIMULACION)
                .impuestoAnualDeterminado(new BigDecimal("350.00"))
                .build();

        assertThrows(BusinessException.class, () -> validator.validarElegibilidadPredial(predialSimulacion, false));
    }

    @Test
    @DisplayName("Debe rechazar generación de obligación predial si ya existe una obligación activa registrada")
    void testRechazarDuplicidadObligacionPredial() {
        CalculoImpuestoPredial predialAprobado = CalculoImpuestoPredial.builder()
                .id(UUID.randomUUID())
                .codigoCalculo("CALC-PRED-001")
                .estado(EstadoCalculoPredial.APROBADO)
                .impuestoAnualDeterminado(new BigDecimal("420.00"))
                .build();

        assertThrows(BusinessException.class, () -> validator.validarElegibilidadPredial(predialAprobado, true));
    }

    @Test
    @DisplayName("Debe rechazar generación de arbitrios si la determinación no está APROBADA")
    void testRechazarDeterminacionArbitriosNoAprobada() {
        CalculoArbitrioMunicipal arbEnRevision = CalculoArbitrioMunicipal.builder()
                .id(UUID.randomUUID())
                .codigoCalculo("CALC-ARB-REV")
                .estado(EstadoCalculoArbitrio.EN_REVISION)
                .importeAnualTotal(new BigDecimal("180.00"))
                .build();

        assertThrows(BusinessException.class, () -> validator.validarElegibilidadArbitrio(arbEnRevision, false));
    }

    @Test
    @DisplayName("Debe exigir motivo de anulación formal de al menos 10 caracteres y resolución")
    void testValidarAnulacionMotivoInsuficiente() {
        ObligacionTributaria obl = ObligacionTributaria.builder()
                .id(UUID.randomUUID())
                .estado(EstadoObligacionTributaria.REGISTRADA)
                .build();

        AccionObligacionRequest reqCorto = AccionObligacionRequest.builder()
                .motivo("Error")
                .resolucionSustento("Res-01")
                .build();

        assertThrows(BusinessException.class, () -> validator.validarAnulacion(obl, reqCorto));

        AccionObligacionRequest reqSinResolucion = AccionObligacionRequest.builder()
                .motivo("Motivo formal suficientemente largo para justificar")
                .resolucionSustento("")
                .build();

        assertThrows(BusinessException.class, () -> validator.validarAnulacion(obl, reqSinResolucion));
    }

    @Test
    @DisplayName("Debe impedir anular una obligación que ya está anulada")
    void testImpedirAnularObligacionYaAnulada() {
        ObligacionTributaria obl = ObligacionTributaria.builder()
                .id(UUID.randomUUID())
                .estado(EstadoObligacionTributaria.ANULADA)
                .build();

        AccionObligacionRequest reqValido = AccionObligacionRequest.builder()
                .motivo("Motivo de prueba con longitud adecuada")
                .resolucionSustento("Resolución N° 100")
                .build();

        assertThrows(BusinessException.class, () -> validator.validarAnulacion(obl, reqValido));
    }
}
