package io.bokun.inventory.plugin.harness;

import javax.annotation.*;

/**
 * Plugin access configuration data.
 *
 * @author Mindaugas Žakšauskas
 */
public class PluginData {

    // hostname and port
    @Nonnull
    public final String url;

    // whether TLS/SSL is used or not
    public final boolean tls;

    // SSL certificate (contents).
    @Nullable
    public final String cert;

    // shared secret, used for authentication against remote plugin
    @Nullable
    public final String sharedSecret;

    public final Transport transport;

    @Nullable
    public final String restBasicAuthUsername;

    @Nullable
    public final String restBasicAuthPassword;

    public PluginData(@Nonnull String url,
                      boolean tls,
                      @Nullable String cert,
                      @Nullable String sharedSecret,
                      String transport,
                      String restBasicAuthUsername,
                      String restBasicAuthPassword) {
        this.url = url;
        this.tls = tls;
        this.cert = cert;
        this.sharedSecret = sharedSecret;
        this.transport = Transport.valueOf(transport);
        this.restBasicAuthUsername = restBasicAuthUsername;
        this.restBasicAuthPassword = restBasicAuthPassword;
    }

    public enum Transport {
        GRPC, REST
    }
}
