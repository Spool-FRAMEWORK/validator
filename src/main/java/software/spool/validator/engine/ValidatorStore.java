package software.spool.validator.engine;

import software.spool.validator.api.Validate;
import software.spool.validator.api.Validator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ValidatorStore {
    private static final Comparator<Validator<?>> BY_ORDER_AND_NAME =
            Comparator.comparingInt((Validator<?> validator) -> annotationOf(validator).order())
                    .thenComparing(validator -> validator.getClass().getName());
    private final Map<String, Map<Class<?>, List<Validator<?>>>> registry = new ConcurrentHashMap<>();

    ValidatorStore(Collection<? extends Validator<?>> validators) {
        validators.forEach(this::register);
    }

    Optional<Class<?>> resolveClass(String sourceId) {
        if (Objects.isNull(sourceId)) return Optional.empty();
        Map<Class<?>, List<Validator<?>>> byClass = registry.get(sourceId);
        if (byClass == null || byClass.isEmpty()) return Optional.empty();
        return byClass.keySet().stream().findFirst();
    }

    boolean hasValidatorsFor(String sourceId, Class<?> eventType) {
        return registry.getOrDefault(sourceId, Collections.emptyMap()).containsKey(eventType);
    }

    int countValidatorsFor(String sourceId, Class<?> eventType) {
        return validatorsFor(sourceId, eventType).size();
    }

    List<Validator<?>> validatorsFor(String sourceId, Class<?> eventType) {
        return registry.getOrDefault(sourceId, Collections.emptyMap())
                .getOrDefault(eventType, Collections.emptyList());
    }

    private void register(Validator<?> validator) {
        Validate annotation = annotationOf(validator);
        registry.computeIfAbsent(annotation.sourceId(), ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(annotation.value(), ignored -> new ArrayList<>())
                .add(validator);
        registry.get(annotation.sourceId())
                .get(annotation.value())
                .sort(BY_ORDER_AND_NAME);
    }

    private static Validate annotationOf(Validator<?> validator) {
        Validate annotation = validator.getClass().getAnnotation(Validate.class);
        if (annotation == null) throw new IllegalArgumentException(
                validator.getClass().getName() + " must be annotated with @Validate");
        return annotation;
    }
}