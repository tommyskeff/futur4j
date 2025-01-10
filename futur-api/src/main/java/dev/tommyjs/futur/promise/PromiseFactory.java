package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.executor.PromiseExecutor;
import dev.tommyjs.futur.util.PromiseUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("unchecked")
public interface PromiseFactory {

    /**
     * Creates a new {@link PromiseFactory} with the given logger and executors.
     *
     * @param logger        the logger
     * @param syncExecutor  the synchronous executor
     * @param asyncExecutor the asynchronous executor
     * @return the new promise factory
     */
    static @NotNull PromiseFactory of(@NotNull Logger logger, @NotNull PromiseExecutor<?> syncExecutor,
                                      @NotNull PromiseExecutor<?> asyncExecutor) {
        return new PromiseFactoryImpl<>(logger, syncExecutor, asyncExecutor);
    }

    /**
     * Creates a new {@link PromiseFactory} with the given logger and dual executor.
     *
     * @param logger   the logger
     * @param executor the executor
     * @return the new promise factory
     */
    static @NotNull PromiseFactory of(@NotNull Logger logger, @NotNull PromiseExecutor<?> executor) {
        return new PromiseFactoryImpl<>(logger, executor, executor);
    }

    /**
     * Creates a new {@link PromiseFactory} with the given logger and executor.
     *
     * @param logger   the logger
     * @param executor the executor
     * @return the new promise factory
     */
    static @NotNull PromiseFactory of(@NotNull Logger logger, @NotNull ScheduledExecutorService executor) {
        return of(logger, PromiseExecutor.of(executor));
    }

    /**
     * Creates a new uncompleted promise.
     *
     * @return the new promise
     */
    <T> @NotNull CompletablePromise<T> unresolved();

    /**
     * Creates a new promise, completed with the given value.
     *
     * @param value the value to complete the promise with
     * @return the new promise
     */
    <T> @NotNull Promise<T> resolve(T value);

    /**
     * Creates a new promise, completed with {@code null}.
     *
     * @return the new promise
     * @apiNote This method is often useful for starting promise chains.
     */
    @NotNull Promise<Void> start();

    /**
     * Creates a new promise, completed exceptionally with the given error.
     *
     * @param error the error to complete the promise with
     * @return the new promise
     */
    <T> @NotNull Promise<T> error(@NotNull Throwable error);

    /**
     * Creates a new promise backed by the given completion and future.
     * The promise will be completed upon completion of the {@link CompletionStage}
     * and the {@link Future} will be cancelled upon cancellation of the promise.
     *
     * @param completion the completion stage to wrap
     * @param future the future to wrap
     * @return the new promise
     */
    <T> @NotNull Promise<T> wrap(@NotNull CompletionStage<T> completion, @Nullable Future<T> future);

    /**
     * Creates a new promise backed by the given future.
     * The promise will be completed upon completion of the {@link CompletableFuture}
     * and the {@link CompletableFuture} will be cancelled upon cancellation of the promise.
     *
     * @param future the future to wrap
     * @return the new promise
     */
    default <T> @NotNull Promise<T> wrap(@NotNull CompletableFuture<T> future) {
        return wrap(future, future);
    };

    /**
     * Combines two promises into a single promise that resolves when both promises are completed.
     * If either input promise completes exceptionally, the other promise will be cancelled
     * and the output promise will complete exceptionally.
     *
     * @param p1   the first promise
     * @param p2   the second promise
     * @return the combined promise
     */
    <K, V> @NotNull Promise<Map.Entry<K, V>> combine(@NotNull Promise<K> p1, @NotNull Promise<V> p2);


    /**
     * Combines key-value pairs of promises into a single promise that completes
     * when all promises are completed, with the results mapped by their keys.
     * If any promise completes exceptionally, the other promises will be cancelled
     * and the combined promise will complete exceptionally.
     *
     * @param promises the input promises
     * @param expectedSize the expected size of the iterator (used for optimization)
     * @return the combined promise
     */
    <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Iterator<Map.Entry<K, Promise<V>>> promises,
                                                     int expectedSize);

    /**
     * Combines key-value pairs of promises into a single promise that completes
     * when all promises are completed, with the results mapped by their keys.
     * If any promise completes exceptionally, the other promises will be cancelled
     * and the combined promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Spliterator<Map.Entry<K, Promise<V>>> promises) {
        return combineMapped(Spliterators.iterator(promises), PromiseUtil.estimateSize(promises));
    }

    /**
     * Combines key-value pairs of promises into a single promise that completes
     * when all promises are completed, with the results mapped by their keys.
     * If any promise completes exceptionally, the other promises will be cancelled
     * and the combined promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Stream<Map.Entry<K, Promise<V>>> promises) {
        return combineMapped(promises.spliterator());
    }

    /**
     * Combines key-value pairs of promises into a single promise that completes
     * when all promises are completed, with the results mapped by their keys.
     * If any promise completes exceptionally, the other promises will be cancelled
     * and the combined promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Iterable<Map.Entry<K, Promise<V>>> promises) {
        return combineMapped(promises.spliterator());
    }

    /**
     * Combines key-value pairs of promises into a single promise that completes
     * when all promises are completed, with the results mapped by their keys.
     * If any promise completes exceptionally, the other promises will be cancelled
     * and the combined promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Map.Entry<K, Promise<V>>... promises) {
        return combineMapped(Arrays.spliterator(promises));
    }

    /**
     * Combines key-value pairs of promises into a single promise that completes
     * when all promises are completed, with the results mapped by their keys.
     * If any promise completes exceptionally, the other promises will be cancelled
     * and the combined promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Map<K, Promise<V>> promises) {
        return combineMapped(promises.entrySet().iterator(), promises.size());
    }

    /**
     * Combines key-value pairs of promises into a single promise that completes
     * when all promises are completed, with the results mapped by their keys.
     * If any promise completes exceptionally, the other promises will be cancelled
     * and the combined promise will complete exceptionally.
     *
     * @param keys the keys to map to promises
     * @param mapper the function to map keys to promises
     * @return the combined promise
     */
    default <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Stream<K> keys,
                                                             @NotNull Function<K, Promise<V>> mapper) {
        return combineMapped(keys.map(k -> new AbstractMap.SimpleImmutableEntry<>(k, mapper.apply(k))));
    }

    /**
     * Combines key-value pairs of promises into a single promise that completes
     * when all promises are completed, with the results mapped by their keys.
     * If any promise completes exceptionally, the other promises will be cancelled
     * and the combined promise will complete exceptionally.
     *
     * @param keys the keys to map to promises
     * @param mapper the function to map keys to promises
     * @return the combined promise
     */
    default <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Iterable<K> keys,
                                                             @NotNull Function<K, Promise<V>> mapper) {
        return combineMapped(StreamSupport.stream(keys.spliterator(), false), mapper);
    }

    /**
     * @deprecated Use combineMapped instead.
     */
    @Deprecated
    default <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises) {
        return combineMapped(promises);
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises
     * are completed, with a list of results in the original order.
     * If any promise completes exceptionally, all other promises will be cancelled
     * and the combined promise will complete exceptionally.
     *
     * @param promises the input promises
     * @param expectedSize the expected size of the iterator (used for optimization)
     * @return the combined promise
     */
    <V> @NotNull Promise<List<V>> combine(@NotNull Iterator<Promise<V>> promises, int expectedSize);

    /**
     * Combines multiple promises into a single promise that completes when all promises
     * are completed, with a list of results in the original order.
     * If any promise completes exceptionally, all other promises will be cancelled
     * and the combined promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <V> @NotNull Promise<List<V>> combine(@NotNull Spliterator<Promise<V>> promises) {
        return combine(Spliterators.iterator(promises), PromiseUtil.estimateSize(promises));
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises
     * are completed, with a list of results in the original order.
     * If any promise completes exceptionally, all other promises will be cancelled
     * and the combined promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <V> @NotNull Promise<List<V>> combine(@NotNull Stream<Promise<V>> promises) {
        return combine(promises.spliterator());
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises
     * are completed, with a list of results in the original order.
     * If any promise completes exceptionally, all other promises will be cancelled
     * and the combined promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <V> @NotNull Promise<List<V>> combine(@NotNull Iterable<Promise<V>> promises) {
        return combine(promises.spliterator());
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises
     * are completed, with a list of results in the original order.
     * If any promise completes exceptionally, all other promises will be cancelled
     * and the combined promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <V> @NotNull Promise<List<V>> combine(@NotNull Promise<V>... promises) {
        return combine(Arrays.spliterator(promises));
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises
     * are completed, with a list of completions in the original order.
     *
     * @param promises the input promises
     * @param expectedSize the expected size of the iterator (used for optimization)
     * @return the combined promise
     */
    @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Iterator<Promise<?>> promises,
                                                            int expectedSize);

    /**
     * Combines multiple promises into a single promise that completes when all promises
     * are completed, with a list of completions in the original order.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Spliterator<Promise<?>> promises) {
        return allSettled(Spliterators.iterator(promises), PromiseUtil.estimateSize(promises));
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises
     * are completed, with a list of completions in the original order.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Stream<Promise<?>> promises) {
        return allSettled(promises.spliterator());
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises
     * are completed, with a list of completions in the original order.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Iterable<Promise<?>> promises) {
        return allSettled(promises.spliterator());
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises
     * are completed, with a list of completions in the original order.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Promise<?>... promises) {
        return allSettled(Arrays.spliterator(promises));
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises complete.
     * If any promise completes exceptionally, all other promises will be cancelled
     * and the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    @NotNull Promise<Void> all(@NotNull Iterator<Promise<?>> promises);

    /**
     * Combines multiple promises into a single promise that completes when all promises complete.
     * If any promise completes exceptionally, all other promises will be cancelled
     * and the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default @NotNull Promise<Void> all(@NotNull Stream<Promise<?>> promises) {
        return all(promises.iterator());
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises complete.
     * If any promise completes exceptionally, all other promises will be cancelled
     * and the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default @NotNull Promise<Void> all(@NotNull Iterable<Promise<?>> promises) {
        return all(promises.iterator());
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises complete.
     * If any promise completes exceptionally, all other promises will be cancelled
     * and the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default @NotNull Promise<Void> all(@NotNull Promise<?>... promises) {
        return all(Arrays.asList(promises).iterator());
    }

    /**
     * Combines multiple promises into a single promise that completes when any promise is completed.
     * If {@code ignoreErrors} is {@code false} and the first promise completed exceptionally, the
     * combined promise will also complete exceptionally. Otherwise, the combined promise will wait for a
     * successful completion or complete with {@code null} if all promises complete exceptionally.
     * Additionally, if {@code cancelLosers} is {@code true}, the other promises will be cancelled
     * once the combined promise is completed.
     *
     * @param promises the input promises
     * @param ignoreErrors whether to ignore promises that complete exceptionally
     * @return the combined promise
     */
    <V> @NotNull Promise<V> race(@NotNull Iterator<Promise<V>> promises, boolean ignoreErrors);


    /**
     * Combines multiple promises into a single promise that completes when any promise is completed.
     * If {@code ignoreErrors} is {@code false} and the first promise completed exceptionally, the
     * combined promise will also complete exceptionally. Otherwise, the combined promise will wait for a
     * successful completion or complete with {@code null} if all promises complete exceptionally.
     * Additionally, The other promises will be cancelled once the combined promise is completed.
     *
     * @param promises the input promises
     * @param ignoreErrors whether to ignore promises that complete exceptionally
     * @return the combined promise
     */
    default <V> @NotNull Promise<V> race(@NotNull Stream<Promise<V>> promises, boolean ignoreErrors) {
        return race(promises.iterator(), ignoreErrors);
    }

    /**
     * Combines multiple promises into a single promise that completes when any promise is completed.
     * If the first promise completed exceptionally, the combined promise will also complete exceptionally.
     * Additionally, the other promises will be cancelled once the combined promise is completed.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <V> @NotNull Promise<V> race(@NotNull Stream<Promise<V>> promises) {
        return race(promises, false);
    }

    /**
     * Combines multiple promises into a single promise that completes when any promise is completed.
     * If {@code ignoreErrors} is {@code false} and the first promise completed exceptionally, the
     * combined promise will also complete exceptionally. Otherwise, the combined promise will wait for a
     * successful completion or complete with {@code null} if all promises complete exceptionally.
     * Additionally, The other promises will be cancelled once the combined promise is completed.
     *
     * @param promises the input promises
     * @param ignoreErrors whether to ignore promises that complete exceptionally
     * @return the combined promise
     */
    default <V> @NotNull Promise<V> race(@NotNull Iterable<Promise<V>> promises, boolean ignoreErrors) {
        return race(promises.iterator(), ignoreErrors);
    }

    /**
     * Combines multiple promises into a single promise that completes when any promise is completed.
     * If the first promise completed exceptionally, the combined promise will also complete exceptionally.
     * Additionally, the other promises will be cancelled once the combined promise is completed.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <V> @NotNull Promise<V> race(@NotNull Iterable<Promise<V>> promises) {
        return race(promises, false);
    }

    /**
     * Combines multiple promises into a single promise that completes when any promise is completed.
     * If {@code ignoreErrors} is {@code false} and the first promise completed exceptionally, the
     * combined promise will also complete exceptionally. Otherwise, the combined promise will wait for a
     * successful completion or complete with {@code null} if all promises complete exceptionally.
     * Additionally, The other promises will be cancelled once the combined promise is completed.
     *
     * @param promises the input promises
     * @param ignoreErrors whether to ignore promises that complete exceptionally
     * @return the combined promise
     */
    default <V> @NotNull Promise<V> race(boolean ignoreErrors, @NotNull Promise<V>... promises) {
        return race(Arrays.asList(promises), ignoreErrors);
    }

    /**
     * Combines multiple promises into a single promise that completes when any promise is completed.
     * If the first promise completed exceptionally, the combined promise will also complete exceptionally.
     * Additionally, the other promises will be cancelled once the combined promise is completed.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <V> @NotNull Promise<V> race(@NotNull Promise<V>... promises) {
        return race(false, promises);
    }

}
