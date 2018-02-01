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

    public PluginData(@Nonnull String url, boolean tls, @Nullable String cert, @Nullable String sharedSecret) {
        this.url = url;
        this.tls = tls;
        this.cert = cert;
        this.sharedSecret = sharedSecret;
    }
}
