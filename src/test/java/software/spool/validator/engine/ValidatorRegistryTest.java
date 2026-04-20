package software.spool.validator.engine;

import org.junit.jupiter.api.Test;
import software.spool.validator.api.ValidationException;
import software.spool.validator.engine.fixture.PricePositiveValidator;
import software.spool.validator.engine.fixture.Product;
import software.spool.validator.engine.fixture.SkuNotBlankValidator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

class ValidatorRegistryTest {

    private ValidatorRegistry registry() {
        return new ValidatorRegistry(List.of(new SkuNotBlankValidator(), new PricePositiveValidator()));
    }

    @Test
    void shouldRecogniseRegisteredSource() {
        assertThat(registry().hasValidatorsFor("product-src", Product.class)).isTrue();
    }

    @Test
    void shouldReturnFalseForUnknownSource() {
        assertThat(registry().hasValidatorsFor("ghost", Product.class)).isFalse();
    }

    @Test
    void shouldCountTwoValidators() {
        assertThat(registry().countValidatorsFor("product-src", Product.class)).isEqualTo(2);
    }

    @Test
    void shouldResolveProductClass() {
        assertThat(registry().resolveClass("product-src")).contains(Product.class);
    }

    @Test
    void shouldPassForValidProduct() {
        assertThatNoException().isThrownBy(() -> registry().validate("product-src", new Product("ABC-1", 9.99)));
    }

    @Test
    void shouldThrowForBlankSku() {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> registry().validate("product-src", new Product("", 9.99)));
    }

    @Test
    void shouldReturnValidWhenAllValidatorsPass() {
        assertThat(registry().validateAll("product-src", new Product("ABC-1", 9.99)).isValid()).isTrue();
    }

    @Test
    void shouldReturnQuarantineWhenSkuIsBlank() {
        assertThat(registry().validateAll("product-src", new Product("  ", 9.99)).isQuarantine()).isTrue();
    }

    @Test
    void shouldReturnWarnWhenOnlyPriceFails() {
        assertThat(registry().validateAll("product-src", new Product("SKU-1", 0.0)).hasWarnings()).isTrue();
    }

    @Test
    void shouldCollectBothViolations() {
        assertThat(registry().validateAll("product-src", new Product("", -1.0)).getViolations()).hasSize(2);
    }
}
