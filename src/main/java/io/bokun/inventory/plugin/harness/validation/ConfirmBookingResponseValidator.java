package io.bokun.inventory.plugin.harness.validation;

import javax.annotation.*;

import io.bokun.inventory.common.api.grpc.*;

import static io.bokun.inventory.common.api.grpc.ConfirmBookingResponse.BookingResultCase.*;

/**
 * Validates whether booking confirmation returned by the plugin has valid semantics.
 *
 * @author Mindaugas Žakšauskas
 */
public final class ConfirmBookingResponseValidator implements Validator<ConfirmBookingResponse> {

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull ConfirmBookingResponse response) {
        ConfirmBookingResponse.BookingResultCase resultType = response.getBookingResultCase();
        ValidationResult.Builder validationResult = new ValidationResult.Builder()
                .assertNotEmpty(resultType, "bookingResult");
        if (resultType == SUCCESSFULBOOKING) {
            SuccessfulBooking successfulBooking = response.getSuccessfulBooking();
            validationResult = validationResult
                    .assertNotEmpty(successfulBooking.getBookingConfirmationCode(), "successfulBooking.bookingConfirmationCode");
        }
        return validationResult.build();
    }
}
