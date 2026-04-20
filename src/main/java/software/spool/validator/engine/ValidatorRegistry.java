package software.spool.validator.engine;

import software.spool.validator.api.ValidationException;
import software.spool.validator.api.ValidationResult;
import software.spool.validator.api.Validator;

import java.util.Collection;
import java.util.Optional;

public final class ValidatorRegistry {

    private final ValidatorStore store;
    private final ValidatorExecutor executor;

    public ValidatorRegistry() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ValidatorRegistry(ClassLoader classLoader) {
        this(ValidatorLoader.load(classLoader));
    }

    public ValidatorRegistry(Collection<? extends Validator<?>> validators) {
        this.store = new ValidatorStore(validators);
        this.executor = new ValidatorExecutor(store);
    }

    public Optional<Class<?>> resolveClass(String sourceId) {
        return store.resolveClass(sourceId);
    }

    public boolean hasValidatorsFor(String sourceId, Class<?> eventType) {
        return store.hasValidatorsFor(sourceId, eventType);
    }

    public int countValidatorsFor(String sourceId, Class<?> eventType) {
        return store.countValidatorsFor(sourceId, eventType);
    }

    public <T> void validate(String sourceId, T event) throws ValidationException {
        executor.validate(sourceId, event);
    }

    public <T> ValidationResult validateAll(String sourceId, T event) {
        return executor.validateAll(sourceId, event);
    }
}