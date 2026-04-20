package software.spool.validator.engine;

import software.spool.validator.api.*;

import java.util.List;
import java.util.Objects;

public class ValidatorExecutor {
    private final ValidatorStore store;

    ValidatorExecutor(ValidatorStore store) {
        this.store = store;
    }

    <T> void validate(String sourceId, T event) throws ValidationException {
        requireEvent(event);
        for (Validator<?> validator : store.validatorsFor(sourceId, event.getClass())) {
            ValidationException failure = validateSafely(validator, event);
            if (failure != null) throw failure;
        }
    }

    <T> ValidationResult validateAll(String sourceId, T event) {
        if (event == null) return ValidationResult.of(List.of(
                    new ValidationException("event", "Event must not be null", Severity.CRITICAL)
            ));
        return ValidationResult.of(getViolations(sourceId, event));
    }

    private <T> List<ValidationException> getViolations(String sourceId, T event) {
        return store.validatorsFor(sourceId, event.getClass()).stream()
                .map(v -> validateSafely(v, event))
                .filter(Objects::nonNull)
                .toList();
    }

    private void requireEvent(Object event) throws ValidationException {
        if (event == null) throw new ValidationException("event", "Event must not be null");
    }

    private <T> ValidationException validateSafely(Validator<?> validator, T event) {
        return validateTyped(cast(validator), event);
    }

    @SuppressWarnings("unchecked")
    private <T> Validator<T> cast(Validator<?> validator) {
        return (Validator<T>) validator;
    }

    private <T> ValidationException validateTyped(Validator<T> validator, T event) {
        Severity severity = annotationOf(validator).severity();
        try {
            if (validator.validate(event)) return null;
            return new ValidationException(
                    validator.failedField(),
                    validator.failMessage(event),
                    severity
            );
        } catch (Exception e) {
            return new ValidationException(
                    validator.failedField(),
                    "Unexpected error in " + validator.getClass().getSimpleName() + ": " + e.getMessage(),
                    severity
            );
        }
    }

    private static Validate annotationOf(Validator<?> validator) {
        Validate annotation = validator.getClass().getAnnotation(Validate.class);
        if (annotation == null)
            throw new IllegalArgumentException(
                    validator.getClass().getName() + " must be annotated with @Validate"
            );
        return annotation;
    }
}