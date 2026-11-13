package software.spool.validator.api;

import org.junit.jupiter.api.Test;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.EventMetadata;
import software.spool.core.model.vo.EventMetadataKey;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.MediaType;
import software.spool.validator.engine.ValidatorRegistry;
import software.spool.validator.engine.fixture.PricePositiveValidator;
import software.spool.validator.engine.fixture.SkuNotBlankValidator;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnvelopeValidatorTest {

    private final EnvelopeValidator validator = new EnvelopeValidator(
            new ValidatorRegistry(List.of(new SkuNotBlankValidator(), new PricePositiveValidator())));

    @Test
    void validate_payloadThatFailsACriticalValidator_isQuarantineWithItsViolation() {
        ValidationResult result = validator.validate(envelope("product-src", "{\"sku\":\"\",\"price\":10}"));

        assertThat(result.isQuarantine()).isTrue();
        assertThat(result.getViolations()).extracting(Throwable::getMessage).anyMatch(message -> message.contains("SKU must not be blank"));
    }

    @Test
    void validate_payloadThatOnlyFailsAWarning_isNotQuarantine() {
        ValidationResult result = validator.validate(envelope("product-src", "{\"sku\":\"A-1\",\"price\":-1}"));

        assertThat(result.isQuarantine()).isFalse();
        assertThat(result.hasWarnings()).isTrue();
    }

    @Test
    void validate_validPayload_isValid() {
        assertThat(validator.validate(envelope("product-src", "{\"sku\":\"A-1\",\"price\":10}")).isValid()).isTrue();
    }

    @Test
    void validate_sourceWithoutValidators_isValid() {
        assertThat(validator.validate(envelope("other-src", "{\"anything\":true}")).isValid()).isTrue();
    }

    @Test
    void validate_envelopeWithoutSource_isValid() {
        Envelope withoutSource = new Envelope(IdempotencyKey.of("k1"), new EventMetadata(), MediaType.of("application/json"),
                "{}".getBytes(StandardCharsets.UTF_8), EnvelopeStatus.CAPTURED, 0, Instant.now(), Instant.now());

        assertThat(validator.validate(withoutSource).isValid()).isTrue();
    }

    private static Envelope envelope(String source, String payload) {
        return new Envelope(IdempotencyKey.of("k1"), new EventMetadata().set(EventMetadataKey.SOURCE, source),
                MediaType.of("application/json"), payload.getBytes(StandardCharsets.UTF_8), EnvelopeStatus.CAPTURED, 0,
                Instant.now(), Instant.now());
    }
}
