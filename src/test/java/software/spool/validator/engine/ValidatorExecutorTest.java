package software.spool.validator.engine;

import org.junit.jupiter.api.Test;
import software.spool.validator.api.Severity;
import software.spool.validator.api.ValidationException;
import software.spool.validator.api.Validator;
import software.spool.validator.engine.fixture.AmountWarningValidator;
import software.spool.validator.engine.fixture.CurrencyValidator;
import software.spool.validator.engine.fixture.ExplodingValidator;
import software.spool.validator.engine.fixture.Order;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

class ValidatorExecutorTest {

    private ValidatorExecutor executorWith(Validator<?>... validators) {
        return new ValidatorExecutor(new ValidatorStore(List.of(validators)));
    }

    @Test
    void shouldNotThrowWhenAllValidatorsPass() {
        ValidatorExecutor executor = executorWith(new CurrencyValidator());

        assertThatNoException().isThrownBy(() -> executor.validate("order-src", new Order("EUR", 100)));
    }

    @Test
    void shouldThrowValidationExceptionOnFirstFailure() {
        ValidatorExecutor executor = executorWith(new CurrencyValidator());

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> executor.validate("order-src", new Order("USD", 100)))
                .withMessageContaining("EUR");
    }

    @Test
    void shouldThrowWhenEventIsNull() {
        ValidatorExecutor executor = executorWith(new CurrencyValidator());

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> executor.validate("order-src", null))
                .withMessageContaining("null");
    }

    @Test
    void shouldWrapUnexpectedExceptionsInFailFastMode() {
        ValidatorExecutor executor = executorWith(new ExplodingValidator());

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> executor.validate("order-src", new Order("EUR", 50)))
                .withMessageContaining("Unexpected error");
    }

    @Test
    void shouldReturnValidResultWhenAllValidatorsPass() {
        ValidatorExecutor executor = executorWith(new CurrencyValidator(), new AmountWarningValidator());

        assertThat(executor.validateAll("order-src", new Order("EUR", 50)).isValid()).isTrue();
        assertThat(executor.validateAll("order-src", new Order("EUR", 50)).getViolations()).isEmpty();
    }

    @Test
    void shouldReturnQuarantineWhenCriticalValidatorFails() {
        ValidatorExecutor executor = executorWith(new CurrencyValidator());

        assertThat(executor.validateAll("order-src", new Order("USD", 100)).isQuarantine()).isTrue();
        assertThat(executor.validateAll("order-src", new Order("USD", 100)).getViolations()).hasSize(1);
    }

    @Test
    void shouldReturnWarnWhenOnlyWarningValidatorsFail() {
        ValidatorExecutor executor = executorWith(new AmountWarningValidator());

        assertThat(executor.validateAll("order-src", new Order("EUR", 5)).hasWarnings()).isTrue();
        assertThat(executor.validateAll("order-src", new Order("EUR", 5)).getViolations())
                .extracting(ValidationException::getSeverity)
                .containsOnly(Severity.WARNING);
    }

    @Test
    void shouldCollectAllViolations() {
        ValidatorExecutor executor = executorWith(new CurrencyValidator(), new AmountWarningValidator());

        assertThat(executor.validateAll("order-src", new Order("USD", 5)).getViolations()).hasSize(2);
    }

    @Test
    void shouldReturnQuarantineWhenEventIsNullInCollectMode() {
        ValidatorExecutor executor = executorWith(new CurrencyValidator());

        assertThat(executor.validateAll("order-src", null).isQuarantine()).isTrue();
        assertThat(executor.validateAll("order-src", null).getViolations()).isNotEmpty();
    }

    @Test
    void shouldWrapUnexpectedExceptionsAsViolations() {
        ValidatorExecutor executor = executorWith(new ExplodingValidator());

        assertThat(executor.validateAll("order-src", new Order("EUR", 50)).isQuarantine()).isTrue();
        assertThat(executor.validateAll("order-src", new Order("EUR", 50)).getViolations())
                .first()
                .extracting(ValidationException::getMessage)
                .asString()
                .contains("Unexpected error");
    }
}
