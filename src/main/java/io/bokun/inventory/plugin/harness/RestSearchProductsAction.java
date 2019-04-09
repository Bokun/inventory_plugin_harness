package io.bokun.inventory.plugin.harness;

import java.util.*;

import javax.annotation.*;

import com.google.gson.*;
import com.google.inject.*;
import com.squareup.okhttp.*;
import io.bokun.inventory.plugin.api.rest.*;
import io.bokun.inventory.plugin.harness.validation.*;
import org.slf4j.*;

import static com.google.common.collect.Collections2.*;
import static io.bokun.inventory.plugin.harness.RestUtil.*;
import static io.bokun.inventory.plugin.harness.validation.ValidationUtils.*;

/**
 * Searches for products on the remote API (before mapping is done).
 *
 * @author Mindaugas Žakšauskas
 */
public class RestSearchProductsAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(RestSearchProductsAction.class);

    private final BasicProductInfoValidator basicProductInfoValidator;

    @Inject
    public RestSearchProductsAction(BasicProductInfoValidator basicProductInfoValidator) {
        this.basicProductInfoValidator = basicProductInfoValidator;
    }

    public List<BasicProductInfo> search(@Nonnull PluginData pluginData,
                                         @Nonnull Collection<PluginConfigurationParameterValue> pluginConfiguration) {
        log.info("Searching for inventory products in plugin {}", pluginData.url);

        SearchProductRequest pluginSearchRequest = new SearchProductRequest();
        pluginSearchRequest.getParameters().addAll(pluginConfiguration);

        OkHttpClient httpClient = getHttpClient(pluginData);
        Request request = new Request.Builder()
                .url((pluginData.tls ? "https://" : "http://") + pluginData.url + "/product/search")
                .post(RequestBody.create(APPLICATION_JSON, new Gson().toJson(pluginSearchRequest)))
                .build();
        List<BasicProductInfo> products = sendHttpRequestAndParseResponseArray(httpClient, request, BasicProductInfo.class);
        validateOrThrow(
                transform(
                        products,
                        GrpcRestMapper::restToGrpc
                ),
                basicProductInfoValidator
        );
        return products;
    }
}
