package io.bokun.inventory.plugin.harness;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import javax.annotation.*;

import com.google.common.collect.*;
import com.google.gson.*;
import com.squareup.okhttp.*;
import io.bokun.inventory.plugin.api.rest.*;

import static io.bokun.inventory.plugin.harness.RestUtil.*;

/**
 * Makes a "shallow" call to receive availabilities of given products. Uses REST for transport.
 *
 * @author Mindaugas Žakšauskas
 */
public class RestShallowAvailabilityAction implements Action {

    public static DateYMD toApiDate(LocalDate in) {
        DateYMD out = new DateYMD();
        out.setYear(in.getYear());
        out.setMonth(in.getMonth().getValue());
        out.setDay(in.getDayOfMonth());
        return out;
    }

    @Nonnull
    public Set<String> getAvailableProducts(@Nonnull PluginData pluginData,
                                            @Nonnull Collection<PluginConfigurationParameterValue> pluginConfiguration,
                                            LocalDate from,
                                            LocalDate to,
                                            int requiredCapacity,
                                            Iterable<String> productIds) {

        ProductsAvailabilityRequest shallowAvailabilityRequest = new ProductsAvailabilityRequest();
        shallowAvailabilityRequest.setParameters(new ArrayList<>(pluginConfiguration));
        DatePeriod range = new DatePeriod();
        range.setFrom(toApiDate(from));
        range.setTo(toApiDate(to));
        shallowAvailabilityRequest.setRange(range);
        shallowAvailabilityRequest.setRequiredCapacity((long) requiredCapacity);
        shallowAvailabilityRequest.setExternalProductIds(ImmutableList.copyOf(productIds));

        OkHttpClient httpClient = getHttpClient(pluginData);
        Request request = new Request.Builder()
                .url((pluginData.tls ? "https://" : "http://") + pluginData.url + "/products/getAvailable")
                .post(RequestBody.create(APPLICATION_JSON, new Gson().toJson(shallowAvailabilityRequest)))
                .build();
        List<ProductsAvailabilityResponse> shallowAvailabilityResponse =
                sendHttpRequestAndParseResponseArray(httpClient, request, ProductsAvailabilityResponse.class);

        return shallowAvailabilityResponse.stream()
                .map(ProductsAvailabilityResponse::getProductId)
                .collect(Collectors.toSet());
    }
}
