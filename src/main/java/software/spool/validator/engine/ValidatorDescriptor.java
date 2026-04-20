package software.spool.validator.engine;

import software.spool.validator.api.Severity;
import software.spool.validator.api.Validate;
import software.spool.validator.api.Validator;

import java.util.Objects;

record ValidatorDescriptor<T>(
        Validator<T> validator,
        String sourceId,
        Class<T> eventType,
        int order,
        Severity severity,
        String validatorName
) {
    ValidatorDescriptor {
        Objects.requireNonNull(validator, "validator must not be null");
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(validatorName, "validatorName must not be null");
    }

    @SuppressWarnings("unchecked")
    static <T> ValidatorDescriptor<T> from(Validator<?> rawValidator) {
        Validate annotation = rawValidator.getClass().getAnnotation(Validate.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    rawValidator.getClass().getName() + " must be annotated with @Validate"
            );
        }

        return new ValidatorDescriptor<>(
                (Validator<T>) rawValidator,
                annotation.sourceId(),
                (Class<T>) annotation.value(),
                annotation.order(),
                annotation.severity(),
                rawValidator.getClass().getSimpleName()
        );
    }
}