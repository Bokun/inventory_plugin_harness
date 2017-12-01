package io.bokun.inventory.plugin.harness.validation;

import java.util.*;

import org.slf4j.*;

/**
 * TODO changeme
 *
 * @author Mindaugas Žakšauskas
 */
public class ValidationUtils {

    private static final Logger log = LoggerFactory.getLogger(ValidationUtils.class);

    private ValidationUtils() {
    }

    public static <T> void validateOrThrow(T entity, Validator<T> validator) {
        ValidationResult validationResult = validator.validate(entity);
        if (!validationResult.success) {
            log.error("Validation error(s): {}", validationResult.validationErrors);
            throw new IllegalStateException("Validator error(s): " + validationResult.validationErrors);
        }
    }

    public static <T> void validateOrThrow(Collection<T> entities, Validator<T> validator) {
        for (T entity : entities) {
            ValidationResult validationResult = validator.validate(entity);
            if (!validationResult.success) {
                log.error("Validation error(s): {}", validationResult.validationErrors);
                throw new IllegalStateException("Validator error(s): " + validationResult.validationErrors);
            }
        }
    }
}
