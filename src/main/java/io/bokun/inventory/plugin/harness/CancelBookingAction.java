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
import static io.bokun.inventory.plugin.harness.validation.ValidationUtils.*;

/**
 * Cancels a booking.
 *
 * @author Mindaugas Žakšauskas
 */
public class CancelBookingAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(CancelBookingAction.class);

    private final CancelBookingResponseValidator cancelBookingResponseValidator;

    @Inject
    public CancelBookingAction(CancelBookingResponseValidator cancelBookingResponseValidator) {
        this.cancelBookingResponseValidator = cancelBookingResponseValidator;
    }

    @Nonnull
    public CancelBookingResponse cancelBooking(String pluginUrl, CancelBookingRequest cancelBookingRequest) {
        List<CancelBookingResponse> result = new ArrayList<>(1);
        consumeChannel(
                pluginUrl,
                channel -> {
                    PluginApiGrpc.PluginApiStub stub = PluginApiGrpc.newStub(channel);
                    log.info("Calling ::cancelBooking@{}", pluginUrl);
                    doWithLatch(
                            latch -> stub.cancelBooking(
                                    cancelBookingRequest,
                                    new StreamObserver<CancelBookingResponse>() {
                                        @Override
                                        public void onNext(CancelBookingResponse response) {
                                            result.add(response);
                                        }

                                        @Override
                                        public void onError(Throwable throwable) {
                                            log.error("Plugin erred on cancelling", throwable);
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
            log.error("Could not get booking cancelled");
            throw new IllegalStateException();
        }
        validateOrThrow(result.get(0), cancelBookingResponseValidator);
        log.info("Success for ::cancelBooking@{}", pluginUrl);
        return result.get(0);
    }
}
