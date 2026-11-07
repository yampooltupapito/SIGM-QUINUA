package pe.gob.municipalidad.sigm.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador para la anotación {@link PositiveId}.
 * Valida que un número sea estrictamente positivo (> 0).
 * Los valores nulos son considerados válidos (usar @NotNull para obligatoriedad).
 */
public class PositiveIdValidator implements ConstraintValidator<PositiveId, Number> {

    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.longValue() > 0;
    }
}
