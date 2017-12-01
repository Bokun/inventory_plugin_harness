package io.bokun.inventory.plugin.harness;

import javax.annotation.*;

import com.google.inject.*;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * <p>The entry point for launching the harness. Also bootstraps Gradle.</p>
 *
 * <p>The following environment variables are mandatory:<ul>
 *     <li><tt>PLUGIN_URL</tt> - url of plugin to test, e.g. <tt>localhost:8080</tt></li>
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

        public final String pluginUrl;

        @Nonnull
        private static String getOrComplain(String key) {
            String result = System.getenv().get(key);
            if (isNullOrEmpty(result)) {
                throw new IllegalArgumentException(key + " environment variable missing");
            }
            return result;
        }

        public Configuration() {
            this.pluginUrl = getOrComplain(PLUGIN_URL);
        }
    }
}
