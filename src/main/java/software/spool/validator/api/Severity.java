package software.spool.validator.api;

/**
 * Defines the impact level of a validator failure.
 *
 * <p>CRITICAL → the event must be rejected or quarantined.
 * <p>WARNING  → the event can continue but the violation is recorded.
 */
public enum Severity {
    CRITICAL,
    WARNING
}
