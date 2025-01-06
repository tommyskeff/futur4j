package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.executor.PromiseExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface PromiseFactory {

    static @NotNull PromiseFactory of(@NotNull Logger logger, @NotNull PromiseExecutor<?> syncExecutor, @NotNull PromiseExecutor<?> asyncExecutor) {
        return new PromiseFactoryImpl<>(logger, syncExecutor, asyncExecutor);
    }

    static @NotNull PromiseFactory of(@NotNull Logger logger, @NotNull PromiseExecutor<?> executor) {
        return new PromiseFactoryImpl<>(logger, executor, executor);
    }

    static @NotNull PromiseFactory of(@NotNull Logger logger, @NotNull ScheduledExecutorService executor) {
        return of(logger, PromiseExecutor.of(executor));
    }

    private static int size(@NotNull Stream<?> stream) {
        long estimate = stream.spliterator().estimateSize();
        return estimate == Long.MAX_VALUE ? 10 : (int) estimate;
    }

    @NotNull Logger getLogger();

    <T> @NotNull CompletablePromise<T> unresolved();

    <K, V> @NotNull Promise<Map.Entry<K, V>> combine(@NotNull Promise<K> p1, @NotNull Promise<V> p2, boolean cancelOnError);

    default <K, V> @NotNull Promise<Map.Entry<K, V>> combine(@NotNull Promise<K> p1, @NotNull Promise<V> p2) {
        return combine(p1, p2, true);
    }

    <K, V> @NotNull Promise<Map<K, V>> combine(
        @NotNull Map<K, Promise<V>> promises,
        @Nullable BiConsumer<K, Throwable> exceptionHandler,
        boolean propagateCancel
    );

    default <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, @NotNull BiConsumer<K, Throwable> exceptionHandler) {
        return combine(promises, exceptionHandler, true);
    }

    default <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, boolean cancelOnError) {
        return combine(promises, null, cancelOnError);
    }

    default <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises) {
        return combine(promises, null, true);
    }

    <V> @NotNull Promise<List<V>> combine(
        @NotNull Iterator<Promise<V>> promises, int expectedSize,
        @Nullable BiConsumer<Integer, Throwable> exceptionHandler, boolean propagateCancel
    );

    default <V> @NotNull Promise<List<V>> combine(
        @NotNull Collection<Promise<V>> promises,
        @NotNull BiConsumer<Integer, Throwable> exceptionHandler,
        boolean propagateCancel
    ) {
        return combine(promises.iterator(), promises.size(), exceptionHandler, propagateCancel);
    }

    default <V> @NotNull Promise<List<V>> combine(
        @NotNull Collection<Promise<V>> promises,
        @NotNull BiConsumer<Integer, Throwable> exceptionHandler
    ) {
        return combine(promises.iterator(), promises.size(), exceptionHandler, true);
    }

    default <V> @NotNull Promise<List<V>> combine(@NotNull Collection<Promise<V>> promises, boolean cancelOnError) {
        return combine(promises.iterator(), promises.size(), null, cancelOnError);
    }

    default <V> @NotNull Promise<List<V>> combine(@NotNull Collection<Promise<V>> promises) {
        return combine(promises.iterator(), promises.size(), null, true);
    }

    default <V> @NotNull Promise<List<V>> combine(
        @NotNull Stream<Promise<V>> promises,
        @NotNull BiConsumer<Integer, Throwable> exceptionHandler,
        boolean propagateCancel
    ) {
        return combine(promises.iterator(), size(promises), exceptionHandler, propagateCancel);
    }

    default <V> @NotNull Promise<List<V>> combine(
        @NotNull Stream<Promise<V>> promises,
        @NotNull BiConsumer<Integer, Throwable> exceptionHandler
    ) {
        return combine(promises.iterator(), size(promises), exceptionHandler, true);
    }

    default <V> @NotNull Promise<List<V>> combine(@NotNull Stream<Promise<V>> promises, boolean cancelOnError) {
        return combine(promises.iterator(), size(promises), null, cancelOnError);
    }

    default <V> @NotNull Promise<List<V>> combine(@NotNull Stream<Promise<V>> promises) {
        return combine(promises.iterator(), size(promises), null, true);
    }

    @NotNull Promise<List<PromiseCompletion<?>>> allSettled(
        @NotNull Iterator<Promise<?>> promises, int estimatedSize, boolean propagateCancel);

    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Collection<Promise<?>> promises, boolean propagateCancel) {
        return allSettled(promises.iterator(), promises.size(), propagateCancel);
    }

    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Collection<Promise<?>> promises) {
        return allSettled(promises.iterator(), promises.size(), true);
    }

    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Stream<Promise<?>> promises, boolean propagateCancel) {
        return allSettled(promises.iterator(), size(promises), propagateCancel);
    }

    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Stream<Promise<?>> promises) {
        return allSettled(promises.iterator(), size(promises), true);
    }

    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(boolean propagateCancel, @NotNull Promise<?>... promises) {
        return allSettled(Arrays.asList(promises).iterator(), promises.length, propagateCancel);
    }

    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Promise<?>... promises) {
        return allSettled(Arrays.asList(promises).iterator(), promises.length, true);
    }

    @NotNull Promise<Void> all(@NotNull Iterator<Promise<?>> promises, boolean cancelAllOnError);

    default @NotNull Promise<Void> all(@NotNull Iterable<Promise<?>> promises, boolean cancelAllOnError) {
        return all(promises.iterator(), cancelAllOnError);
    }

    default @NotNull Promise<Void> all(@NotNull Iterable<Promise<?>> promises) {
        return all(promises.iterator(), true);
    }

    default @NotNull Promise<Void> all(@NotNull Stream<Promise<?>> promises, boolean cancelAllOnError) {
        return all(promises.iterator(), cancelAllOnError);
    }

    default @NotNull Promise<Void> all(@NotNull Stream<Promise<?>> promises) {
        return all(promises.iterator(), true);
    }

    default @NotNull Promise<Void> all(boolean cancelAllOnError, @NotNull Promise<?>... promises) {
        return all(Arrays.asList(promises).iterator(), cancelAllOnError);
    }

    default @NotNull Promise<Void> all(@NotNull Promise<?>... promises) {
        return all(Arrays.asList(promises).iterator(), true);
    }

    <V> @NotNull Promise<V> race(@NotNull Iterator<Promise<V>> promises, boolean cancelLosers);

    default <V> @NotNull Promise<V> race(@NotNull Iterable<Promise<V>> promises, boolean cancelLosers) {
        return race(promises.iterator(), cancelLosers);
    }

    default <V> @NotNull Promise<V> race(@NotNull Iterable<Promise<V>> promises) {
        return race(promises.iterator(), true);
    }

    default <V> @NotNull Promise<V> race(@NotNull Stream<Promise<V>> promises, boolean cancelLosers) {
        return race(promises.iterator(), cancelLosers);
    }

    default <V> @NotNull Promise<V> race(@NotNull Stream<Promise<V>> promises) {
        return race(promises.iterator(), true);
    }

    <T> @NotNull Promise<T> wrap(@NotNull CompletableFuture<T> future);

    default @NotNull Promise<Void> start() {
        return resolve(null);
    }

    <T> @NotNull Promise<T> resolve(T value);

    <T> @NotNull Promise<T> error(@NotNull Throwable error);

}
