package software.spool.validator.api;

import software.spool.core.adapter.jackson.PayloadDeserializerFactory;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.EventMetadataKey;
import software.spool.validator.engine.ValidatorRegistry;

import java.util.List;
import java.util.Optional;

/**
 * Validates the payload of an envelope with the validators of its source.
 *
 * <p>
 * The source comes from the envelope's {@code SOURCE} metadata. Its payload is read as JSON into the class the
 * validators of that source are declared for, so each validator receives the domain object and never the envelope.
 * An envelope whose source has no validators is valid.
 * </p>
 */
public class EnvelopeValidator {
    private final ValidatorRegistry registry;

    public EnvelopeValidator(ValidatorRegistry registry) {
        this.registry = registry;
    }

    public ValidationResult validate(Envelope envelope) {
        String sourceId = envelope.metadata().get(EventMetadataKey.SOURCE);
        Optional<Class<?>> payloadType = registry.resolveClass(sourceId);
        if (payloadType.isEmpty() || !registry.hasValidatorsFor(sourceId, payloadType.get())) {
            return ValidationResult.of(List.of());
        }
        return registry.validateAll(sourceId,
                PayloadDeserializerFactory.json().as(payloadType.get()).deserialize(envelope.payload()));
    }
}
