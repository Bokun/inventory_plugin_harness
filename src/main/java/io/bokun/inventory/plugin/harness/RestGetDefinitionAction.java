package io.bokun.inventory.plugin.harness;

import javax.annotation.*;

import com.google.inject.*;
import com.squareup.okhttp.*;
import io.bokun.inventory.plugin.api.rest.*;
import io.bokun.inventory.plugin.harness.validation.*;

import static io.bokun.inventory.plugin.harness.GrpcRestMapper.*;
import static io.bokun.inventory.plugin.harness.RestUtil.*;
import static io.bokun.inventory.plugin.harness.validation.ValidationUtils.*;

/**
 * Gets plugin definition and validates whether returned result is valid. Uses RESTful transport protocol.
 *
 * @author Mindaugas Žakšauskas
 */
public class RestGetDefinitionAction implements Action {

    private final PluginDefinitionValidator pluginDefinitionValidator;

    @Inject
    public RestGetDefinitionAction(PluginDefinitionValidator pluginDefinitionValidator) {
        this.pluginDefinitionValidator = pluginDefinitionValidator;
    }

    @Nonnull
    public PluginDefinition getDefinition(PluginData pluginData) {
        OkHttpClient httpClient = getHttpClient(pluginData);
        Request request = new Request.Builder()
                             .url((pluginData.tls ? "https://" : "http://") + pluginData.url + "/plugin/definition")
                             .build();
        PluginDefinition definition = sendHttpRequestAndParseResponse(httpClient, request, PluginDefinition.class);
        validateOrThrow(restToGrpc(definition), pluginDefinitionValidator);
        return definition;
    }
}
