package ru.practicum.ewm.event.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;


public class EventDateValidator implements ConstraintValidator<EventDateConstraint, LocalDateTime> {

    private int hours;

    @Override
    public void initialize(EventDateConstraint constraintAnnotation) {
        this.hours = constraintAnnotation.hours();
    }

    @Override
    public boolean isValid(LocalDateTime localDateTime, ConstraintValidatorContext constraintValidatorContext) {
        return localDateTime == null || LocalDateTime.now().plusHours(hours).compareTo(localDateTime) <= 0;
    }

}
