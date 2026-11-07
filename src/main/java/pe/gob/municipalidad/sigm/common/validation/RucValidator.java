package pe.gob.municipalidad.sigm.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador oficial de RUC peruano según algoritmo módulo 11 de la SUNAT.
 */
public class RucValidator implements ConstraintValidator<Ruc, String> {

    private static final String RUC_REGEX = "^(10|15|16|17|20)[0-9]{9}$";
    private static final int[] MULTIPLIERS = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Use @NotBlank alongside if mandatory
        }

        String ruc = value.trim();
        if (!ruc.matches(RUC_REGEX)) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(ruc.charAt(i)) * MULTIPLIERS[i];
        }

        int remainder = sum % 11;
        int checkDigit = 11 - remainder;
        if (checkDigit == 10) {
            checkDigit = 0;
        } else if (checkDigit == 11) {
            checkDigit = 1;
        }

        return checkDigit == Character.getNumericValue(ruc.charAt(10));
    }
}
