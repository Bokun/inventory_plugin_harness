package io.bokun.inventory.plugin.harness.validation;

import java.math.*;

import javax.annotation.*;

/**
 * Ensures given string is a number.
 *
 * @author Mindaugas Žakšauskas
 */
public final class NumberValidator implements Validator<String> {

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull String s) {
        try {
            new BigDecimal(s);
            return ValidationResult.SUCCESSFUL;
        } catch (NumberFormatException nfe) {
            return new ValidationResult.Builder().addError("Is not a number: " + s).build();
        }
    }
}
