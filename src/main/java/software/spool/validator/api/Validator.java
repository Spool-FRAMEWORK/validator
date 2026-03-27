package software.spool.validator.api;

public interface Validator<T> {

    boolean validate(T object);

    /**
     * Called only when validate() returns false.
     * Receives the failing object so semantic messages can be built.
     * Example: "Currency 'USD' not accepted, expected EUR for region ES"
     */
    default String failMessage(T object) {
        return "Validation failed in " + getClass().getSimpleName();
    }

    default String failedField() {
        return null;
    }
}
