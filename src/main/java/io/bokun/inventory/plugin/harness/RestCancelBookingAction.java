package io.bokun.inventory.plugin.harness;

import javax.annotation.*;

import com.google.gson.*;
import com.google.inject.*;
import com.squareup.okhttp.*;
import io.bokun.inventory.plugin.api.rest.*;
import io.bokun.inventory.plugin.harness.validation.*;
import org.slf4j.*;

import static io.bokun.inventory.util.GrpcRestMapper.restToGrpc;
import static io.bokun.inventory.plugin.harness.RestUtil.*;
import static io.bokun.inventory.plugin.harness.validation.ValidationUtils.validateOrThrow;

/**
 * Cancels a booking. Uses RESTful transport protocol.
 *
 * @author Mindaugas Žakšauskas
 */
public class RestCancelBookingAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(RestCancelBookingAction.class);

    private final CancelBookingResponseValidator cancelBookingResponseValidator;

    @Inject
    public RestCancelBookingAction(CancelBookingResponseValidator cancelBookingResponseValidator) {
        this.cancelBookingResponseValidator = cancelBookingResponseValidator;
    }

    @Nonnull
    public CancelBookingResponse cancelBooking(@Nonnull PluginData pluginData, @Nonnull CancelBookingRequest cancelBookingRequest) {
        OkHttpClient httpClient = getHttpClient(pluginData);
        Request request = new Request.Builder()
                .url((pluginData.tls ? "https://" : "http://") + pluginData.url + "/booking/cancel")
                .post(RequestBody.create(APPLICATION_JSON, new Gson().toJson(cancelBookingRequest)))
                .build();
        CancelBookingResponse confirmBookingResponse = sendHttpRequestAndParseResponse(httpClient, request, CancelBookingResponse.class);
        validateOrThrow(restToGrpc(confirmBookingResponse), cancelBookingResponseValidator);
        log.info("Success for ::cancelBooking@{}", pluginData.url);
        return confirmBookingResponse;
    }
}
