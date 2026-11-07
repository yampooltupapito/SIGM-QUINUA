package pe.gob.municipalidad.sigm.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para validar que un identificador numérico de base de datos sea estrictamente positivo (> 0).
 */
@Documented
@Constraint(validatedBy = PositiveIdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PositiveId {

    String message() default "El identificador debe ser un número entero positivo mayor a cero.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
