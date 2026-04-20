package software.spool.validator.engine.fixture;

import software.spool.validator.api.Severity;
import software.spool.validator.api.Validate;
import software.spool.validator.api.Validator;

@Validate(value = Order.class, sourceId = "order-src", severity = Severity.WARNING)
public class AmountWarningValidator implements Validator<Order> {
    @Override
    public boolean validate(Order order) {
        return order.amount() >= 10;
    }

    @Override
    public String failedField() {
        return "amount";
    }

    @Override
    public String failMessage(Order order) {
        return "Amount below minimum: " + order.amount();
    }
}