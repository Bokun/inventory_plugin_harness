package io.bokun.inventory.plugin.harness.validation;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.Function;

import javax.annotation.*;

import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.protobuf.*;

import static com.google.common.collect.Iterables.*;

/**
 * Result of a validation.
 *
 * @see Validator#validate(Object)
 *
 * @author Mindaugas Žakšauskas
 */
public final class ValidationResult {
    public static final ValidationResult SUCCESSFUL = new ValidationResult();

    public final boolean success;

    public final ImmutableCollection<String> validationErrors;

    private ValidationResult() {
        this.success = true;
        this.validationErrors = ImmutableSet.of();
    }

    private ValidationResult(@Nonnull Iterable<String> errors) {
        this.validationErrors = ImmutableSet.copyOf(errors);
        this.success = false;
    }

    public static final class Builder {
        private static final Set<String> UNDEFINED_ENUMS = ImmutableSet.of("UNRECOGNIZED", "UNDEFINED");
        private final AtomicBoolean hasErrors = new AtomicBoolean(false);
        private final java.util.function.Supplier<Set<String>> errors = Suppliers.memoize(() -> {
            hasErrors.set(true);
            return new HashSet<>();
        });

        public Builder assertNotEmpty(@Nullable String s, @Nonnull String field) {
            if (Strings.isNullOrEmpty(s)) {
                addError(String.format("Field %s is empty or missing", field));
            }
            return this;
        }

        public Builder assertNotEmpty(@Nullable GeneratedMessageV3 object, @Nonnull String field) {
            if (object == null) {
                addError(String.format("Field %s is empty or missing", field));
            }
            return this;
        }

        public <T> Builder assertValidIf(boolean condition, @Nonnull T entity, @Nonnull Validator<T> validator) {
            if (condition) {
                return assertValid(entity, validator);
            } else {
                return this;
            }
        }

        public <T> Builder assertValid(@Nonnull T entity, @Nonnull Validator<T> validator) {
            ValidationResult result = validator.validate(entity);
            if (!result.success) {
                addErrors(result.validationErrors);
            }
            return this;
        }

        // only validates with second validator if first validator succeeded
        public <T1, T2> Builder assertValidChain(@Nonnull T1 entity,
                                                 @Nonnull Validator<T1> validator1,
                                                 @Nonnull Function<T1, T2> function,
                                                 @Nonnull Validator<T2> validator2) {
            ValidationResult result1 = validator1.validate(entity);
            if (result1.success) {
                ValidationResult result2 = validator2.validate(function.apply(entity));
                if (!result2.success) {
                    addErrors(result2.validationErrors);
                }
            } else {
                addErrors(result1.validationErrors);
            }
            return this;
        }

        public Builder assertNotEmpty(@Nullable Iterable<?> iterable, @Nonnull String field) {
            if ((iterable == null) || isEmpty(iterable)) {
                addError(String.format("Field %s is empty or missing", field));
            }
            return this;
        }

        public <T> Builder assertElementsValid(@Nonnull Iterable<T> elements, @Nonnull Validator<T> validator, @Nonnull String field) {
            for (T t : elements) {
                ValidationResult result = validator.validate(t);
                if (!result.success) {
                    addErrors(result.validationErrors);
                }
            }
            return this;
        }

        public Builder assertNotEmptyIf(boolean condition, @Nullable Iterable<?> iterable, @Nonnull String field) {
            if (condition) {
                return assertNotEmpty(iterable, field);
            } else {
                return this;
            }
        }

        public Builder assertNotEmptyIf(boolean condition, @Nullable Internal.EnumLite enumLite, @Nonnull String field) {
            if (condition) {
                return assertNotEmpty(enumLite, field);
            } else {
                return this;
            }
        }

        public Builder assertNotEmpty(@Nullable Internal.EnumLite enumLite, @Nonnull String field) {
            if (enumLite == null || enumLite.getNumber() == 0) {
                addError(String.format("Field %s is empty or missing", field));
            }
            return this;
        }

        public Builder addErrors(@Nonnull Collection<String> errorStrings) {
            this.errors.get().addAll(errorStrings);
            return this;
        }

        public Builder addError(@Nonnull String errorString) {
            this.errors.get().add(errorString);
            return this;
        }

        public boolean hasErrors() {
            return hasErrors.get();
        }

        public ValidationResult build() {
            if (!hasErrors.get()) {
                return SUCCESSFUL;
            } else {
                return new ValidationResult(errors.get());
            }
        }
    }
}
