package io.bokun.inventory.plugin.harness;

import java.io.*;
import java.lang.reflect.*;
import java.net.Proxy;
import java.util.*;

import com.google.common.collect.*;
import com.google.gson.*;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.*;
import org.slf4j.*;

import static com.google.common.base.Strings.*;
import static io.bokun.inventory.plugin.harness.CustomTrust.*;

/**
 * Various utilities pertaining to RESTful transport layer.
 *
 * @author Mindaugas Žakšauskas
 */
public class RestUtil {

    private static final Logger log = LoggerFactory.getLogger(RestUtil.class);

    public static final MediaType APPLICATION_JSON = MediaType.parse("application/json; charset=utf-8");

    public static Authenticator getRestHttpAuthenticator(String username, String password) {
        return new Authenticator() {
            @Override
            public Request authenticate(Proxy proxy, Response response) {
                if (response.request().header("Authorization") != null) {
                    return null; // Give up, we've already attempted to authenticate.
                }
                return response.request().newBuilder()
                        .header("Authorization", Credentials.basic(username, password))
                        .build();
            }

            @Override
            public Request authenticateProxy(Proxy proxy, Response response) {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static <T> T parseJson(String json, Class<T> clazz) {
        try {
            return new Gson().fromJson(json, clazz);
        } catch (JsonSyntaxException jse) {
            throw new RuntimeException(jse);
        }
    }

    public static <T> T sendHttpRequestAndParseResponse(OkHttpClient httpClient, Request request, Class<T> clazz) {
        try {
            String uri = request.uri().toString();
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                log.error("{} has returned {}. Body: ", uri, response.code(), response.body());
                throw new IllegalStateException();
            }
            String bodyString = response.body().string();
            return parseJson(bodyString, clazz);
        } catch (IOException ioe) {
            log.error("Could not call request {}", request, ioe);
            throw new IllegalStateException();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> sendHttpRequestAndParseResponseArray(OkHttpClient httpClient, Request request, Class<T> clazz) {
        try {
            String uri = request.uri().toString();
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                log.error("{} has returned {}. Body: ", uri, response.code(), response.body());
                throw new IllegalStateException();
            }
            String bodyString = response.body().string();
            Object arr = parseJson(bodyString, Array.newInstance(clazz, 0).getClass());
            if (!arr.getClass().isArray()) {
                throw new IllegalStateException();
            }
            return ImmutableList.copyOf((T[]) arr);
        } catch (IOException ioe) {
            log.error("Could not call request {}", request, ioe);
            throw new IllegalStateException();
        }
    }

    public static OkHttpClient getHttpClient(PluginData pluginData) {
        OkHttpClient httpClient = new OkHttpClient();
        if (!nullToEmpty(pluginData.restBasicAuthUsername).isEmpty()) {
            httpClient.setAuthenticator(getRestHttpAuthenticator(pluginData.restBasicAuthUsername, pluginData.restBasicAuthPassword));
        }
        if (pluginData.tls) {
            assert pluginData.cert != null;
            acceptSelfSignedCertificate(httpClient, pluginData.cert);
        }
        return httpClient;
    }
}
