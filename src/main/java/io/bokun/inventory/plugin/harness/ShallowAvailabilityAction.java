package io.bokun.inventory.plugin.harness;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import javax.annotation.*;

import io.bokun.inventory.common.api.grpc.Date;
import io.bokun.inventory.common.api.grpc.*;
import io.bokun.inventory.plugin.api.grpc.*;
import io.grpc.stub.*;
import org.slf4j.*;

import static io.bokun.inventory.plugin.harness.GrpcUtil.*;

/**
 * Makes a "shallow" call to receive availabilities of given products.
 *
 * @author Mindaugas Žakšauskas
 */
public class ShallowAvailabilityAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(ShallowAvailabilityAction.class);

    @Nonnull
    public Set<String> getAvailableProducts(@Nonnull PluginData pluginData,
                                            @Nonnull Collection<PluginConfigurationParameterValue> pluginConfiguration,
                                            LocalDate from,
                                            LocalDate to,
                                            int requiredCapacity,
                                            Iterable<String> productIds) {
        ProductsAvailabilityRequest availabilityRequest = ProductsAvailabilityRequest.newBuilder()
                .addAllParameters(pluginConfiguration)
                .setRange(
                        DatePeriod.newBuilder()
                                .setFrom(Date.newBuilder().setYear(from.getYear()).setMonth(from.getMonthValue()).setDay(from.getDayOfMonth()))
                                .setTo(Date.newBuilder().setYear(to.getYear()).setMonth(to.getMonthValue()).setDay(to.getDayOfMonth()))
                                .build()
                )
                .setRequiredCapacity(requiredCapacity)
                .addAllExternalProductIds(productIds)
                .build();

        Collection<ProductsAvailabilityResponse> result = new ArrayList<>();
        withPluginStub(
                pluginData,
                stub -> doWithLatch(
                        latch -> stub.getAvailableProducts(
                                availabilityRequest,
                                new StreamObserver<ProductsAvailabilityResponse>() {
                                    @Override
                                    public void onNext(ProductsAvailabilityResponse productDescription) {
                                        result.add(productDescription);
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        log.error("Plugin erred on shallow avail check", throwable);
                                        latch.countDown();
                                    }

                                    @Override
                                    public void onCompleted() {
                                        latch.countDown();
                                    }
                                }
                        )
                )
        );
        return result.stream()
                .map(ProductsAvailabilityResponse::getProductId)
                .collect(Collectors.toSet());
    }
}
