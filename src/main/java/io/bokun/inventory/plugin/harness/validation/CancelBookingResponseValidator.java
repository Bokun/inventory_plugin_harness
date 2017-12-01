package io.bokun.inventory.plugin.harness.validation;

import javax.annotation.*;

import io.bokun.inventory.common.api.grpc.*;

/**
 * Validates whether booking cancellation returned by the plugin has valid semantics.
 *
 * @author Mindaugas Žakšauskas
 */
public final class CancelBookingResponseValidator implements Validator<CancelBookingResponse> {

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull CancelBookingResponse response) {
        return new ValidationResult.Builder()
                .assertNotEmpty(response.getCancellationResultCase(), "cancellationResult")
                .build();
    }
}
