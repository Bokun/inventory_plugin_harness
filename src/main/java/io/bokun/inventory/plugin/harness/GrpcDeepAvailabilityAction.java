package io.bokun.inventory.plugin.harness;

import java.time.*;
import java.util.*;

import javax.annotation.*;

import com.google.inject.*;
import io.bokun.inventory.common.api.grpc.Date;
import io.bokun.inventory.common.api.grpc.*;
import io.bokun.inventory.plugin.api.grpc.*;
import io.bokun.inventory.plugin.harness.validation.*;
import io.grpc.stub.*;
import org.slf4j.*;

import static io.bokun.inventory.plugin.harness.GrpcUtil.*;
import static io.bokun.inventory.plugin.harness.validation.ValidationUtils.*;

/**
 * Makes a "deep" call to receive availability of given single product. Uses gRPC protocol.
 *
 * @author Mindaugas Žakšauskas
 */
public class GrpcDeepAvailabilityAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(GrpcDeepAvailabilityAction.class);

    private final ProductAvailabilityWithRatesResponseValidator responseValidator;

    @Inject
    public GrpcDeepAvailabilityAction(ProductAvailabilityWithRatesResponseValidator responseValidator) {
        this.responseValidator = responseValidator;
    }

    @Nonnull
    public List<ProductAvailabilityWithRatesResponse> getAvailability(@Nonnull PluginData pluginData,
                                                                      @Nonnull Collection<PluginConfigurationParameterValue> pluginConfiguration,
                                                                      @Nonnull LocalDate from,
                                                                      @Nonnull LocalDate to,
                                                                      @Nonnull String productId) {
        ProductAvailabilityRequest availabilityRequest = ProductAvailabilityRequest.newBuilder()
                .addAllParameters(pluginConfiguration)
                .setRange(
                        DatePeriod.newBuilder()
                                .setFrom(Date.newBuilder().setYear(from.getYear()).setMonth(from.getMonthValue()).setDay(from.getDayOfMonth()))
                                .setTo(Date.newBuilder().setYear(to.getYear()).setMonth(to.getMonthValue()).setDay(to.getDayOfMonth()))
                                .build()
                )
                .setProductId(productId)
                .build();

        List<ProductAvailabilityWithRatesResponse> result = new ArrayList<>();
        withPluginStub(
                pluginData,
                stub -> doWithLatch(
                        latch -> stub.getProductAvailability(
                                availabilityRequest,
                                new StreamObserver<ProductAvailabilityWithRatesResponse>() {
                                    @Override
                                    public void onNext(ProductAvailabilityWithRatesResponse response) {
                                        result.add(response);
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        log.error("Plugin erred on deep avail check", throwable);
                                        latch.countDown();
                                    }

                                    @Override
                                    public void onCompleted() {
                                        latch.countDown();
                                    }
                                })
                )
        );
        result.forEach(response -> validateOrThrow(response, responseValidator));
        return result;
    }
}
