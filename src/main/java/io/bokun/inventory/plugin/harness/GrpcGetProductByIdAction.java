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
 * Searches for products on the remote API (before mapping is done). Uses gRPC transport.
 *
 * @author Mindaugas Žakšauskas
 */
public class GrpcGetProductByIdAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(GrpcGetProductByIdAction.class);

    private final ProductDescriptionValidator productDescriptionValidator;

    @Inject
    public GrpcGetProductByIdAction(ProductDescriptionValidator productDescriptionValidator) {
        this.productDescriptionValidator = productDescriptionValidator;
    }

    @Nonnull
    public ProductDescription getProductById(@Nonnull PluginData pluginData,
                                             @Nonnull Collection<PluginConfigurationParameterValue> pluginConfiguration,
                                             String productId) {
        log.info("Searching for inventory products in plugin {}", pluginData.url);

        GetProductByIdRequest getRequest = GetProductByIdRequest.newBuilder()
                .addAllParameters(pluginConfiguration)
                .setExternalId(productId)
                .build();

        Collection<ProductDescription> result = new ArrayList<>();
        withPluginStub(
                pluginData,
                stub -> {
                    doWithLatch(
                            latch -> stub.getProductById(
                                    getRequest,
                                    new StreamObserver<ProductDescription>() {
                                        @Override
                                        public void onNext(ProductDescription productDescription) {
                                            result.add(productDescription);
                                        }

                                        @Override
                                        public void onError(Throwable throwable) {
                                            log.error("Plugin erred on getProductById", throwable);
                                            latch.countDown();
                                        }

                                        @Override
                                        public void onCompleted() {
                                            latch.countDown();
                                        }
                                    })
                    );
                }
        );
        if (result.size() != 1) {
            String error = "Too many or no products returned (" + result.size() + ")";
            log.error(error);
            throw new IllegalStateException(error);
        }
        validateOrThrow(result, productDescriptionValidator);
        return result.iterator().next();
    }
}
