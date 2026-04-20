package software.spool.validator.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationExceptionTest {

    @Test
    void shouldStoreMessageAndDefaultValues() {
        ValidationException exception = new ValidationException("something went wrong");

        assertThat(exception.getMessage()).isEqualTo("something went wrong");
        assertThat(exception.getSeverity()).isEqualTo(Severity.CRITICAL);
        assertThat(exception.getField()).isNull();
    }

    @Test
    void shouldStoreFieldMessageAndDefaultSeverity() {
        ValidationException exception = new ValidationException("email", "must not be blank");

        assertThat(exception.getField()).isEqualTo("email");
        assertThat(exception.getMessage()).isEqualTo("must not be blank");
        assertThat(exception.getSeverity()).isEqualTo(Severity.CRITICAL);
    }

    @Test
    void shouldStoreFullConstructorArguments() {
        ValidationException exception = new ValidationException("amount", "too low", Severity.WARNING);

        assertThat(exception.getField()).isEqualTo("amount");
        assertThat(exception.getMessage()).isEqualTo("too low");
        assertThat(exception.getSeverity()).isEqualTo(Severity.WARNING);
    }

    @Test
    void shouldStoreCause() {
        RuntimeException cause = new RuntimeException("root");
        ValidationException exception = new ValidationException("wrapped", cause);

        assertThat(exception.getMessage()).isEqualTo("wrapped");
        assertThat(exception.getCause()).isSameAs(cause);
        assertThat(exception.getSeverity()).isEqualTo(Severity.CRITICAL);
        assertThat(exception.getField()).isNull();
    }
}
