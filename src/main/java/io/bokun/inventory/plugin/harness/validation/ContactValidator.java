package io.bokun.inventory.plugin.harness.validation;

import javax.annotation.*;

import io.bokun.inventory.common.api.grpc.*;

/**
 * Ensures if mandatory parameters of contact are all set.
 *
 * @author Mindaugas Žakšauskas
 */
public final class ContactValidator implements Validator<Contact> {

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull Contact contact) {
        return new ValidationResult.Builder()
                .assertNotEmpty(contact.getFirstName(), "firstName")
                .assertNotEmpty(contact.getLastName(), "lastName")
                .build();
    }
}
