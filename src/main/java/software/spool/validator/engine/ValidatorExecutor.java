package software.spool.validator.engine;

import software.spool.validator.api.Severity;
import software.spool.validator.api.ValidationException;
import software.spool.validator.api.ValidationResult;
import software.spool.validator.api.Validator;

import java.util.ArrayList;
import java.util.List;

final class ValidatorExecutor {

    private final ValidatorStore store;

    ValidatorExecutor(ValidatorStore store) {
        this.store = store;
    }

    <T> void validate(String sourceId, T event) throws ValidationException {
        requireEvent(event);

        for (ValidatorDescriptor<?> candidate : store.descriptorsFor(sourceId, event.getClass())) {
            ValidationException failure = execute(candidate, event);
            if (failure != null) {
                throw failure;
            }
        }
    }

    <T> ValidationResult validateAll(String sourceId, T event) {
        if (event == null) {
            return ValidationResult.of(List.of(
                    new ValidationException("event", "Event must not be null", Severity.CRITICAL)
            ));
        }

        List<ValidationException> failures = new ArrayList<>();

        for (ValidatorDescriptor<?> candidate : store.descriptorsFor(sourceId, event.getClass())) {
            ValidationException failure = execute(candidate, event);
            if (failure == null) {
                continue;
            }

            failures.add(failure);

            if (failure.getSeverity() == Severity.CRITICAL) {
                break;
            }
        }

        return ValidationResult.of(failures);
    }

    private void requireEvent(Object event) throws ValidationException {
        if (event == null) {
            throw new ValidationException("event", "Event must not be null");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> ValidationException execute(ValidatorDescriptor<?> candidate, T event) {
        ValidatorDescriptor<T> descriptor = (ValidatorDescriptor<T>) candidate;
        Validator<T> validator = descriptor.validator();

        try {
            boolean valid = validator.validate(event);
            if (valid) {
                return null;
            }

            return new ValidationException(
                    validator.failedField(),
                    validator.failMessage(event),
                    descriptor.severity()
            );
        } catch (Exception e) {
            return new ValidationException(
                    validator.failedField(),
                    "Unexpected error in " + descriptor.validatorName() + ": " + e.getMessage(),
                    descriptor.severity()
            );
        }
    }
}