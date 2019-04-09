package io.bokun.inventory.plugin.harness;

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
 * Gets plugin definition and validates whether returned result is valid. Uses gRPC transport protocol.
 *
 * @author Mindaugas Žakšauskas
 */
public class GrpcGetDefinitionAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(GrpcGetDefinitionAction.class);

    private final PluginDefinitionValidator pluginDefinitionValidator;

    @Inject
    public GrpcGetDefinitionAction(PluginDefinitionValidator pluginDefinitionValidator) {
        this.pluginDefinitionValidator = pluginDefinitionValidator;
    }

    @Nonnull
    public PluginDefinition getDefinition(PluginData pluginData) {
        PluginDefinition[] result = new PluginDefinition[1];
        withPluginStub(
                pluginData,
                stub -> {
                    log.info("Calling ::getDefinition@{}", pluginData.url);
                    doWithLatch(
                            latch -> stub.getDefinition(
                                    Empty.getDefaultInstance(),
                                    new StreamObserver<PluginDefinition>() {
                                        @Override
                                        public void onNext(PluginDefinition pluginDefinition) {
                                            if (result[0] != null) {
                                                log.error("Plugin can only send one definition only");
                                                throw new IllegalStateException("Plugin can only send one definition only");
                                            }
                                            result[0] = pluginDefinition;
                                        }

                                        @Override
                                        public void onError(Throwable throwable) {
                                            log.error("Plugin erred on returning definition", throwable);
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
        validateOrThrow(result[0], pluginDefinitionValidator);
        log.info("Success for ::getDefinition@{}", pluginData.url);
        return result[0];
    }
}
