package io.bokun.inventory.plugin.harness;

import java.util.*;

import javax.annotation.*;

import com.google.common.collect.*;
import io.bokun.inventory.common.api.grpc.*;
import io.bokun.inventory.plugin.api.grpc.*;
import org.slf4j.*;

import static com.google.common.base.Strings.*;

/**
 * Inspects env var options in order to match required configuration options.
 *
 * @author Mindaugas Žakšauskas
 */
public class GrpcConfigurePluginAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(GrpcConfigurePluginAction.class);

    public static final String PLUGIN_CONFIG_PREFIX = "PLUGIN_CONFIG_";

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean valueCanBeParsed(PluginParameterDataType type, String key, String value) {
        try {
            switch (type) {
                case STRING: break;
                case BOOLEAN: Boolean.parseBoolean(value); break;
                case DOUBLE: Double.parseDouble(value); break;
                case LONG: Long.parseLong(value); break;
                case UNASSIGNED_PLUGIN_PARAMETER_DATA_TYPE:
                case UNRECOGNIZED:
                default: {
                    log.error("Unsupported/unknown/unset plugin configuration type: {}", type);
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException nfe) {
            log.error("Unparseable value for {}: {}", key, value);
            return false;
        }
    }

    @Nonnull
    public Collection<PluginConfigurationParameterValue> getPluginConfigurationParameterValues(@Nonnull PluginDefinition pluginDefinition) {
        ImmutableList.Builder<PluginConfigurationParameterValue> result = new ImmutableList.Builder<>();
        Map<String, String> environment = System.getenv();
        for (io.bokun.inventory.plugin.api.grpc.PluginConfigurationParameter parameter : pluginDefinition.getParametersList()) {
            String envVarName = PLUGIN_CONFIG_PREFIX + parameter.getName();
            String envVarValue = environment.get(envVarName);
            if (parameter.getRequired() && isNullOrEmpty(envVarValue)) {
                throw new IllegalStateException(envVarName + " environment variable is missing");
            }
            if (!isNullOrEmpty(envVarValue) &&  !valueCanBeParsed(parameter.getType(), envVarName, envVarValue)) {
                throw new IllegalStateException("Unparseable value");
            }
            result.add(
                    PluginConfigurationParameterValue.newBuilder()
                                            .setName(envVarName.substring(PLUGIN_CONFIG_PREFIX.length()))
                                            .setValue(envVarValue)
                                            .build()
            );
        }
        return result.build();
    }
}
