package software.spool.validator.engine;

import software.spool.validator.api.Validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

final class ValidatorStore {

    private static final Comparator<ValidatorDescriptor<?>> BY_ORDER_AND_NAME =
            Comparator.comparingInt((ValidatorDescriptor<?> d) -> d.order())
                    .thenComparing(d -> d.validatorName());

    private final Map<String, Map<Class<?>, List<ValidatorDescriptor<?>>>> registry = new ConcurrentHashMap<>();

    ValidatorStore(Collection<? extends Validator<?>> validators) {
        validators.stream()
                .map(ValidatorDescriptor::from)
                .forEach(this::register);
    }

    private void register(ValidatorDescriptor<?> descriptor) {
        registry
                .computeIfAbsent(descriptor.sourceId(), ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(descriptor.eventType(), ignored -> new ArrayList<>())
                .add(descriptor);

        registry.get(descriptor.sourceId())
                .get(descriptor.eventType())
                .sort(BY_ORDER_AND_NAME);
    }

    Optional<Class<?>> resolveClass(String sourceId) {
        Map<Class<?>, List<ValidatorDescriptor<?>>> byClass = registry.get(sourceId);
        if (byClass == null || byClass.isEmpty()) {
            return Optional.empty();
        }
        return byClass.keySet().stream().findFirst();
    }

    boolean hasValidatorsFor(String sourceId, Class<?> eventType) {
        return registry.getOrDefault(sourceId, Collections.emptyMap()).containsKey(eventType);
    }

    int countValidatorsFor(String sourceId, Class<?> eventType) {
        return descriptorsFor(sourceId, eventType).size();
    }

    List<ValidatorDescriptor<?>> descriptorsFor(String sourceId, Class<?> eventType) {
        return registry
                .getOrDefault(sourceId, Collections.emptyMap())
                .getOrDefault(eventType, Collections.emptyList());
    }
}