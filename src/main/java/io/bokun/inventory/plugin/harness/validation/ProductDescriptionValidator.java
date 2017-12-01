package io.bokun.inventory.plugin.harness.validation;

import javax.annotation.*;

import com.google.inject.*;
import io.bokun.inventory.common.api.grpc.*;

import static io.bokun.inventory.common.api.grpc.BookingType.*;
import static io.bokun.inventory.common.api.grpc.MeetingType.*;
import static io.bokun.inventory.common.api.grpc.TicketSupport.*;

/**
 * Ensures {@link ProductDescription}, returned from plugin is valid - necessary fields set and contain correct elements.
 *
 * @author Mindaugas Žakšauskas
 */
public final class ProductDescriptionValidator implements Validator<ProductDescription> {

    private final CountryCodeValidator countryCodeValidator;
    private final PricingCategoryValidator pricingCategoryValidator;
    private final RateValidator rateValidator;

    @Inject
    public ProductDescriptionValidator(CountryCodeValidator countryCodeValidator,
                                       PricingCategoryValidator pricingCategoryValidator,
                                       RateValidator rateValidator) {
        this.countryCodeValidator = countryCodeValidator;
        this.pricingCategoryValidator = pricingCategoryValidator;
        this.rateValidator = rateValidator;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull ProductDescription productDescription) {
        return new ValidationResult.Builder()
                .assertNotEmpty(productDescription.getId(), "id")
                .assertNotEmpty(productDescription.getName(), "name")
                .assertNotEmpty(productDescription.getPricingCategoriesList(), "pricingCategories")
                .assertElementsValid(productDescription.getPricingCategoriesList(), pricingCategoryValidator, "pricingCategories")
                .assertNotEmpty(productDescription.getRatesList(), "rates")
                .assertElementsValid(productDescription.getRatesList(), rateValidator, "rates")
                .assertNotEmpty(productDescription.getBookingType(), "bookingType")
                .assertNotEmpty(productDescription.getMeetingType(), "meetingType")
                .assertNotEmptyIf(
                        productDescription.getMeetingType() == PICK_UP && !productDescription.getCustomPickupPlaceAllowed(),
                        productDescription.getPickupPlacesList(),
                        "pickupPlaces"
                )
                .assertNotEmptyIf(
                        productDescription.getDropoffAvailable() && !productDescription.getCustomDropoffPlaceAllowed(),
                        productDescription.getPickupPlacesList(),
                        "dropoffPlaces"
                )
                .assertNotEmpty(productDescription.getProductCategory(), "productCategory")
                .assertNotEmpty(productDescription.getTicketSupportList(), "ticketSupport")
                .assertNotEmptyIf(
                        productDescription.getBookingType() == DATE_AND_TIME,
                        productDescription.getStartTimesList(),
                        "startTimes"
                )
                .assertNotEmptyIf(
                        !productDescription.getTicketSupportList().contains(TICKETS_NOT_REQUIRED),      // should specify what kind of ticket required
                        productDescription.getTicketType(),                                             // unless tickets are not required
                        "ticketType"
                )
                .assertElementsValid(productDescription.getCountriesList(), countryCodeValidator, "countries")
                .build();
    }
}
