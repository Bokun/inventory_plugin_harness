package io.bokun.inventory.plugin.harness;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import javax.annotation.*;
import javax.net.ssl.*;

import com.google.common.base.*;
import io.bokun.inventory.plugin.api.grpc.*;
import io.grpc.*;
import io.grpc.netty.*;
import io.netty.handler.ssl.*;
import org.slf4j.*;

import static com.google.common.base.Strings.*;
import static io.grpc.Metadata.*;
import static io.grpc.Status.*;
import static io.grpc.netty.NegotiationType.*;
import static io.netty.handler.ssl.ClientAuth.*;
import static io.netty.handler.ssl.SslProvider.*;

/**
 * Various static helpers for operating with gRPC data types.
 *
 * @author Mindaugas Žakšauskas
 */
public class GrpcUtil {

    private static final Logger log = LoggerFactory.getLogger(GrpcUtil.class);

    public static final String SHARED_SECRET_HEADER = "sharedSecret";
    public static final Metadata.Key<String> SHARED_SECRET_METADATA_KEY = Metadata.Key.of(SHARED_SECRET_HEADER, ASCII_STRING_MARSHALLER);

    private static final Splitter COLON_SPLITTER = Splitter.on(":");

    /**
     * Block with latch for at max X seconds so we don't leak resources.
     */
    private static final long MAX_WAIT_TIME_S = 600L;

    /**
     * Wraps opening/closing of the channel towards a plugin. Adds SSL/TLS and/or shared secret if required. Also forwards request ID logging.
     *
     * @param pluginData plugin config info for shaping the channel.
     * @param stubConsumer gives access to the stub.
     */
    public static void withPluginStub(@Nonnull PluginData pluginData, @Nonnull Consumer<PluginApiGrpc.PluginApiStub> stubConsumer) {
        ManagedChannel channel;

        if (pluginData.tls) {
            SslContextBuilder sslContextBuilder = GrpcSslContexts.configure(SslContextBuilder.forClient(), OPENSSL);
            if (!isNullOrEmpty(pluginData.cert)) {
                // Some certs (such as GoDaddy), albeit valid, don't work with Java out of the box.
                // Here we can add such certs to the trusted chain so this wouldn't cause us trouble. It also enables to use self-signed certs too
                sslContextBuilder.trustManager(new ByteArrayInputStream(pluginData.cert.getBytes()));
            }
            SslContext sslContext;
            try {
                sslContext = sslContextBuilder          // if tls is enabled, decorate with SSL
                        .sslProvider(OPENSSL)
                        .clientAuth(OPTIONAL)
                        .build();
            } catch (SSLException ssle) {
                log.error("Could not build SSL context", ssle);
                throw new RuntimeException(ssle);
            }

            Iterator<String> urlWithPort = COLON_SPLITTER.split(pluginData.url).iterator();
            String hostname = urlWithPort.next();
            int port = Integer.parseInt(urlWithPort.next());

            channel = NettyChannelBuilder.forAddress(hostname, port)
                    .usePlaintext(false)
                    .sslContext(sslContext)
                    .negotiationType(TLS)
                    .build();
        } else {
            channel = ManagedChannelBuilder.forTarget(pluginData.url)
                    .usePlaintext(true)
                    .build();
        }

        PluginApiGrpc.PluginApiStub stub = PluginApiGrpc.newStub(channel);

        // if shared secret is set, add it to the headers/metadata
        if (!isNullOrEmpty(pluginData.sharedSecret)) {
            stub = stub.withCallCredentials(
                    (method, attrs, appExecutor, applier) -> {
                        try {
                            Metadata headers = new Metadata();
                            headers.put(SHARED_SECRET_METADATA_KEY, pluginData.sharedSecret);
                            applier.apply(headers);
                        } catch (Throwable t) {
                            log.error("Could not apply shared secret metadata", t);
                            applier.fail(UNAUTHENTICATED.withCause(t));
                        }
                    });
        }
        try {
            stubConsumer.accept(stub);
        } finally {
            channel.shutdown();
        }
    }
    
    /**
     * Create a {@link CountDownLatch} and executes some code block which uses this latch and counts it down at some point.
     * This method blocks until the latch is exhausted or max wait time elapses.
     *
     * @param count initial latch capacity to use.
     * @param latchConsumer code which, apart other useful things, runs down the latch.
     */
    public static void doWithLatch(int count, @Nonnull Consumer<CountDownLatch> latchConsumer) {
        CountDownLatch latch = new CountDownLatch(count);
        log.trace("Latch enter");
        latchConsumer.accept(latch);
        try {
            latch.await(MAX_WAIT_TIME_S, TimeUnit.SECONDS);
        } catch (InterruptedException ignore) {
        }
        log.trace("Latch exit");
    }

    /**
     * Same as {@link #doWithLatch(int, Consumer)} but with count of 1.
     */
    public static void doWithLatch(@Nonnull Consumer<CountDownLatch> latchConsumer) {
        doWithLatch(1, latchConsumer);
    }
}
