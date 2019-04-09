package io.bokun.inventory.plugin.harness;

import javax.annotation.*;

import com.google.gson.*;
import com.google.inject.*;
import com.squareup.okhttp.*;
import io.bokun.inventory.plugin.api.rest.*;
import io.bokun.inventory.plugin.harness.validation.*;
import org.slf4j.*;

import static io.bokun.inventory.plugin.harness.GrpcRestMapper.*;
import static io.bokun.inventory.plugin.harness.RestUtil.*;
import static io.bokun.inventory.plugin.harness.validation.ValidationUtils.*;

/**
 * Creates & confirms booking for plugin, if the latter supports it. Uses RESTful transport protocol.
 *
 * @author Mindaugas Žakšauskas
 */
public class RestCreateAndConfirmBookingAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(RestCreateAndConfirmBookingAction.class);

    private final ConfirmBookingResponseValidator confirmBookingResponseValidator;

    @Inject
    public RestCreateAndConfirmBookingAction(ConfirmBookingResponseValidator confirmBookingResponseValidator) {
        this.confirmBookingResponseValidator = confirmBookingResponseValidator;
    }

    @Nonnull
    public ConfirmBookingResponse createAndConfirmBooking(@Nonnull PluginData pluginData, @Nonnull CreateConfirmBookingRequest createConfirmBookingRequest) {
        OkHttpClient httpClient = getHttpClient(pluginData);
        Request request = new Request.Builder()
                .url((pluginData.tls ? "https://" : "http://") + pluginData.url + "/booking/createAndConfirm")
                .post(RequestBody.create(APPLICATION_JSON, new Gson().toJson(createConfirmBookingRequest)))
                .build();
        ConfirmBookingResponse confirmBookingResponse = sendHttpRequestAndParseResponse(httpClient, request, ConfirmBookingResponse.class);
        validateOrThrow(restToGrpc(confirmBookingResponse), confirmBookingResponseValidator);
        log.info("Success for ::createConfirmBooking@{}", pluginData.url);
        return confirmBookingResponse;
    }
}
