package io.bokun.inventory.plugin.harness.validation;

import javax.annotation.*;

import io.bokun.inventory.common.api.grpc.*;

/**
 * Checks correctness of {@link Rate}.
 *
 * @author Mindaugas Žakšauskas
 */
public final class RateValidator implements Validator<Rate> {

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull Rate rate) {
        return new ValidationResult.Builder()
                .assertNotEmpty(rate.getId(), "rate.id")
                .assertNotEmpty(rate.getLabel(), "rate.label")
                .build();
    }
}
