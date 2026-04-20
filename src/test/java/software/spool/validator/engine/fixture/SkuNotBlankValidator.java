package software.spool.validator.engine.fixture;

import software.spool.validator.api.Validate;
import software.spool.validator.api.Validator;

@Validate(value = Product.class, sourceId = "product-src")
public class SkuNotBlankValidator implements Validator<Product> {
    @Override
    public boolean validate(Product product) {
        return product.sku() != null && !product.sku().isBlank();
    }

    @Override
    public String failedField() {
        return "sku";
    }

    @Override
    public String failMessage(Product product) {
        return "SKU must not be blank";
    }
}