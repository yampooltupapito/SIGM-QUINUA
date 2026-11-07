package pe.gob.municipalidad.sigm.modules.recaudaciontributaria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.PredioCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.ClasificacionPredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.UsoDeclaradoPredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PredioTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validation.PredioValidator;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PredioValidatorTest {

    @Mock
    private PredioTributarioRepository predioRepository;

    private PredioValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PredioValidator(predioRepository);
    }

    @Test
    @DisplayName("Validación de áreas: debe ser estrictamente mayor a cero")
    void testValidacionAreas() {
        assertDoesNotThrow(() -> validator.validarArea(new BigDecimal("150.00"), "M2"));
        assertDoesNotThrow(() -> validator.validarArea(new BigDecimal("0.5"), "HA"));

        assertThrows(BusinessException.class, () -> validator.validarArea(BigDecimal.ZERO, "M2"));
        assertThrows(BusinessException.class, () -> validator.validarArea(new BigDecimal("-10.00"), "M2"));
        assertThrows(BusinessException.class, () -> validator.validarArea(null, "M2"));
    }

    @Test
    @DisplayName("Conversión de áreas a metros cuadrados estándar con BigDecimal")
    void testConversionMetrosCuadrados() {
        // En M2 la superficie se conserva
        BigDecimal areaM2 = validator.calcularAreaEnMetrosCuadrados(new BigDecimal("250.75"), "M2");
        assertEquals(new BigDecimal("250.75"), areaM2);

        // En Hectáreas (HA) 1 Ha = 10,000 m2
        BigDecimal areaHa = validator.calcularAreaEnMetrosCuadrados(new BigDecimal("2.5"), "HA");
        assertEquals(new BigDecimal("25000.0"), areaHa);

        // Unidad nula o no informada asume M2 por defecto
        BigDecimal areaDefecto = validator.calcularAreaEnMetrosCuadrados(new BigDecimal("100.00"), null);
        assertEquals(new BigDecimal("100.00"), areaDefecto);
    }

    @Test
    @DisplayName("Validación de coordenadas geográficas en el estándar WGS84")
    void testValidacionCoordenadasWgs84() {
        // Coordenadas válidas en Quinua, Ayacucho
        assertDoesNotThrow(() -> validator.validarCoordenadas(new BigDecimal("-13.045612"), new BigDecimal("-74.138945")));
        // Coordenadas opcionales nulas son válidas
        assertDoesNotThrow(() -> validator.validarCoordenadas(null, null));

        // Inconsistencia: una presente y la otra nula
        assertThrows(BusinessException.class, () -> validator.validarCoordenadas(new BigDecimal("-13.0456"), null));
        assertThrows(BusinessException.class, () -> validator.validarCoordenadas(null, new BigDecimal("-74.1389")));

        // Fuera de rango de latitud (-90 a 90)
        assertThrows(BusinessException.class, () -> validator.validarCoordenadas(new BigDecimal("-95.00"), new BigDecimal("-74.1389")));
        assertThrows(BusinessException.class, () -> validator.validarCoordenadas(new BigDecimal("91.00"), new BigDecimal("-74.1389")));

        // Fuera de rango de longitud (-180 a 180)
        assertThrows(BusinessException.class, () -> validator.validarCoordenadas(new BigDecimal("-13.0456"), new BigDecimal("-185.00")));
        assertThrows(BusinessException.class, () -> validator.validarCoordenadas(new BigDecimal("-13.0456"), new BigDecimal("190.00")));
    }

    @Test
    @DisplayName("Validación de código catastral: opcional pero único si está presente")
    void testValidacionCodigoCatastralUnicidad() {
        // Código catastral nulo o en blanco es permitido
        assertDoesNotThrow(() -> validator.validarCodigoCatastral(null, null));
        assertDoesNotThrow(() -> validator.validarCodigoCatastral("   ", null));

        // Código catastral duplicado
        when(predioRepository.existsByCodigoCatastralIgnoreCase("05011001001")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> validator.validarCodigoCatastral("05011001001", null));

        // Código catastral perteneciente al mismo predio en edición (permitido)
        UUID predioId = UUID.randomUUID();
        when(predioRepository.existsByCodigoCatastralIgnoreCaseAndIdNot("05011001001", predioId)).thenReturn(false);
        assertDoesNotThrow(() -> validator.validarCodigoCatastral("05011001001", predioId));
    }

    @Test
    @DisplayName("Validación de porcentaje de participación de contribuyente en el predio")
    void testValidacionPorcentajeParticipacion() {
        assertDoesNotThrow(() -> validator.validarPorcentajeParticipacion(new BigDecimal("100.00")));
        assertDoesNotThrow(() -> validator.validarPorcentajeParticipacion(new BigDecimal("50.00")));
        assertDoesNotThrow(() -> validator.validarPorcentajeParticipacion(new BigDecimal("0.01")));
        assertDoesNotThrow(() -> validator.validarPorcentajeParticipacion(null)); // opcional

        assertThrows(BusinessException.class, () -> validator.validarPorcentajeParticipacion(BigDecimal.ZERO));
        assertThrows(BusinessException.class, () -> validator.validarPorcentajeParticipacion(new BigDecimal("-5.00")));
        assertThrows(BusinessException.class, () -> validator.validarPorcentajeParticipacion(new BigDecimal("100.01")));
    }

    @Test
    @DisplayName("Validación de transiciones de estado administrativo del predio")
    void testValidacionTransicionEstado() {
        // Transiciones válidas con motivo
        assertDoesNotThrow(() -> validator.validarTransicionEstado(EstadoPredio.ACTIVO, EstadoPredio.OBSERVADO, "Falta documentación"));
        assertDoesNotThrow(() -> validator.validarTransicionEstado(EstadoPredio.OBSERVADO, EstadoPredio.EN_REVISION, "Documentos subsanados"));
        assertDoesNotThrow(() -> validator.validarTransicionEstado(EstadoPredio.EN_REVISION, EstadoPredio.ACTIVO, "Revisión aprobada"));
        assertDoesNotThrow(() -> validator.validarTransicionEstado(EstadoPredio.ACTIVO, EstadoPredio.INACTIVO, "Demolición total"));

        // Mismo estado
        assertThrows(BusinessException.class, () -> validator.validarTransicionEstado(EstadoPredio.ACTIVO, EstadoPredio.ACTIVO, "Sin cambio"));

        // Motivo vacío
        assertThrows(BusinessException.class, () -> validator.validarTransicionEstado(EstadoPredio.ACTIVO, EstadoPredio.OBSERVADO, ""));
        assertThrows(BusinessException.class, () -> validator.validarTransicionEstado(EstadoPredio.ACTIVO, EstadoPredio.OBSERVADO, null));

        // Transición no permitida desde INACTIVO hacia ACTIVO directo sin revisión
        assertThrows(BusinessException.class, () -> validator.validarTransicionEstado(EstadoPredio.INACTIVO, EstadoPredio.ACTIVO, "Reactivar"));
    }
}
