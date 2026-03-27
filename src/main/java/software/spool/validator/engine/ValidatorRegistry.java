package software.spool.validator.engine;

import software.spool.validator.api.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ValidatorRegistry {

    private final Map<Class<?>, List<Validator<?>>> registry = new ConcurrentHashMap<>();

    public ValidatorRegistry() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ValidatorRegistry(ClassLoader classLoader) {
        ServiceLoader.load(Validator.class, classLoader)
                .forEach(this::register);
    }

    /** Explicit registration. Intended for tests or programmatic setups. */
    public ValidatorRegistry(List<Validator<?>> validators) {
        validators.forEach(this::register);
    }

    private void register(Validator<?> validator) {
        Validate annotation = validator.getClass().getAnnotation(Validate.class);
        if (annotation == null) throw new IllegalArgumentException(
                validator.getClass().getName() + " must be annotated with @Validate"
        );
        registry.computeIfAbsent(annotation.value(), k -> new ArrayList<>())
                .add(validator);

        registry.get(annotation.value()).sort(
                Comparator
                        .comparingInt((Validator<?> v) -> v.getClass().getAnnotation(Validate.class).order())
                        .thenComparing(v -> v.getClass().getName())
        );
    }

    /**
     * Fail-fast: stops at the first failure regardless of severity.
     * Suitable for simple synchronous validations.
     */
    @SuppressWarnings("unchecked")
    public <T> void validate(T event) throws ValidationException {
        if (event == null) throw new ValidationException("event", "Event must not be null");

        for (Validator<?> v : registeredFor(event.getClass())) {
            Validator<T> typed = (Validator<T>) v;
            boolean passed;
            try {
                passed = typed.validate(event);
            } catch (Exception e) {
                Severity severity = v.getClass().getAnnotation(Validate.class).severity();
                throw new ValidationException(
                        v.failedField(),
                        "Unexpected error in " + v.getClass().getSimpleName() + ": " + e.getMessage(),
                        severity
                );
            }
            if (!passed) {
                Severity severity = v.getClass().getAnnotation(Validate.class).severity();
                throw new ValidationException(v.failedField(), typed.failMessage(event), severity);
            }
        }
    }

    /**
     * Resilient: runs ALL validators, collects every violation.
     * CRITICAL violations short-circuit the chain to avoid cascading failures
     * on validators that depend on basic data integrity.
     * Returns a {@link ValidationResult} with outcome and full violation list.
     */
    @SuppressWarnings("unchecked")
    public <T> ValidationResult validateAll(T event) {
        if (event == null) {
            return ValidationResult.of(List.of(
                    new ValidationException("event", "Event must not be null", Severity.CRITICAL)
            ));
        }

        List<ValidationException> violations = new ArrayList<>();

        for (Validator<?> v : registeredFor(event.getClass())) {
            Validator<T> typed = (Validator<T>) v;
            Severity severity = v.getClass().getAnnotation(Validate.class).severity();
            boolean passed;

            try {
                passed = typed.validate(event);
            } catch (Exception e) {
                violations.add(new ValidationException(
                        v.failedField(),
                        "Unexpected error in " + v.getClass().getSimpleName() + ": " + e.getMessage(),
                        severity
                ));
                if (severity == Severity.CRITICAL) break;
                continue;
            }

            if (!passed) {
                violations.add(new ValidationException(v.failedField(), typed.failMessage(event), severity));
                if (severity == Severity.CRITICAL) break;
            }
        }

        return ValidationResult.of(violations);
    }

    public boolean hasValidatorsFor(Class<?> eventType) {
        return registry.containsKey(eventType);
    }

    public int countValidatorsFor(Class<?> eventType) {
        return registeredFor(eventType).size();
    }

    private List<Validator<?>> registeredFor(Class<?> type) {
        return registry.getOrDefault(type, Collections.emptyList());
    }
}
