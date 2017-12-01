package io.bokun.inventory.plugin.harness.validation;

import java.util.function.*;

import javax.annotation.*;

import com.google.common.base.*;
import io.grpc.*;
import io.grpc.stub.*;
import org.slf4j.*;

/**
 * Marker interface for validator. Validation is done at {@link Consumer#accept(Object)}.
 *
 * @author Mindaugas Žakšauskas
 */
public interface Validator<T> {

    /**
     * Implementors should validate given entity and produce either a collection of failures or successful validation result.
     *
     * @param entity entity to validate.
     * @return either successful or failed validation result.
     */
    @Nonnull
    ValidationResult validate(@Nonnull T entity);

    /**
     * Similar to {@link #validate(Object)} but additionally channels all errors to response observer in case of failure.
     *
     * @param entity entity to validate.
     * @param responseObserver observer to channel validation errors to.
     * @return true if validation succeeded (and there were no errors), otherwise false.
     */
    default boolean validate(@Nonnull T entity, @Nonnull StreamObserver<?> responseObserver) {
        ValidationResult result = validate(entity);
        if (result.success) {
            return true;
        } else {
            Logger log = LoggerFactory.getLogger(getClass());
            log.warn("Validation error(s): {}", result.validationErrors);
            responseObserver.onError(Status.INVALID_ARGUMENT.augmentDescription(Joiner.on('\n').join(result.validationErrors)).asException());
            return false;
        }
    }
}
