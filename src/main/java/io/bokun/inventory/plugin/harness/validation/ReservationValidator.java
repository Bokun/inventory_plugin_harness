package io.bokun.inventory.plugin.harness.validation;

import javax.annotation.*;

import com.google.inject.*;
import io.bokun.inventory.common.api.grpc.*;

/**
 * Ensures fields of given {@link Reservation} are set.
 *
 * @author Mindaugas Žakšauskas
 */
public final class ReservationValidator implements Validator<Reservation> {

    private final PassengerValidator passengerValidator;

    @Inject
    public ReservationValidator(PassengerValidator passengerValidator) {
        this.passengerValidator = passengerValidator;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull Reservation reservation) {
        return new ValidationResult.Builder()
                .assertNotEmpty(reservation.getRateId(), "rateId")
                .assertNotEmpty(reservation.getPassengersList(), "passengers")
                .assertElementsValid(reservation.getPassengersList(), passengerValidator, "passengers")
                .build();
    }
}
