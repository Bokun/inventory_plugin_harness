package io.bokun.inventory.plugin.harness;

import java.util.*;

import javax.annotation.*;

import com.google.gson.*;
import com.google.inject.*;
import com.squareup.okhttp.*;
import io.bokun.inventory.plugin.api.rest.*;
import io.bokun.inventory.plugin.harness.validation.*;
import org.slf4j.*;

import static io.bokun.inventory.plugin.harness.RestUtil.*;
import static io.bokun.inventory.plugin.harness.validation.ValidationUtils.validateOrThrow;

/**
 * Searches for products on the remote API (before mapping is done). Uses RESTful API.
 *
 * @author Mindaugas Žakšauskas
 */
public class RestGetProductByIdAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(RestGetProductByIdAction.class);

    private final ProductDescriptionValidator productDescriptionValidator;

    @Inject
    public RestGetProductByIdAction(ProductDescriptionValidator productDescriptionValidator) {
        this.productDescriptionValidator = productDescriptionValidator;
    }

    @Nonnull
    public ProductDescription getProductById(@Nonnull PluginData pluginData,
                                             @Nonnull Collection<PluginConfigurationParameterValue> pluginConfiguration,
                                             String productId) {
        log.info("Searching for inventory products in plugin {}", pluginData.url);

        GetProductByIdRequest getByIdRequest = new GetProductByIdRequest();
        getByIdRequest.getParameters().addAll(pluginConfiguration);
        getByIdRequest.setExternalId(productId);

        OkHttpClient httpClient = getHttpClient(pluginData);
        Request request = new Request.Builder()
                .url((pluginData.tls ? "https://" : "http://") + pluginData.url + "/product/getById")
                .post(RequestBody.create(APPLICATION_JSON, new Gson().toJson(getByIdRequest)))
                .build();
        ProductDescription product = sendHttpRequestAndParseResponse(httpClient, request, ProductDescription.class);
        validateOrThrow(GrpcRestMapper.restToGrpc(product), productDescriptionValidator);
        return product;
    }
}
