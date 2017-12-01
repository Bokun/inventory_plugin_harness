package io.bokun.inventory.plugin.harness.validation;

import javax.annotation.*;

import io.bokun.inventory.common.api.grpc.*;

/**
 * Validates if time passed from plugin has valid values.
 *
 * @author Mindaugas Žakšauskas
 */
public final class TimeValidator implements Validator<Time> {

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull Time time) {
        return new ValidationResult.Builder()
                .assertValid(
                        time.getHour(),
                        new Validator<Integer>() {
                            @Nonnull
                            @Override
                            public ValidationResult validate(@Nonnull Integer hour) {
                                if (hour < 0 || hour > 23) {
                                    return new ValidationResult.Builder().addError("Invalid hour: " + hour).build();
                                } else {
                                    return ValidationResult.SUCCESSFUL;
                                }
                            }
                        }
                )
                .assertValid(
                        time.getMinute(),
                        new Validator<Integer>() {
                            @Nonnull
                            @Override
                            public ValidationResult validate(@Nonnull Integer minute) {
                                if (minute < 0 || minute > 59) {
                                    return new ValidationResult.Builder().addError("Invalid minute: " + minute).build();
                                } else {
                                    return ValidationResult.SUCCESSFUL;
                                }
                            }
                        }
                )
                .build();
    }
}
