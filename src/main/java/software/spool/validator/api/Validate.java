package software.spool.validator.api;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Validate {
    Class<?> value();
    String sourceId();
    Severity severity() default Severity.CRITICAL;

    /**
     * Execution priority. Lower = runs first.
     * Validators without explicit order (default) run after all ordered ones.
     * Ties are broken alphabetically by class name (deterministic).
     */
    int order() default Integer.MAX_VALUE;
}
