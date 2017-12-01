package io.bokun.inventory.plugin.harness.validation;

import java.time.*;

import javax.annotation.*;

import io.bokun.inventory.common.api.grpc.*;

/**
 * Ensures date parameters (year, month, day) are all set correctly (non-lenient).
 *
 * @author Mindaugas Žakšauskas
 */
public final class DateValidator implements Validator<Date> {

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull Date date) {
        return new ValidationResult.Builder()
                .assertValid(
                        date.getYear(),
                        new Validator<Integer>() {
                            @Nonnull
                            @Override
                            public ValidationResult validate(@Nonnull Integer year) {
                                if (year < 1000 || year > 9999) {
                                    return new ValidationResult.Builder().addError("Invalid year: " + year).build();
                                } else {
                                    return ValidationResult.SUCCESSFUL;
                                }
                            }
                        }
                )
                .assertValid(
                        date.getMonth(),
                        new Validator<Integer>() {
                            @Nonnull
                            @Override
                            public ValidationResult validate(@Nonnull Integer month) {
                                if (month < 1 || month > 12) {
                                    return new ValidationResult.Builder().addError("Invalid month: " + month).build();
                                } else {
                                    return ValidationResult.SUCCESSFUL;
                                }
                            }
                        }
                )
                .assertValid(
                        date.getDay(),
                        new Validator<Integer>() {
                            @Nonnull
                            @Override
                            public ValidationResult validate(@Nonnull Integer day) {
                                if (day < 1 || day > LocalDate.of(date.getYear(), date.getMonth(), 1).lengthOfMonth()) {
                                    return new ValidationResult.Builder().addError("Invalid day: " + day).build();
                                } else {
                                    return ValidationResult.SUCCESSFUL;
                                }
                            }
                        }
                )
                .build();
    }
}
