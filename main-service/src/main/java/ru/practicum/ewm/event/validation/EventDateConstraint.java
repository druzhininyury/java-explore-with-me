package ru.practicum.ewm.event.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EventDateValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EventDateConstraint {

    String message() default "Time must be two hours later then now.";

    int hours() default 2;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
