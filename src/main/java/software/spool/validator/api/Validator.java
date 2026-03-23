package software.spool.validator.api;

public interface Validator<T> {
    boolean validate(T object);
}
