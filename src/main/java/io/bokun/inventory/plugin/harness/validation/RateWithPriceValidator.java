package io.bokun.inventory.plugin.harness.validation;

import javax.annotation.*;

import com.google.inject.*;
import io.bokun.inventory.common.api.grpc.*;

import static com.google.common.collect.Iterables.*;

/**
 * Validates corectness of {@link RateWithPrice} entity.
 *
 * @author Mindaugas Žakšauskas
 */
public final class RateWithPriceValidator implements Validator<RateWithPrice> {

    private final PriceValidator priceValidator;

    @Inject
    public RateWithPriceValidator(PriceValidator priceValidator) {
        this.priceValidator = priceValidator;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull RateWithPrice rateWithPrice) {
        ValidationResult.Builder builder = new ValidationResult.Builder()
                .assertNotEmpty(rateWithPrice.getRateId(), "rate");
        if (rateWithPrice.getPricingOptionsCase() == RateWithPrice.PricingOptionsCase.PRICEPERPERSON) {
            builder.assertNotEmpty(rateWithPrice.getPricePerPerson().getPricingCategoryWithPriceList(), "pricingCategories")
                    .assertElementsValid(
                            transform(
                                    rateWithPrice.getPricePerPerson().getPricingCategoryWithPriceList(),
                                    PricingCategoryWithPrice::getPrice
                            ),
                            priceValidator,
                            "pricingCategories"
                    );
        }
        return builder.build();
    }
}
