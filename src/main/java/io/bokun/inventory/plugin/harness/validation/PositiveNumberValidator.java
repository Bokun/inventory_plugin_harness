package io.bokun.inventory.plugin.harness.validation;

import java.math.*;

import javax.annotation.*;

/**
 * Ensures given number is positive (greater than zero).
 *
 * @author Mindaugas Žakšauskas
 */
public final class PositiveNumberValidator implements Validator<Number> {

    @Nonnull
    private final String subject;

    public PositiveNumberValidator(@Nonnull String subject) {
        this.subject = subject;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull Number number) {
        if (new BigDecimal(number.toString()).compareTo(BigDecimal.ZERO) == 1) {
            return ValidationResult.SUCCESSFUL;
        } else {
            return new ValidationResult.Builder()
                    .addError(String.format("%s must be positive number", subject))
                    .build();
        }
    }
}
