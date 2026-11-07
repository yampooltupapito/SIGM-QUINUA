package pe.gob.municipalidad.sigm.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para validar Registro Único de Contribuyentes (RUC) peruano ante SUNAT (11 dígitos).
 */
@Documented
@Constraint(validatedBy = RucValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Ruc {

    String message() default "{validation.ruc.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
