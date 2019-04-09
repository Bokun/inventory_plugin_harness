package io.bokun.inventory.plugin.harness;

import javax.annotation.*;

import com.google.gson.*;
import com.google.inject.*;
import com.squareup.okhttp.*;
import io.bokun.inventory.plugin.api.rest.*;
import io.bokun.inventory.plugin.harness.validation.*;
import org.slf4j.*;

import static io.bokun.inventory.plugin.harness.GrpcRestMapper.restToGrpc;
import static io.bokun.inventory.plugin.harness.RestUtil.*;
import static io.bokun.inventory.plugin.harness.validation.ValidationUtils.validateOrThrow;

/**
 * Creates reservation for plugin, if the latter supports it. Uses RESTful transport protocol.
 *
 * @author Mindaugas Žakšauskas
 */
public class RestCreateReservationAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(RestCreateReservationAction.class);

    private final ReservationResponseValidator reservationResponseValidator;

    @Inject
    public RestCreateReservationAction(ReservationResponseValidator reservationResponseValidator) {
        this.reservationResponseValidator = reservationResponseValidator;
    }

    @Nonnull
    public ReservationResponse createReservation(PluginData pluginData, ReservationRequest reservationRequest) {
        OkHttpClient httpClient = getHttpClient(pluginData);
        Request request = new Request.Builder()
                .url((pluginData.tls ? "https://" : "http://") + pluginData.url + "/booking/reserve")
                .post(RequestBody.create(APPLICATION_JSON, new Gson().toJson(reservationRequest)))
                .build();
        ReservationResponse reservationResponse = sendHttpRequestAndParseResponse(httpClient, request, ReservationResponse.class);
        validateOrThrow(restToGrpc(reservationResponse), reservationResponseValidator);
        log.info("Success for ::createReservation@{}", pluginData.url);
        return reservationResponse;
    }
}
