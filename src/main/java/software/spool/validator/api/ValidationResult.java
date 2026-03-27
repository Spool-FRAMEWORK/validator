package software.spool.validator.api;

import java.util.List;

/**
 * Aggregated result of running all validators against a single event.
 */
public final class ValidationResult {

    public enum Outcome { VALID, QUARANTINE, WARN }

    private final List<ValidationException> violations;
    private final Outcome outcome;

    private ValidationResult(List<ValidationException> violations, Outcome outcome) {
        this.violations = List.copyOf(violations);
        this.outcome = outcome;
    }

    public static ValidationResult of(List<ValidationException> violations) {
        if (violations.isEmpty()) return new ValidationResult(violations, Outcome.VALID);

        boolean hasCritical = violations.stream()
                .anyMatch(e -> e.getSeverity() == Severity.CRITICAL);

        Outcome outcome = hasCritical ? Outcome.QUARANTINE : Outcome.WARN;
        return new ValidationResult(violations, outcome);
    }

    public boolean isValid()       { return outcome == Outcome.VALID; }
    public boolean isQuarantine()  { return outcome == Outcome.QUARANTINE; }
    public boolean hasWarnings()   { return outcome == Outcome.WARN; }
    public Outcome getOutcome()    { return outcome; }
    public List<ValidationException> getViolations() { return violations; }

    @Override
    public String toString() {
        return "ValidationResult{outcome=" + outcome + ", violations=" + violations.size() + "}";
    }
}
