package io.bokun.inventory.plugin.harness;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import javax.annotation.*;

import com.google.common.collect.*;
import com.google.protobuf.*;
import io.grpc.*;
import org.slf4j.*;

/**
 * Various static helpers for operating with gRPC data types.
 *
 * @author Mindaugas Žakšauskas
 */
public class GrpcUtil {

    private static volatile Table<Descriptors.FieldDescriptor, Class<?>, Optional<Descriptors.FieldDescriptor>> fieldDescriptorCache = ImmutableTable.of();
    private static final Object fieldDescriptorCacheLock = new Object();
    private static final Logger log = LoggerFactory.getLogger(GrpcUtil.class);

    /**
     * Block with latch for at max X seconds so we don't leak resources.
     */
    private static final long MAX_WAIT_TIME_S = 600L;

    /**
     * Check if given field (descriptor of which is given) has matching counterpart in the target class (builder of which is given).
     */
    @Nonnull
    private static Optional<Descriptors.FieldDescriptor> findMatch(Descriptors.FieldDescriptor matchFor, GeneratedMessageV3.Builder<?> destBuilder) {
        Table<Descriptors.FieldDescriptor, Class<?>, Optional<Descriptors.FieldDescriptor>> cache = fieldDescriptorCache;
        Optional<Descriptors.FieldDescriptor> result = cache.get(matchFor, destBuilder.getClass());
        if (result != null) {
            return result;
        }

        // otherwise scan/match all fields and cache
        ImmutableTable.Builder<Descriptors.FieldDescriptor, Class<?>, Optional<Descriptors.FieldDescriptor>> tableBuilder = new ImmutableTable.Builder<>();

        for (Descriptors.FieldDescriptor sourceDescriptor : matchFor.getContainingType().getFields()) {
            boolean matchFound = false;
            for (Descriptors.FieldDescriptor targetDescriptor : destBuilder.getDescriptorForType().getFields()) {
                if (sourceDescriptor.getJavaType() == targetDescriptor.getJavaType() &&
                        sourceDescriptor.getName().equalsIgnoreCase(targetDescriptor.getName()) &&
                        sourceDescriptor.isRepeated() == targetDescriptor.isRepeated()) {
                    matchFound = true;
                    tableBuilder.put(sourceDescriptor, destBuilder.getClass(), Optional.of(targetDescriptor));
                }
            }
            if (!matchFound) {
                tableBuilder.put(sourceDescriptor, destBuilder.getClass(), Optional.empty());
            }
        }
        cache = tableBuilder
                .putAll(cache)
                .build();
        synchronized (fieldDescriptorCacheLock) {
            fieldDescriptorCache = cache;
        }
        return findMatch(matchFor, destBuilder);        // try again, should work this time
    }

    /**
     * Allows bulk-copying of object attributes from <tt>source</tt> to <tt>destBuilder</tt>.
     * Values are only copied if name and type matches across two different types.
     *
     * @param source source object.
     * @param destBuilder destination object builder.
     */
    public static void copyCommonFields(@Nonnull GeneratedMessageV3 source, @Nonnull GeneratedMessageV3.Builder<?> destBuilder) {
        for (Map.Entry<Descriptors.FieldDescriptor, Object> entry : source.getAllFields().entrySet()) {
            if (entry.getValue() == null) {
                continue;       // value not set, don't copy
            }
            Descriptors.FieldDescriptor descriptor = entry.getKey();
            Optional<Descriptors.FieldDescriptor> match = findMatch(descriptor, destBuilder);
            match.ifPresent(fieldDescriptor -> destBuilder.setField(fieldDescriptor, entry.getValue()));
        }
    }

    /**
     * Wraps opening/closing of the channel. Useful for short-lived calls. Uses plaintext.
     *
     * @param url url of the enpoint where channel should be linked to.
     * @param channelConsumer gives access to the channel.
     */
    public static void consumeChannel(@Nonnull String url, @Nonnull Consumer<Channel> channelConsumer) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(url)
                .usePlaintext(true)
                .build();
        try {
            channelConsumer.accept(channel);
        } finally {
            channel.shutdown();
        }
    }

    /**
     * Similar as {@link #consumeChannel(String, Consumer)} but is able to return value.
     */
    public static <T> T doWithChannel(@Nonnull String url, @Nonnull Function<Channel, T> function) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(url)
                .usePlaintext(true)
                .build();
        try {
            return function.apply(channel);
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
