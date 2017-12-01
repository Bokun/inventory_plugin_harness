package io.bokun.inventory.plugin.harness.validation;

import javax.annotation.*;

import io.bokun.inventory.plugin.api.grpc.*;

/**
 * Validates plugin definition data which is served by a plugin.
 *
 * @author Mindaugas Žakšauskas
 */
public final class PluginDefinitionValidator implements Validator<PluginDefinition> {

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ValidationResult validate(@Nonnull PluginDefinition definition) {
        return new ValidationResult.Builder()
                .assertNotEmpty(definition.getName(), "name")
                .assertNotEmpty(definition.getCapabilitiesList(), "capabilities")
                .assertNotEmpty(definition.getParametersList(), "parameters")
                .build();
    }
}
