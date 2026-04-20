package software.spool.validator.engine;

import software.spool.validator.api.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

final class ValidatorLoader {

    private ValidatorLoader() {
    }

    static List<Validator<?>> load(ClassLoader classLoader) {
        List<Validator<?>> validators = new ArrayList<>();

        ServiceLoader.load(rawValidatorType(), classLoader)
                .stream()
                .map(ServiceLoader.Provider::get)
                .map(ValidatorLoader::toWildcardValidator)
                .forEach(validators::add);

        return List.copyOf(validators);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Class<Validator> rawValidatorType() {
        return (Class<Validator>) (Class) Validator.class;
    }

    private static Validator<?> toWildcardValidator(Validator validator) {
        return validator;
    }
}