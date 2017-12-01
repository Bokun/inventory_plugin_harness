package io.bokun.inventory.plugin.harness.validation;

import java.math.*;

import javax.annotation.*;

/**
 * Ensures given number is not negative (zero or positive).
 *
 * @author Mindaugas Žakšauskas
 */
public final class NonNegativeNumberValidator implements Validator<Number> {

    @Nonnull
    private final String subject;

    public NonNegativeNumberValidator(@Nonnull String subject) {
        this.subject = subject;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull Number number) {
        if (new BigDecimal(number.toString()).compareTo(BigDecimal.ZERO) == -1) {
            return new ValidationResult.Builder()
                    .addError(String.format("%s must be zero or positive number", subject))
                    .build();
        } else {
            return ValidationResult.SUCCESSFUL;
        }
    }
}
