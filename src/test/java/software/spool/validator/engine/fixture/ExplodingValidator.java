package software.spool.validator.engine.fixture;

import software.spool.validator.api.Validate;
import software.spool.validator.api.Validator;

@Validate(value = Order.class, sourceId = "order-src")
public class ExplodingValidator implements Validator<Order> {
    @Override
    public boolean validate(Order order) {
        throw new RuntimeException("unexpected NPE");
    }

    @Override
    public String failedField() {
        return "internal";
    }
}