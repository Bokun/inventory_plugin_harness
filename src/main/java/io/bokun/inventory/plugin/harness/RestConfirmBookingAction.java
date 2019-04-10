package io.bokun.inventory.plugin.harness;

import javax.annotation.*;

import com.google.gson.*;
import com.google.inject.*;
import com.squareup.okhttp.*;
import io.bokun.inventory.plugin.api.rest.*;
import io.bokun.inventory.plugin.harness.validation.*;
import org.slf4j.*;

import static io.bokun.inventory.util.GrpcRestMapper.*;
import static io.bokun.inventory.plugin.harness.RestUtil.*;
import static io.bokun.inventory.plugin.harness.validation.ValidationUtils.*;

/**
 * Confirms reservation for plugin, if the latter supports it. Uses RESTful transport protocol.
 *
 * @author Mindaugas Žakšauskas
 */
public class RestConfirmBookingAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(RestConfirmBookingAction.class);

    private final ConfirmBookingResponseValidator confirmBookingResponseValidator;

    @Inject
    public RestConfirmBookingAction(ConfirmBookingResponseValidator confirmBookingResponseValidator) {
        this.confirmBookingResponseValidator = confirmBookingResponseValidator;
    }

    @Nonnull
    public ConfirmBookingResponse confirmBooking(@Nonnull PluginData pluginData, @Nonnull ConfirmBookingRequest confirmBookingRequest) {
        OkHttpClient httpClient = getHttpClient(pluginData);
        Request request = new Request.Builder()
                .url((pluginData.tls ? "https://" : "http://") + pluginData.url + "/booking/confirm")
                .post(RequestBody.create(APPLICATION_JSON, new Gson().toJson(confirmBookingRequest)))
                .build();
        ConfirmBookingResponse confirmBookingResponse = sendHttpRequestAndParseResponse(httpClient, request, ConfirmBookingResponse.class);
        validateOrThrow(restToGrpc(confirmBookingResponse), confirmBookingResponseValidator);
        log.info("Success for ::confirmBooking@{}", pluginData.url);
        return confirmBookingResponse;
    }
}
