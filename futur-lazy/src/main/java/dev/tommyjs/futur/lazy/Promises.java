package dev.tommyjs.futur.lazy;

import dev.tommyjs.futur.executor.PromiseExecutor;
import dev.tommyjs.futur.promise.CompletablePromise;
import dev.tommyjs.futur.promise.Promise;
import dev.tommyjs.futur.promise.PromiseCompletion;
import dev.tommyjs.futur.promise.PromiseFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public final class Promises {

    private static final Logger LOGGER = LoggerFactory.getLogger(Promises.class);
    private static PromiseFactory factory = PromiseFactory.of(LOGGER, PromiseExecutor.virtualThreaded());

    public static void useFactory(@NotNull PromiseFactory factory) {
        Promises.factory = factory;
    }

    public static <T> @NotNull CompletablePromise<T> unresolved() {
        return factory.unresolved();
    }

    public static <K, V> @NotNull Promise<Map.Entry<K, V>> combine(@NotNull Promise<K> p1, @NotNull Promise<V> p2, boolean cancelOnError) {
        return factory.combine(p1, p2, cancelOnError);
    }

    public static <K, V> @NotNull Promise<Map.Entry<K, V>> combine(@NotNull Promise<K> p1, @NotNull Promise<V> p2) {
        return factory.combine(p1, p2);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(
        @NotNull Map<K, Promise<V>> promises,
        @Nullable BiConsumer<K, Throwable> exceptionHandler,
        boolean propagateCancel
    ) {
        return factory.combine(promises, exceptionHandler, propagateCancel);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, @NotNull BiConsumer<K, Throwable> exceptionHandler) {
        return factory.combine(promises, exceptionHandler);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, boolean cancelOnError) {
        return factory.combine(promises, cancelOnError);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises) {
        return factory.combine(promises);
    }

    public static <V> @NotNull Promise<List<V>> combine(
        @NotNull Iterator<Promise<V>> promises, int expectedSize,
        @Nullable BiConsumer<Integer, Throwable> exceptionHandler, boolean propagateCancel
    ) {
        return factory.combine(promises, expectedSize, exceptionHandler, propagateCancel);
    }

    public static <V> @NotNull Promise<List<V>> combine(
        @NotNull Collection<Promise<V>> promises,
        @NotNull BiConsumer<Integer, Throwable> exceptionHandler,
        boolean propagateCancel
    ) {
        return factory.combine(promises, exceptionHandler, propagateCancel);
    }

    public static <V> @NotNull Promise<List<V>> combine(
        @NotNull Collection<Promise<V>> promises,
        @NotNull BiConsumer<Integer, Throwable> exceptionHandler
    ) {
        return factory.combine(promises, exceptionHandler);
    }

    public static <V> @NotNull Promise<List<V>> combine(@NotNull Collection<Promise<V>> promises, boolean cancelOnError) {
        return factory.combine(promises, cancelOnError);
    }

    public static <V> @NotNull Promise<List<V>> combine(@NotNull Collection<Promise<V>> promises) {
        return factory.combine(promises);
    }

    public static <V> @NotNull Promise<List<V>> combine(
        @NotNull Stream<Promise<V>> promises,
        @NotNull BiConsumer<Integer, Throwable> exceptionHandler,
        boolean propagateCancel
    ) {
        return factory.combine(promises, exceptionHandler, propagateCancel);
    }

    public static <V> @NotNull Promise<List<V>> combine(
        @NotNull Stream<Promise<V>> promises,
        @NotNull BiConsumer<Integer, Throwable> exceptionHandler
    ) {
        return factory.combine(promises, exceptionHandler);
    }

    public static <V> @NotNull Promise<List<V>> combine(@NotNull Stream<Promise<V>> promises, boolean cancelOnError) {
        return factory.combine(promises, cancelOnError);
    }

    public static <V> @NotNull Promise<List<V>> combine(@NotNull Stream<Promise<V>> promises) {
        return factory.combine(promises);
    }

    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(
        @NotNull Iterator<Promise<?>> promises, int estimatedSize, boolean propagateCancel) {
        return factory.allSettled(promises, estimatedSize, propagateCancel);
    }

    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Collection<Promise<?>> promises, boolean propagateCancel) {
        return factory.allSettled(promises, propagateCancel);
    }

    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Collection<Promise<?>> promises) {
        return factory.allSettled(promises);
    }

    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Stream<Promise<?>> promises, boolean propagateCancel) {
        return factory.allSettled(promises, propagateCancel);
    }

    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Stream<Promise<?>> promises) {
        return factory.allSettled(promises);
    }

    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(boolean propagateCancel, @NotNull Promise<?>... promises) {
        return factory.allSettled(propagateCancel, promises);
    }

    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Promise<?>... promises) {
        return factory.allSettled(promises);
    }

    public static @NotNull Promise<Void> all(@NotNull Iterator<Promise<?>> promises, boolean cancelAllOnError) {
        return factory.all(promises, cancelAllOnError);
    }

    public static @NotNull Promise<Void> all(@NotNull Iterable<Promise<?>> promises, boolean cancelAllOnError) {
        return factory.all(promises, cancelAllOnError);
    }

    public static @NotNull Promise<Void> all(@NotNull Iterable<Promise<?>> promises) {
        return factory.all(promises);
    }

    public static @NotNull Promise<Void> all(@NotNull Stream<Promise<?>> promises, boolean cancelAllOnError) {
        return factory.all(promises, cancelAllOnError);
    }

    public static @NotNull Promise<Void> all(@NotNull Stream<Promise<?>> promises) {
        return factory.all(promises);
    }

    public static @NotNull Promise<Void> all(boolean cancelAllOnError, @NotNull Promise<?>... promises) {
        return factory.all(cancelAllOnError, promises);
    }

    public static @NotNull Promise<Void> all(@NotNull Promise<?>... promises) {
        return factory.all(promises);
    }

    public static <V> @NotNull Promise<V> race(@NotNull Iterator<Promise<V>> promises, boolean cancelLosers) {
        return factory.race(promises, cancelLosers);
    }

    public static <V> @NotNull Promise<V> race(@NotNull Iterable<Promise<V>> promises, boolean cancelLosers) {
        return factory.race(promises, cancelLosers);
    }

    public static <V> @NotNull Promise<V> race(@NotNull Iterable<Promise<V>> promises) {
        return factory.race(promises);
    }

    public static <V> @NotNull Promise<V> race(@NotNull Stream<Promise<V>> promises, boolean cancelLosers) {
        return factory.race(promises, cancelLosers);
    }

    public static <V> @NotNull Promise<V> race(@NotNull Stream<Promise<V>> promises) {
        return factory.race(promises);
    }

    public static <T> @NotNull Promise<T> wrap(@NotNull CompletableFuture<T> future) {
        return factory.wrap(future);
    }

    public static @NotNull Promise<Void> start() {
        return factory.start();
    }

    public static <T> @NotNull Promise<T> resolve(T value) {
        return factory.resolve(value);
    }

    public static <T> @NotNull Promise<T> error(@NotNull Throwable error) {
        return factory.error(error);
    }

}
