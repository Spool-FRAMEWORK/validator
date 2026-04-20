package software.spool.validator.api;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationResultTest {

    @Test
    void shouldBeValidWhenThereAreNoViolations() {
        ValidationResult result = ValidationResult.of(List.of());

        assertThat(result.isValid()).isTrue();
        assertThat(result.isQuarantine()).isFalse();
        assertThat(result.hasWarnings()).isFalse();
        assertThat(result.getOutcome()).isEqualTo(ValidationResult.Outcome.VALID);
        assertThat(result.getViolations()).isEmpty();
    }

    @Test
    void shouldBeQuarantineWhenThereIsACriticalViolation() {
        ValidationException critical = new ValidationException("field", "critical", Severity.CRITICAL);
        ValidationResult result = ValidationResult.of(List.of(critical));

        assertThat(result.isQuarantine()).isTrue();
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasWarnings()).isFalse();
        assertThat(result.getOutcome()).isEqualTo(ValidationResult.Outcome.QUARANTINE);
    }

    @Test
    void shouldBeQuarantineWhenViolationsAreMixed() {
        ValidationException critical = new ValidationException("a", "c", Severity.CRITICAL);
        ValidationException warning = new ValidationException("b", "w", Severity.WARNING);
        ValidationResult result = ValidationResult.of(List.of(warning, critical));

        assertThat(result.isQuarantine()).isTrue();
    }

    @Test
    void shouldBeWarnWhenAllViolationsAreWarnings() {
        ValidationException warning = new ValidationException("field", "soft issue", Severity.WARNING);
        ValidationResult result = ValidationResult.of(List.of(warning));

        assertThat(result.hasWarnings()).isTrue();
        assertThat(result.isValid()).isFalse();
        assertThat(result.isQuarantine()).isFalse();
        assertThat(result.getOutcome()).isEqualTo(ValidationResult.Outcome.WARN);
    }

    @Test
    void shouldExposeImmutableViolationsList() {
        ValidationException warning = new ValidationException("f", "msg", Severity.WARNING);
        ValidationResult result = ValidationResult.of(List.of(warning));

        assertThat(result.getViolations()).hasSize(1);
        assertThatThrownBy(() -> result.getViolations().add(warning))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldContainOutcomeAndCountInToString() {
        ValidationException warning = new ValidationException("x", "y", Severity.WARNING);
        ValidationResult result = ValidationResult.of(List.of(warning));

        assertThat(result.toString()).contains("WARN").contains("1");
    }
}
