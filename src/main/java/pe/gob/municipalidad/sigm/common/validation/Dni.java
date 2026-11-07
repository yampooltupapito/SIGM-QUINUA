package pe.gob.municipalidad.sigm.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para validar Documento Nacional de Identidad (DNI) peruano (8 dígitos numéricos).
 */
@Documented
@Constraint(validatedBy = DniValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Dni {

    String message() default "{validation.dni.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
