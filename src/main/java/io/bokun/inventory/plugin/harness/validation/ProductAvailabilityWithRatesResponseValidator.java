package io.bokun.inventory.plugin.harness.validation;

import javax.annotation.*;

import com.google.inject.*;
import io.bokun.inventory.common.api.grpc.*;

/**
 * Validates availability responses which come from a plugin (for particular product deep/narrow availability check).
 *
 * @author Mindaugas Žakšauskas
 */
public final class ProductAvailabilityWithRatesResponseValidator implements Validator<ProductAvailabilityWithRatesResponse> {

    private final DateValidator dateValidator;
    private final TimeValidator timeValidator;
    private final RateWithPriceValidator rateWithPriceValidator;

    @Inject
    public ProductAvailabilityWithRatesResponseValidator(DateValidator dateValidator,
                                                         TimeValidator timeValidator,
                                                         RateWithPriceValidator rateWithPriceValidator) {
        this.dateValidator = dateValidator;
        this.timeValidator = timeValidator;
        this.rateWithPriceValidator = rateWithPriceValidator;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull ProductAvailabilityWithRatesResponse response) {
        return new ValidationResult.Builder()
                .assertValid(response.getCapacity(), new PositiveNumberValidator("capacity"))
                .assertNotEmpty(response.getDate(), "date")
                .assertValid(response.getDate(), dateValidator)
                .assertValidIf(response.getTime() != Time.getDefaultInstance(), response.getTime(), timeValidator)
                .assertValidIf(response.getPickupTime() != Time.getDefaultInstance(), response.getPickupTime(), timeValidator)
                .assertNotEmpty(response.getRatesList(), "rates")
                .assertElementsValid(response.getRatesList(), rateWithPriceValidator, "rates")
                .build();
    }
}
