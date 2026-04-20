package software.spool.validator.engine.fixture;

import software.spool.validator.api.Validator;

public class NoAnnotationValidator implements Validator<TestEvent> {
    @Override
    public boolean validate(TestEvent event) {
        return true;
    }
}