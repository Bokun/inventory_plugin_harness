package io.bokun.inventory.plugin.harness.validation;

import java.util.*;

import javax.annotation.*;

import com.google.common.base.*;
import com.google.common.collect.*;

/**
 * Validates if country code is two letters (once set).
 *
 * @author Mindaugas Žakšauskas
 */
public final class CountryCodeValidator implements Validator<String> {

    private final Set<String> validCodes = ImmutableSet.copyOf(Locale.getISOCountries());

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull String countryCode) {
        if (!Strings.isNullOrEmpty(countryCode)) {
            if (countryCode.length() != 2) {
                return new ValidationResult.Builder()
                        .addError("Country code should be 2 letters if specified")
                        .build();
            }
            if (!validCodes.contains(countryCode)) {
                return new ValidationResult.Builder()
                        .addError("Unsupported country code: " + countryCode)
                        .build();
            }
        }
        return ValidationResult.SUCCESSFUL;
    }
}
