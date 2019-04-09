package io.bokun.inventory.plugin.harness;

import java.io.*;
import java.nio.file.*;

import javax.annotation.*;

import com.google.inject.*;

import static com.google.common.base.Charsets.*;
import static com.google.common.base.Strings.*;

/**
 * <p>The entry point for launching the harness. Also bootstraps Gradle.</p>
 *
 * <p>The following environment variables are required:<ul>
 *     <li><tt>PLUGIN_URL</tt> - url of plugin to test, e.g. <tt>localhost:8080</tt></li>
 *     <li><tt>PLUGIN_CONFIG_$PARAMETER</tt>, e.g.: <tt>PLUGIN_CONFIG_USERNAME</tt> - a list of config parameters prefixed with
 *     <tt>PLUGIN_CONFIG_</tt> your plugin requires</li>
 * </ul>
 * </p>
 *
 * @author Mindaugas Žakšauskas
 */
public class Main {

    public static void main(String[] args) {
        boolean isRest = (args.length == 1) && "-rest".equals(args[0]);
        boolean isGrpc = (args.length == 1) && "-grpc".equals(args[0]);

        if (!isRest && !isGrpc) {
            System.err.println("Usage: Main [OPTION]");
            System.err.println("  -rest Runs sample RESTful service");
            System.err.println("  -grpc Runs sample gRPC service");
            System.exit(1);
        }

        Injector injector = Guice.createInjector();
        Configuration configuration = injector.getInstance(Configuration.class);
        if (isGrpc) {
            injector.getInstance(GrpcHarness.class).runEndToEnd(configuration);
        }
        if (isRest) {
            injector.getInstance(RestHarness.class).runEndToEnd(configuration);
        }
    }

    public static class Configuration {
        public static final String PLUGIN_URL = "PLUGIN_URL";
        public static final String USE_TLS = "USE_TLS";
        public static final String SSL_CERT_FILE = "SSL_CERT_FILE";
        public static final String SHARED_SECRET = "SHARED_SECRET";
        public static final String TRANSPORT = "TRANSPORT";
        public static final String REST_BASIC_AUTH_USERNAME = "REST_BASIC_AUTH_USERNAME";
        public static final String REST_BASIC_AUTH_PASSWORD = "REST_BASIC_AUTH_PASSWORD";

        public final PluginData pluginData;

        @Nonnull
        private static String getMandatoryString(String key) {
            String result = System.getenv().get(key);
            if (isNullOrEmpty(result)) {
                throw new IllegalArgumentException(key + " environment variable missing");
            }
            return result;
        }

        @Nullable
        private static String getOptionalString(String key, String defaultTo) {
            String value = System.getenv().get(key);
            return value != null ? value : defaultTo;
        }

        private static boolean getOptionalBoolean(String key, boolean defaultTo) {
            String value = System.getenv().get(key);
            if (Boolean.TRUE.toString().equalsIgnoreCase(value)) {
                return true;
            }
            if (Boolean.FALSE.toString().equalsIgnoreCase(value)) {
                return false;
            }
            return defaultTo;
        }

        private static String getOptionalFile(String key, String defaultTo) {
            String value = System.getenv().get(key);
            if (value == null) {
                return defaultTo;
            }
            Path path = Paths.get(value);
            if (!Files.exists(path) || !Files.isReadable(path)) {
                throw new IllegalArgumentException("No such file or can't read: " + value);
            }
            try {
                return new String(Files.readAllBytes(path), UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public Configuration() {
            this.pluginData = new PluginData(
                    getMandatoryString(PLUGIN_URL),
                    getOptionalBoolean(USE_TLS, false),
                    getOptionalFile(SSL_CERT_FILE, null),
                    getOptionalString(SHARED_SECRET, null),
                    getOptionalString(TRANSPORT, "REST"),
                    getOptionalString(REST_BASIC_AUTH_USERNAME, null),
                    getOptionalString(REST_BASIC_AUTH_PASSWORD, null)
            );
        }
    }
}
