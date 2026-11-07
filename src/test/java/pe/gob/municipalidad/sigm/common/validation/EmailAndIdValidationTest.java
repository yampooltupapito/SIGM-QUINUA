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

class EmailAndIdValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private static class EmailDto {
        @ValidEmail
        private final String email;

        public EmailDto(String email) {
            this.email = email;
        }
    }

    private static class IdDto {
        @PositiveId
        private final Long id;

        public IdDto(Long id) {
            this.id = id;
        }
    }

    @Test
    @DisplayName("Correos electrónicos válidos con RFC 5322 sintáctico deben pasar")
    void validEmailsShouldPass() {
        assertTrue(validator.validate(new EmailDto("alcaldia@muniquinua.gob.pe")).isEmpty());
        assertTrue(validator.validate(new EmailDto("juan.perez@gmail.com")).isEmpty());
        assertTrue(validator.validate(new EmailDto("contacto+tramite@empresa.com.pe")).isEmpty());
        assertTrue(validator.validate(new EmailDto(null)).isEmpty()); // Nulo pasa a menos que se use @NotBlank
    }

    @Test
    @DisplayName("Correos inválidos o con sintaxis errónea deben ser rechazados")
    void invalidEmailsShouldFail() {
        assertFalse(validator.validate(new EmailDto("correo-sin-arroba")).isEmpty());
        assertFalse(validator.validate(new EmailDto("correo@")).isEmpty());
        assertFalse(validator.validate(new EmailDto("@dominio.com")).isEmpty());
        assertFalse(validator.validate(new EmailDto("espacio en@correo.com")).isEmpty());
    }

    @Test
    @DisplayName("Identificadores positivos mayores a cero deben pasar")
    void positiveIdShouldPass() {
        assertTrue(validator.validate(new IdDto(1L)).isEmpty());
        assertTrue(validator.validate(new IdDto(999999L)).isEmpty());
        assertTrue(validator.validate(new IdDto(null)).isEmpty()); // Nulo es válido para creación si no se exige @NotNull
    }

    @Test
    @DisplayName("Identificadores cero o negativos deben fallar")
    void zeroOrNegativeIdShouldFail() {
        assertFalse(validator.validate(new IdDto(0L)).isEmpty());
        assertFalse(validator.validate(new IdDto(-5L)).isEmpty());
    }
}
