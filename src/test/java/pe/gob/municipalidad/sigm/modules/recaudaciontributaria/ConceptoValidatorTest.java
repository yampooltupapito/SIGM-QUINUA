package pe.gob.municipalidad.sigm.modules.recaudaciontributaria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ConceptoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ConfiguracionConceptoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validation.ConceptoValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConceptoValidatorTest {

    @Mock
    private ConceptoTributarioRepository conceptoRepository;

    @Mock
    private ConfiguracionConceptoTributarioRepository configuracionRepository;

    @InjectMocks
    private ConceptoValidator validator;

    private ConceptoCreateRequest requestCrear;
    private ParametroCreateRequest paramRequest;

    @BeforeEach
    void setUp() {
        requestCrear = ConceptoCreateRequest.builder()
                .codigo("IMP_PREDIAL_TEST")
                .nombre("Impuesto Predial de Prueba")
                .naturalezaTributaria(NaturalezaTributaria.IMPUESTO)
                .tipoConcepto(TipoConceptoTributario.IMPUESTO_PREDIAL)
                .periodicidad(PeriodicidadConcepto.ANUAL)
                .aplicaAPredio(true)
                .cuentaPresupuestal("1.1.2.1.1.1")
                .fechaInicioVigencia(LocalDate.of(2026, 1, 1))
                .build();

        paramRequest = ParametroCreateRequest.builder()
                .codigo("TRAMO_1")
                .nombre("Tramo 1")
                .tipoDato(TipoDatoParametro.DECIMAL)
                .unidad(UnidadMedidaParametro.PORCENTAJE)
                .valorDecimal(new BigDecimal("0.20"))
                .esObligatorio(true)
                .build();
    }

    @Test
    @DisplayName("Debe validar exitosamente cuando el código es único y válido")
    void testValidarCreacionExitoso() {
        when(conceptoRepository.existsByCodigoIgnoreCase("IMP_PREDIAL_TEST")).thenReturn(false);

        validator.validarCreacion(requestCrear);
        // Sin excepciones
    }

    @Test
    @DisplayName("Debe fallar si el código ya existe en el catálogo maestro")
    void testValidarCreacionCodigoDuplicado() {
        when(conceptoRepository.existsByCodigoIgnoreCase("IMP_PREDIAL_TEST")).thenReturn(true);

        assertThatThrownBy(() -> validator.validarCreacion(requestCrear))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya se encuentra registrado");
    }

    @Test
    @DisplayName("Debe fallar si el código contiene caracteres no permitidos")
    void testValidarCodigoInvalido() {
        requestCrear.setCodigo("imp predial!");
        assertThatThrownBy(() -> validator.validarCreacion(requestCrear))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("mayúsculas");
    }

    @Test
    @DisplayName("Debe fallar si la fecha fin es anterior a fecha inicio")
    void testValidarFechasIncoherentes() {
        requestCrear.setFechaInicioVigencia(LocalDate.of(2026, 5, 1));
        requestCrear.setFechaFinVigencia(LocalDate.of(2026, 1, 1));

        assertThatThrownBy(() -> validator.validarCreacion(requestCrear))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no puede ser anterior");
    }

    @Test
    @DisplayName("Debe permitir agregar parámetros cuando la configuración está en BORRADOR")
    void testPermitirModificacionEnBorrador() {
        ConfiguracionConceptoTributario conf = ConfiguracionConceptoTributario.builder()
                .estado(EstadoConfiguracionConcepto.BORRADOR)
                .build();

        validator.validarModificable(conf);
        // Exitoso
    }

    @Test
    @DisplayName("Debe rechazar modificaciones si la versión está en estado ACTIVO o APROBADO")
    void testRechazarModificacionEnActivo() {
        ConfiguracionConceptoTributario conf = ConfiguracionConceptoTributario.builder()
                .numeroVersion(1)
                .estado(EstadoConfiguracionConcepto.ACTIVO)
                .build();

        assertThatThrownBy(() -> validator.validarModificable(conf))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no puede ser modificada directamente");
    }

    @Test
    @DisplayName("Debe validar parámetro con porcentaje dentro del rango 0 a 100")
    void testValidarParametroPorcentaje() {
        validator.validarParametro(paramRequest);
        // Exitoso

        paramRequest.setValorDecimal(new BigDecimal("150.00"));
        assertThatThrownBy(() -> validator.validarParametro(paramRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("debe estar comprendido entre 0 y 100");
    }

    @Test
    @DisplayName("Debe fallar al aprobar si no se proporciona resolución formal")
    void testValidarAprobacionSinResolucion() {
        ConfiguracionConceptoTributario conf = ConfiguracionConceptoTributario.builder()
                .estado(EstadoConfiguracionConcepto.EN_REVISION)
                .build();

        assertThatThrownBy(() -> validator.validarAprobacion(conf, ""))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("resolución");
    }

    @Test
    @DisplayName("Debe validar transición de estado para activación")
    void testValidarActivacionSoloDesdeAprobado() {
        ConfiguracionConceptoTributario conf = ConfiguracionConceptoTributario.builder()
                .numeroVersion(1)
                .estado(EstadoConfiguracionConcepto.BORRADOR)
                .build();

        assertThatThrownBy(() -> validator.validarActivacion(conf))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("APROBADA");
    }

    @Test
    @DisplayName("Debe fallar al desactivar si no se proporciona motivo fundamentado")
    void testValidarDesactivacionSinMotivo() {
        ConfiguracionConceptoTributario conf = ConfiguracionConceptoTributario.builder()
                .estado(EstadoConfiguracionConcepto.ACTIVO)
                .build();

        assertThatThrownBy(() -> validator.validarDesactivacion(conf, "   "))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("motivo fundamentado");
    }
}
