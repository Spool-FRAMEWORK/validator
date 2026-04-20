package software.spool.validator.engine;

import org.junit.jupiter.api.Test;
import software.spool.validator.engine.fixture.AlwaysFailValidator;
import software.spool.validator.engine.fixture.AlwaysPassValidator;
import software.spool.validator.engine.fixture.NoAnnotationValidator;
import software.spool.validator.engine.fixture.TestEvent;
import software.spool.validator.engine.fixture.WarnValidator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ValidatorStoreTest {

    @Test
    void shouldRecogniseRegisteredSourceId() {
        ValidatorStore store = new ValidatorStore(List.of(new AlwaysPassValidator()));

        assertThat(store.hasValidatorsFor("src-a", TestEvent.class)).isTrue();
    }

    @Test
    void shouldReturnFalseForUnknownSourceId() {
        ValidatorStore store = new ValidatorStore(List.of(new AlwaysPassValidator()));

        assertThat(store.hasValidatorsFor("unknown", TestEvent.class)).isFalse();
    }

    @Test
    void shouldThrowWhenValidatorHasNoAnnotation() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ValidatorStore(List.of(new NoAnnotationValidator())))
                .withMessageContaining("@Validate");
    }

    @Test
    void shouldCountRegisteredValidators() {
        ValidatorStore store = new ValidatorStore(List.of(new AlwaysPassValidator(), new AlwaysFailValidator()));

        assertThat(store.countValidatorsFor("src-a", TestEvent.class)).isEqualTo(2);
    }

    @Test
    void shouldReturnZeroForUnknownSourceIdWhenCounting() {
        ValidatorStore store = new ValidatorStore(List.of(new AlwaysPassValidator()));

        assertThat(store.countValidatorsFor("no-such-src", TestEvent.class)).isEqualTo(0);
    }

    @Test
    void shouldSortValidatorsByOrder() {
        ValidatorStore store = new ValidatorStore(List.of(new AlwaysFailValidator(), new AlwaysPassValidator()));

        assertThat(store.validatorsFor("src-a", TestEvent.class).get(0)).isInstanceOf(AlwaysPassValidator.class);
        assertThat(store.validatorsFor("src-a", TestEvent.class).get(1)).isInstanceOf(AlwaysFailValidator.class);
    }

    @Test
    void shouldResolveKnownSourceClass() {
        ValidatorStore store = new ValidatorStore(List.of(new AlwaysPassValidator()));

        assertThat(store.resolveClass("src-a")).contains(TestEvent.class);
    }

    @Test
    void shouldReturnEmptyOptionalForUnknownSourceId() {
        ValidatorStore store = new ValidatorStore(List.of(new AlwaysPassValidator()));

        assertThat(store.resolveClass("ghost")).isEmpty();
    }

    @Test
    void shouldIsolateValidatorsBySourceId() {
        ValidatorStore store = new ValidatorStore(List.of(new AlwaysPassValidator(), new WarnValidator()));

        assertThat(store.hasValidatorsFor("src-a", TestEvent.class)).isTrue();
        assertThat(store.hasValidatorsFor("src-b", TestEvent.class)).isTrue();
        assertThat(store.countValidatorsFor("src-a", TestEvent.class)).isEqualTo(1);
        assertThat(store.countValidatorsFor("src-b", TestEvent.class)).isEqualTo(1);
    }
}
