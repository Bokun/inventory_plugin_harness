package io.bokun.inventory.plugin.harness;

import java.time.*;
import java.util.*;

import javax.annotation.*;

import com.google.common.collect.*;
import com.google.gson.*;
import com.google.inject.*;
import com.squareup.okhttp.*;
import io.bokun.inventory.plugin.api.rest.*;
import io.bokun.inventory.plugin.harness.validation.*;

import static io.bokun.inventory.plugin.harness.GrpcRestMapper.restToGrpc;
import static io.bokun.inventory.plugin.harness.RestShallowAvailabilityAction.*;
import static io.bokun.inventory.plugin.harness.RestUtil.*;
import static io.bokun.inventory.plugin.harness.validation.ValidationUtils.validateOrThrow;

/**
 * Makes a "deep" call to receive availability of given single product. Uses RESTful transport.
 *
 * @author Mindaugas Žakšauskas
 */
public class RestDeepAvailabilityAction implements Action {

    private final ProductAvailabilityWithRatesResponseValidator responseValidator;

    @Inject
    public RestDeepAvailabilityAction(ProductAvailabilityWithRatesResponseValidator responseValidator) {
        this.responseValidator = responseValidator;
    }

    @Nonnull
    public List<ProductAvailabilityWithRatesResponse> getAvailability(@Nonnull PluginData pluginData,
                                                                      @Nonnull Collection<PluginConfigurationParameterValue> pluginConfiguration,
                                                                      @Nonnull LocalDate from,
                                                                      @Nonnull LocalDate to,
                                                                      @Nonnull String productId) {
        ProductAvailabilityRequest deepAvailabilityRequest = new ProductAvailabilityRequest();
        deepAvailabilityRequest.setParameters(Lists.newArrayList(pluginConfiguration));
        DatePeriod range = new DatePeriod();
        range.setFrom(toApiDate(from));
        range.setTo(toApiDate(to));
        deepAvailabilityRequest.setRange(range);
        deepAvailabilityRequest.setProductId(productId);

        OkHttpClient httpClient = getHttpClient(pluginData);
        Request request = new Request.Builder()
                .url((pluginData.tls ? "https://" : "http://") + pluginData.url + "/product/getAvailability")
                .post(RequestBody.create(APPLICATION_JSON, new Gson().toJson(deepAvailabilityRequest)))
                .build();
        List<ProductAvailabilityWithRatesResponse> deepAvailabilityResponse =
                sendHttpRequestAndParseResponseArray(httpClient, request, ProductAvailabilityWithRatesResponse.class);
        deepAvailabilityResponse.forEach(response -> validateOrThrow(restToGrpc(response), responseValidator));
        return deepAvailabilityResponse;
    }
}
