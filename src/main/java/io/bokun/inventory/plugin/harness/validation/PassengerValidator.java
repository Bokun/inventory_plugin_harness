package io.bokun.inventory.plugin.harness.validation;

import javax.annotation.*;

import io.bokun.inventory.common.api.grpc.*;

/**
 * Validator for {@link Passenger} objects.
 *
 * @author Mindaugas Žakšauskas
 */
public class PassengerValidator implements Validator<Passenger> {

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull Passenger passenger) {
        return new ValidationResult.Builder()
                .assertNotEmpty(passenger.getPricingCategoryId(), "pricingCategoryId")
                .assertNotEmpty(passenger.getContact(), "contact")
                .build();
    }
}
