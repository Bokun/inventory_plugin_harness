package io.bokun.inventory.plugin.harness.validation;

import javax.annotation.*;

import com.google.inject.*;
import io.bokun.inventory.common.api.grpc.*;

/**
 * Validates period: if both ranges are specified.
 *
 * @author Mindaugas Žakšauskas
 */
public final class DatePeriodValidator implements Validator<DatePeriod> {

    private final DateValidator dateValidator;

    @Inject
    public DatePeriodValidator(DateValidator dateValidator) {
        this.dateValidator = dateValidator;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull DatePeriod period) {
        return new ValidationResult.Builder()
                .assertNotEmpty(period.getFrom(), "from")
                .assertValid(period.getFrom(), dateValidator)
                .assertNotEmpty(period.getTo(), "to")
                .assertValid(period.getTo(), dateValidator)
                .build();
    }
}
