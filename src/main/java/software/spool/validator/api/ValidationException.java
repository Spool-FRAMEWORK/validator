package software.spool.validator.api;

public class ValidationException extends Exception {

    private final String field;
    private final Severity severity;

    public ValidationException(String message) {
        super(message);
        this.field = null;
        this.severity = Severity.CRITICAL;
    }

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
        this.severity = Severity.CRITICAL;
    }

    public ValidationException(String field, String message, Severity severity) {
        super(message);
        this.field = field;
        this.severity = severity;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.field = null;
        this.severity = Severity.CRITICAL;
    }

    public String getField() { return field; }
    public Severity getSeverity() { return severity; }
}
