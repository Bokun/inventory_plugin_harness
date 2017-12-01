package io.bokun.inventory.plugin.harness.validation;

import javax.annotation.*;

import com.google.inject.*;
import io.bokun.inventory.common.api.grpc.*;

/**
 * Ensures {@link BasicProductInfo}, returned from plugin is valid - necessary fields set and contain correct elements.
 *
 * @author Mindaugas Žakšauskas
 */
public final class BasicProductInfoValidator implements Validator<BasicProductInfo> {

    private final PricingCategoryValidator pricingCategoryValidator;

    @Inject
    public BasicProductInfoValidator(PricingCategoryValidator pricingCategoryValidator) {
        this.pricingCategoryValidator = pricingCategoryValidator;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull BasicProductInfo info) {
        return new ValidationResult.Builder()
                .assertNotEmpty(info.getId(), "id")
                .assertNotEmpty(info.getName(), "name")
                .assertElementsValid(info.getPricingCategoriesList(), pricingCategoryValidator, "pricingCategories")
                .build();
    }
}
