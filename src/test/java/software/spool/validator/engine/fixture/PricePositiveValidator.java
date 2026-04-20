package software.spool.validator.engine.fixture;

import software.spool.validator.api.Severity;
import software.spool.validator.api.Validate;
import software.spool.validator.api.Validator;

@Validate(value = Product.class, sourceId = "product-src", severity = Severity.WARNING)
public class PricePositiveValidator implements Validator<Product> {
    @Override
    public boolean validate(Product product) {
        return product.price() > 0;
    }

    @Override
    public String failedField() {
        return "price";
    }

    @Override
    public String failMessage(Product product) {
        return "Price should be positive: " + product.price();
    }
}