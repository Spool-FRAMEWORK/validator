package software.spool.validator.engine.fixture;

import software.spool.validator.api.Validate;
import software.spool.validator.api.Validator;

@Validate(value = Order.class, sourceId = "order-src")
public class CurrencyValidator implements Validator<Order> {
    @Override
    public boolean validate(Order order) {
        return "EUR".equals(order.currency());
    }

    @Override
    public String failedField() {
        return "currency";
    }

    @Override
    public String failMessage(Order order) {
        return "Currency must be EUR, got: " + order.currency();
    }
}