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
 * Searches for products on the remote API (before mapping is done).
 *
 * @author Mindaugas Žakšauskas
 */
public class GrpcSearchProductsAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(GrpcSearchProductsAction.class);

    private final BasicProductInfoValidator basicProductInfoValidator;

    @Inject
    public GrpcSearchProductsAction(BasicProductInfoValidator basicProductInfoValidator) {
        this.basicProductInfoValidator = basicProductInfoValidator;
    }

    public List<BasicProductInfo> search(@Nonnull PluginData pluginData,
                                         @Nonnull Collection<PluginConfigurationParameterValue> pluginConfiguration) {
        log.info("Searching for inventory products in plugin {}", pluginData.url);

        SearchProductsRequest pluginSearchRequest = SearchProductsRequest.newBuilder()
                .addAllParameters(pluginConfiguration)
                .build();

        List<BasicProductInfo> result = Collections.synchronizedList(new ArrayList<>());
        withPluginStub(
                pluginData,
                stub -> doWithLatch(
                        latch -> stub.searchProducts(
                                pluginSearchRequest,
                                new StreamObserver<BasicProductInfo>() {
                                    @Override
                                    public void onNext(BasicProductInfo basicProductInfo) {
                                        result.add(basicProductInfo);
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        log.error("Plugin erred on Search", throwable);
                                        latch.countDown();
                                    }

                                    @Override
                                    public void onCompleted() {
                                        latch.countDown();
                                    }
                                })
                )
        );
        if (result.isEmpty()) {
            String error = "No products returned";
            log.error(error);
            throw new IllegalStateException(error);
        }
        validateOrThrow(result, basicProductInfoValidator);
        return result;
    }
}
