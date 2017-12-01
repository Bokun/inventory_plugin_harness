package io.bokun.inventory.plugin.harness.validation;

import javax.annotation.*;

import io.bokun.inventory.common.api.grpc.*;

/**
 * Checks correctness of {@link PricingCategory}.
 *
 * @author Mindaugas Žakšauskas
 */
public final class PricingCategoryValidator implements Validator<PricingCategory> {

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull PricingCategory rate) {
        return new ValidationResult.Builder()
                .assertNotEmpty(rate.getId(), "pricingCategory.id")
                .assertNotEmpty(rate.getLabel(), "pricingCategory.label")
                .build();
    }
}
