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
        Injector injector = Guice.createInjector();
        Configuration configuration = injector.getInstance(Configuration.class);
        injector.getInstance(Harness.class).runEndToEnd(configuration);
    }

    public static class Configuration {
        public static final String PLUGIN_URL = "PLUGIN_URL";
        public static final String USE_TLS = "USE_TLS";
        public static final String SSL_CERT_FILE = "SSL_CERT_FILE";
        public static final String SHARED_SECRET = "SHARED_SECRET";

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
                    getOptionalString(SHARED_SECRET, null)
            );
        }
    }
}
