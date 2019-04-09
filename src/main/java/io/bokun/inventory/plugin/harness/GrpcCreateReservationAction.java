package io.bokun.inventory.plugin.harness;

import java.util.*;

import javax.annotation.*;

import com.google.inject.*;
import io.bokun.inventory.common.api.grpc.*;
import io.bokun.inventory.plugin.api.grpc.*;
import io.bokun.inventory.plugin.harness.validation.*;
import io.grpc.stub.*;
import org.slf4j.*;

import static io.bokun.inventory.plugin.harness.GrpcUtil.*;
import static io.bokun.inventory.plugin.harness.validation.ValidationUtils.validateOrThrow;

/**
 * Creates reservation for plugin, if the latter supports it. Uses gRPC transport protocol.
 *
 * @author Mindaugas Žakšauskas
 */
public class GrpcCreateReservationAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(GrpcCreateReservationAction.class);

    private final ReservationResponseValidator reservationResponseValidator;

    @Inject
    public GrpcCreateReservationAction(ReservationResponseValidator reservationResponseValidator) {
        this.reservationResponseValidator = reservationResponseValidator;
    }

    @Nonnull
    public ReservationResponse createReservation(PluginData pluginData, ReservationRequest reservationRequest) {
        List<ReservationResponse> result = new ArrayList<>(1);
        withPluginStub(
                pluginData,
                stub -> {
                    log.info("Calling ::createReservation@{} with params:{}", pluginData.url, reservationRequest);
                    doWithLatch(
                            latch -> stub.createReservation(
                                    reservationRequest,
                                    new StreamObserver<ReservationResponse>() {
                                        @Override
                                        public void onNext(ReservationResponse response) {
                                            result.add(response);
                                        }

                                        @Override
                                        public void onError(Throwable throwable) {
                                            log.error("Plugin erred on reserving", throwable);
                                            latch.countDown();
                                        }

                                        @Override
                                        public void onCompleted() {
                                            latch.countDown();
                                        }
                                    }
                            )
                    );
                }
        );
        if (result.size() != 1) {
            log.error("Could not get booking reserved");
            throw new IllegalStateException();
        }
        validateOrThrow(result.get(0), reservationResponseValidator);
        log.info("Success for ::createReservation@{}", pluginData.url);
        return result.get(0);
    }
}
