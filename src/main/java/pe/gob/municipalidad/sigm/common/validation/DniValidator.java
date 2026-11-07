package pe.gob.municipalidad.sigm.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador para DNI peruano (8 dígitos numéricos exactos).
 */
public class DniValidator implements ConstraintValidator<Dni, String> {

    private static final String DNI_REGEX = "^[0-9]{8}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Use @NotBlank alongside if mandatory
        }
        return value.trim().matches(DNI_REGEX);
    }
}
