package io.bokun.inventory.plugin.harness.validation;

import javax.annotation.*;

import io.bokun.inventory.common.api.grpc.*;

import static io.bokun.inventory.common.api.grpc.ReservationResponse.ReservationResultCase.*;

/**
 * Validates whether reservation returned by the plugin has valid semantics.
 *
 * @author Mindaugas Žakšauskas
 */
public final class ReservationResponseValidator implements Validator<ReservationResponse> {

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull ReservationResponse response) {
        ReservationResponse.ReservationResultCase resultType = response.getReservationResultCase();
        ValidationResult.Builder validationResult = new ValidationResult.Builder()
                .assertNotEmpty(resultType, "reservationResult");
        if (resultType == SUCCESSFULRESERVATION) {
            SuccessfulReservation successfulReservation = response.getSuccessfulReservation();
            validationResult = validationResult
                    .assertNotEmpty(successfulReservation.getReservationConfirmationCode(), "successfulReservation.reservationConfirmationCode");
        }
        return validationResult.build();
    }
}
