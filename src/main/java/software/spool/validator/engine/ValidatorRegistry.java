package software.spool.validator.engine;

import software.spool.validator.api.Validate;
import software.spool.validator.api.Validator;
import software.spool.validator.api.ValidationException;

import java.util.*;

public class ValidatorRegistry {

    private final Map<Class<?>, List<Validator<?>>> registry = new HashMap<>();

    public ValidatorRegistry() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ValidatorRegistry(ClassLoader classLoader) {
        ServiceLoader.load(Validator.class, classLoader)
            .forEach(this::register);
    }

    /** Constructor para tests: permite registrar validators manualmente. */
    public ValidatorRegistry(List<Validator<?>> validators) {
        validators.forEach(this::register);
    }

    private void register(Validator<?> validator) {
        Validate annotation = validator.getClass().getAnnotation(Validate.class);
        if (annotation == null) throw new IllegalArgumentException(
                validator.getClass().getName() + " must be annotated with @SpoolValidator"
        );
        registry.computeIfAbsent(annotation.value(), k -> new ArrayList<>())
                .add(validator);
    }

    @SuppressWarnings("unchecked")
    public <T> void validate(T event) throws ValidationException {
        if (event == null) throw new ValidationException("event", "Event must not be null");

        List<Validator<?>> validators =
            registry.getOrDefault(event.getClass(), Collections.emptyList());

        for (Validator<?> v : validators) {
            ((Validator<T>) v).validate(event);
        }
    }

    public boolean hasValidatorsFor(Class<?> eventType) {
        return registry.containsKey(eventType);
    }

    public int countValidatorsFor(Class<?> eventType) {
        return registry.getOrDefault(eventType, Collections.emptyList()).size();
    }
}
