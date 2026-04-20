package software.spool.validator.engine.fixture;

import software.spool.validator.api.Severity;
import software.spool.validator.api.Validate;
import software.spool.validator.api.Validator;

@Validate(value = TestEvent.class, sourceId = "src-b", severity = Severity.WARNING)
public class WarnValidator implements Validator<TestEvent> {
    @Override
    public boolean validate(TestEvent event) {
        return true;
    }
}