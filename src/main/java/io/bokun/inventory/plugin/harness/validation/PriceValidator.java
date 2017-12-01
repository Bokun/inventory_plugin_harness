package io.bokun.inventory.plugin.harness.validation;

import java.math.*;
import java.util.*;

import javax.annotation.*;

import com.google.common.collect.*;
import com.google.inject.*;
import io.bokun.inventory.common.api.grpc.*;

/**
 * Validates price: if currency symbol is set/supported and if amount is set.
 *
 * @author Mindaugas Žakšauskas
 */
public final class PriceValidator implements Validator<Price> {

    private final ImmutableSet<String> validCurrencyCodes = Currency.getAvailableCurrencies().stream()
            .map(Currency::getCurrencyCode)
            .collect(ImmutableSet.toImmutableSet());

    private final NumberValidator numberValidator;

    @Inject
    public PriceValidator(NumberValidator numberValidator) {
        this.numberValidator = numberValidator;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull Price price) {
        return new ValidationResult.Builder()
                .assertNotEmpty(price.getCurrency(), "currency")
                .assertValid(price.getCurrency(), new Validator<String>() {
                    @Nonnull
                    @Override
                    public ValidationResult validate(@Nonnull String currency) {
                        if (validCurrencyCodes.contains(currency)) {
                            return ValidationResult.SUCCESSFUL;
                        } else {
                            return new ValidationResult.Builder().addError("Unsupported currency code: " + currency).build();
                        }
                    }
                })
                .assertNotEmpty(price.getAmount(), "amount")
                .assertValidChain(
                        price.getAmount(), numberValidator,
                        BigDecimal::new, new NonNegativeNumberValidator("amount")
                )
                .build();
    }
}
