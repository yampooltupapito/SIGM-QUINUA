package pe.gob.municipalidad.sigm.common.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeruvianIdentificationValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private static class DniDto {
        @Dni
        private final String dni;

        public DniDto(String dni) {
            this.dni = dni;
        }
    }

    private static class RucDto {
        @Ruc
        private final String ruc;

        public RucDto(String ruc) {
            this.ruc = ruc;
        }
    }

    @Test
    @DisplayName("DNI peruano de 8 dígitos numéricos debe ser válido")
    void validDniShouldPass() {
        DniDto dto = new DniDto("45892134");
        Set<ConstraintViolation<DniDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("DNI con letras o longitud distinta de 8 dígitos debe fallar")
    void invalidDniShouldFail() {
        DniDto dtoWithLetters = new DniDto("4589213A");
        assertFalse(validator.validate(dtoWithLetters).isEmpty());

        DniDto dtoTooShort = new DniDto("1234567");
        assertFalse(validator.validate(dtoTooShort).isEmpty());
    }

    @Test
    @DisplayName("RUC peruano con dígito verificador válido debe pasar")
    void validRucShouldPass() {
        RucDto validRuc = new RucDto("20177758958");
        Set<ConstraintViolation<RucDto>> violations = validator.validate(validRuc);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("RUC con prefijo o dígito verificador incorrecto debe fallar")
    void invalidRucShouldFail() {
        RucDto invalidCheckDigit = new RucDto("20177758959"); // Último dígito incorrecto
        assertFalse(validator.validate(invalidCheckDigit).isEmpty());

        RucDto wrongPrefix = new RucDto("30177758958"); // Prefijo 30 no existe en SUNAT
        assertFalse(validator.validate(wrongPrefix).isEmpty());
    }
}
