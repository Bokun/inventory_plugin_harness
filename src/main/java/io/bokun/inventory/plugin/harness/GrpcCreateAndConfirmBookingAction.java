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
 * Creates & confirms booking for plugin, if the latter supports it.
 *
 * @author Mindaugas Žakšauskas
 */
public class GrpcCreateAndConfirmBookingAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(GrpcCreateAndConfirmBookingAction.class);

    private final ConfirmBookingResponseValidator confirmBookingResponseValidator;

    @Inject
    public GrpcCreateAndConfirmBookingAction(ConfirmBookingResponseValidator confirmBookingResponseValidator) {
        this.confirmBookingResponseValidator = confirmBookingResponseValidator;
    }

    @Nonnull
    public ConfirmBookingResponse createAndConfirmBooking(@Nonnull PluginData pluginData, @Nonnull CreateConfirmBookingRequest request) {
        List<ConfirmBookingResponse> result = new ArrayList<>(1);
        withPluginStub(
                pluginData,
                stub -> {
                    log.info("Calling ::createConfirmBooking@{}", pluginData.url);
                    doWithLatch(
                            latch -> stub.createAndConfirmBooking(
                                    request,
                                    new StreamObserver<ConfirmBookingResponse>() {
                                        @Override
                                        public void onNext(ConfirmBookingResponse response) {
                                            result.add(response);
                                        }

                                        @Override
                                        public void onError(Throwable throwable) {
                                            log.error("Plugin erred on creating & confirming", throwable);
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
            log.error("Could not get booking confirmed");
            throw new IllegalStateException();
        }
        validateOrThrow(result.get(0), confirmBookingResponseValidator);
        log.info("Success for ::createConfirmBooking@{}", pluginData.url);
        return result.get(0);
    }
}
