package software.spool.validator.engine.fixture;

import software.spool.validator.api.Validate;
import software.spool.validator.api.Validator;

@Validate(value = TestEvent.class, sourceId = "src-a", order = 2)
public class AlwaysFailValidator implements Validator<TestEvent> {
    @Override
    public boolean validate(TestEvent event) {
        return false;
    }
}