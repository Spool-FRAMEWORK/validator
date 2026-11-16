package software.spool.validator.api;

import software.spool.core.adapter.jackson.PayloadDeserializerFactory;
import software.spool.core.adapter.logging.LoggerFactory;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.EventMetadataKey;
import software.spool.core.port.logging.Logger;
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
 *
 * <p>
 * An envelope with the {@code VALIDATION_BYPASS} metadata is valid without running any validator, and a warning with
 * its key and the reason of the bypass is logged. Only resolving its quarantine with a bypass sets that metadata.
 * </p>
 */
public class EnvelopeValidator {
    private final ValidatorRegistry registry;
    private final Logger logger;

    public EnvelopeValidator(ValidatorRegistry registry) {
        this(registry, LoggerFactory.getLogger(EnvelopeValidator.class));
    }

    public EnvelopeValidator(ValidatorRegistry registry, Logger logger) {
        this.registry = registry;
        this.logger = logger;
    }

    public ValidationResult validate(Envelope envelope) {
        Optional<String> bypassReason = Optional.ofNullable(envelope.metadata().get(EventMetadataKey.VALIDATION_BYPASS));
        if (bypassReason.isPresent()) {
            logger.warn("Envelope {} skipped validation because its quarantine was bypassed: {}",
                    envelope.idempotencyKey().value(), bypassReason.get());
            return ValidationResult.of(List.of());
        }
        String sourceId = envelope.metadata().get(EventMetadataKey.SOURCE);
        Optional<Class<?>> payloadType = registry.resolveClass(sourceId);
        if (payloadType.isEmpty() || !registry.hasValidatorsFor(sourceId, payloadType.get())) {
            return ValidationResult.of(List.of());
        }
        return registry.validateAll(sourceId,
                PayloadDeserializerFactory.json().as(payloadType.get()).deserialize(envelope.payload()));
    }
}
