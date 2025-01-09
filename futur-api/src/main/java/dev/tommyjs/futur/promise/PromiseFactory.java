package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.executor.PromiseExecutor;
import dev.tommyjs.futur.util.PromiseUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    default @NotNull Promise<Void> start() {
        return resolve(null);
    }

    /**
     * Creates a new promise, completed exceptionally with the given error.
     *
     * @param error the error to complete the promise with
     * @return the new promise
     */
    <T> @NotNull Promise<T> error(@NotNull Throwable error);

    /**
     * Creates a new promise backed by the given future. The promise will be completed upon completion
     * of the future.
     *
     * @param future the future to wrap
     * @return the new promise
     */
    <T> @NotNull Promise<T> wrap(@NotNull CompletableFuture<T> future);

    /**
     * Combines two promises into a single promise that completes when both promises complete. If
     * {@code link} is {@code true} and either input promise completes exceptionally (including
     * cancellation), the other promise will be cancelled and the output promise will complete
     * exceptionally.
     *
     * @param p1   the first promise
     * @param p2   the second promise
     * @param link whether to cancel the other promise on error
     * @return the combined promise
     */
    <K, V> @NotNull Promise<Map.Entry<K, V>> combine(@NotNull Promise<K> p1, @NotNull Promise<V> p2,
                                                     boolean link);

    /**
     * Combines two promises into a single promise that completes when both promises complete. If either
     * input promise completes exceptionally, the other promise will be cancelled and the output promise
     * will complete exceptionally.
     *
     * @param p1 the first promise
     * @param p2 the second promise
     * @return the combined promise
     */
    default <K, V> @NotNull Promise<Map.Entry<K, V>> combine(@NotNull Promise<K> p1, @NotNull Promise<V> p2) {
        return combine(p1, p2, true);
    }

    /**
     * Combines key-value pairs of inputs to promises into a single promise that completes with key-value
     * pairs of inputs to outputs when all promises complete. If {@code link} is {@code true}
     * and any promise completes exceptionally, the other promises will be cancelled and the output
     * promise will complete exceptionally.
     *
     * @param promises the input promises
     * @param link     whether to cancel all promises on any exceptional completions
     * @return the combined promise
     */
    <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Iterator<Map.Entry<K, Promise<V>>> promises,
                                                     int expectedSize, boolean link);

    /**
     * Combines key-value pairs of inputs to promises into a single promise that completes with key-value
     * pairs of inputs to outputs when all promises complete. If any promise completes exceptionally,
     * the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Collection<Map.Entry<K, Promise<V>>> promises,
                                                             boolean link) {
        return combineMapped(promises.iterator(), promises.size(), link);
    }

    /**
     * Combines key-value pairs of inputs to promises into a single promise that completes with key-value
     * pairs of inputs to outputs when all promises complete. If {@code link} is {@code true}
     * and any promise completes exceptionally, the other promises will be cancelled and the output
     * promise will complete exceptionally.
     *
     * @param promises the input promises
     * @param link     whether to cancel all promises on any exceptional completions
     * @return the combined promise
     */
    default <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Map<K, Promise<V>> promises,
                                                             boolean link) {
        return combineMapped(promises.entrySet().iterator(), promises.size(), link);
    }

    /**
     * Combines key-value pairs of inputs to promises into a single promise that completes with key-value
     * pairs of inputs to outputs when all promises complete. If any promise completes exceptionally,
     * the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Map<K, Promise<V>> promises) {
        return combineMapped(promises, true);
    }

    /**
     * Combines key-value pairs of inputs to promises into a single promise that completes with key-value
     * pairs of inputs to outputs when all promises complete. If any promise completes exceptionally,
     * the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Collection<Map.Entry<K, Promise<V>>> promises) {
        return combineMapped(promises, true);
    }

    /**
     * Combines key-value pairs of inputs to promises into a single promise that completes with key-value
     * pairs of inputs to outputs when all promises complete. If {@code link} is {@code true}
     * and any promise completes exceptionally, the other promises will be cancelled and the output
     * promise will complete exceptionally.
     *
     * @param promises the input promises
     * @param link     whether to cancel all promises on any exceptional completions
     * @return the combined promise
     */
    default <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Stream<Map.Entry<K, Promise<V>>> promises,
                                                             boolean link) {
        return combineMapped(promises.iterator(), PromiseUtil.estimateSize(promises), link);
    }

    /**
     * Combines key-value pairs of inputs to promises into a single promise that completes with key-value
     * pairs of inputs to outputs when all promises complete. If any promise completes exceptionally,
     * the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Stream<Map.Entry<K, Promise<V>>> promises) {
        return combineMapped(promises, true);
    }

    /**
     * Combines key-value pairs of inputs to promises into a single promise that completes with key-value
     * pairs of inputs to outputs when all promises complete. If {@code link} is {@code true}
     * and any promise completes exceptionally, the other promises will be cancelled and the output
     * promise will complete exceptionally.
     *
     * @param keys   the input keys
     * @param mapper the function to map keys to value promises
     * @param link   whether to cancel all promises on any exceptional completions
     * @return the combined promise
     */
    default <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Iterable<K> keys,
                                                             @NotNull Function<K, Promise<V>> mapper,
                                                             boolean link) {
        return combineMapped(StreamSupport.stream(keys.spliterator(), true)
            .map(k -> new AbstractMap.SimpleImmutableEntry<>(k, mapper.apply(k))), link);
    }

    /**
     * Combines key-value pairs of inputs to promises into a single promise that completes with key-value
     * pairs of inputs to outputs when all promises complete. If any promise completes exceptionally,
     * the output promise will complete exceptionally.
     *
     * @param keys   the input keys
     * @param mapper the function to map keys to value promises
     * @return the combined promise
     */
    default <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Iterable<K> keys,
                                                             @NotNull Function<K, Promise<V>> mapper) {
        return combineMapped(keys, mapper, true);
    }

    @Deprecated
    default <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, boolean link) {
        return combineMapped(promises, link);
    }

    @Deprecated
    default <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises) {
        return combineMapped(promises);
    }

    /**
     * Combines an iterator of promises into a single promise that completes with a list of results when all
     * promises complete. If {@code link} is {@code true} and any promise completes exceptionally, all
     * other promises will be cancelled and the output promise will complete exceptionally. If an exception
     * handler is present, promises that fail will not cause this behaviour, and instead the exception
     * handler will be called with the index that failed and the exception.
     *
     * @param promises the input promises
     * @param link     whether to cancel all promises on any exceptional completions
     * @return the combined promise
     */
    <V> @NotNull Promise<List<V>> combine(@NotNull Iterator<Promise<V>> promises, int expectedSize,
                                          boolean link);

    /**
     * Combines a collection of promises into a single promise that completes with a list of results when all
     * promises complete. If {@code link} is {@code true} and any promise completes exceptionally, all
     * other promises will be cancelled and the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @param link     whether to cancel all promises on any exceptional completions
     * @return the combined promise
     */
    default <V> @NotNull Promise<List<V>> combine(@NotNull Collection<Promise<V>> promises, boolean link) {
        return combine(promises.iterator(), promises.size(), link);
    }

    /**
     * Combines a collection of promises into a single promise that completes with a list of results when all
     * promises complete. If any promise completes exceptionally, the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <V> @NotNull Promise<List<V>> combine(@NotNull Collection<Promise<V>> promises) {
        return combine(promises, true);
    }

    /**
     * Combines a stream of promises into a single promise that completes with a list of results when all
     * promises complete. If {@code link} is {@code true} and any promise completes exceptionally, all
     * other promises will be cancelled and the output promise will complete exceptionally. If an exception
     * handler is present, promises that fail will not cause this behaviour, and instead the exception
     * handler will be called with the index that failed and the exception.
     *
     * @param promises the input promises
     * @param link     whether to cancel all promises on any exceptional completions
     * @return the combined promise
     */
    default <V> @NotNull Promise<List<V>> combine(@NotNull Stream<Promise<V>> promises, boolean link) {
        return combine(promises.iterator(), PromiseUtil.estimateSize(promises), link);
    }

    /**
     * Combines a stream of promises into a single promise that completes with a list of results when all
     * promises complete. The output promise will always complete successfully regardless of whether input
     * promises fail.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <V> @NotNull Promise<List<V>> combine(@NotNull Stream<Promise<V>> promises) {
        return combine(promises.iterator(), PromiseUtil.estimateSize(promises), true);
    }

    /**
     * Combines an iterator of promises into a single promise that completes with a list of results when all
     * promises complete. If {@code link} is {@code true} and any promise completes exceptionally, all
     * other promises will be cancelled. The output promise will always complete successfully regardless
     * of whether input promises fail.
     *
     * @param promises     the input promises
     * @param expectedSize the expected size of the list (used for optimization)
     * @param link         whether to cancel all promises on any exceptional completions
     * @return the combined promise
     */
    @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Iterator<Promise<?>> promises,
                                                            int expectedSize, boolean link);

    /**
     * Combines a collection of promises into a single promise that completes with a list of results when all
     * promises complete. If {@code link} is {@code true} and any promise completes exceptionally, all
     * other promises will be cancelled. The output promise will always complete successfully regardless
     * of whether input promises fail.
     *
     * @param promises the input promises
     * @param link     whether to cancel all promises on any exceptional completions
     * @return the combined promise
     */
    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Collection<Promise<?>> promises,
                                                                    boolean link) {
        return allSettled(promises.iterator(), promises.size(), link);
    }

    /**
     * Combines a collection of promises into a single promise that completes with a list of results when all
     * promises complete. If any promise completes exceptionally, all other promises will be cancelled.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Collection<Promise<?>> promises) {
        return allSettled(promises.iterator(), promises.size(), true);
    }

    /**
     * Combines a stream of promises into a single promise that completes with a list of results when all
     * promises complete. If {@code link} is {@code true} and any promise completes exceptionally, all
     * other promises will be cancelled. The output promise will always complete successfully regardless
     * of whether input promises fail.
     *
     * @param promises the input promises
     * @param link     whether to cancel all promises on any exceptional completions
     * @return the combined promise
     */
    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Stream<Promise<?>> promises,
                                                                    boolean link) {
        return allSettled(promises.iterator(), PromiseUtil.estimateSize(promises), link);
    }

    /**
     * Combines a stream of promises into a single promise that completes with a list of results when all
     * promises complete. If any promise completes exceptionally, all other promises will be cancelled.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Stream<Promise<?>> promises) {
        return allSettled(promises.iterator(), PromiseUtil.estimateSize(promises), true);
    }

    /**
     * Combines an array of promises into a single promise that completes with a list of results when all
     * promises complete. If {@code link} is {@code true} and any promise completes exceptionally, all
     * other promises will be cancelled. The output promise will always complete successfully regardless
     * of whether input promises fail.
     *
     * @param link     whether to cancel all promises on any exceptional completions
     * @param promises the input promises
     * @return the combined promise
     */
    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(boolean link,
                                                                    @NotNull Promise<?>... promises) {
        return allSettled(Arrays.asList(promises).iterator(), promises.length, link);
    }

    /**
     * Combines an array of promises into a single promise that completes with a list of results when all
     * promises complete. If any promise completes exceptionally, all other promises will be cancelled.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Promise<?>... promises) {
        return allSettled(Arrays.asList(promises).iterator(), promises.length, true);
    }

    /**
     * Combines an iterator of promises into a single promise that completes when all promises complete.
     * If {@code link} is {@code true} and any promise completes exceptionally, all other promises will
     * be cancelled and the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @param link     whether to cancel all promises on any exceptional completions
     * @return the combined promise
     */
    @NotNull Promise<Void> all(@NotNull Iterator<Promise<?>> promises, boolean link);

    /**
     * Combines an iterable of promises into a single promise that completes when all promises complete.
     * If {@code link} is {@code true} and any promise completes exceptionally, all other promises will
     * be cancelled and the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @param link     whether to cancel all promises on any exceptional completions
     * @return the combined promise
     */
    default @NotNull Promise<Void> all(@NotNull Iterable<Promise<?>> promises, boolean link) {
        return all(promises.iterator(), link);
    }

    /**
     * Combines an iterable of promises into a single promise that completes when all promises complete.
     * If any promise completes exceptionally, all other promises will be cancelled and the output
     * promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default @NotNull Promise<Void> all(@NotNull Iterable<Promise<?>> promises) {
        return all(promises.iterator(), true);
    }

    /**
     * Combines a stream of promises into a single promise that completes when all promises complete.
     * If {@code link} is {@code true} and any promise completes exceptionally, all other promises will
     * be cancelled and the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @param link     whether to cancel all promises on any exceptional completions
     * @return the combined promise
     */
    default @NotNull Promise<Void> all(@NotNull Stream<Promise<?>> promises, boolean link) {
        return all(promises.iterator(), link);
    }

    /**
     * Combines a stream of promises into a single promise that completes when all promises complete.
     * If any promise completes exceptionally, all other promises will be cancelled and the output
     * promise willcomplete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default @NotNull Promise<Void> all(@NotNull Stream<Promise<?>> promises) {
        return all(promises.iterator(), true);
    }

    /**
     * Combines an array of promises into a single promise that completes when all promises complete.
     * If {@code link} is {@code true} and any promise completes exceptionally, all other promises will
     * be cancelled
     * and the output promise will complete exceptionally.
     *
     * @param link     whether to cancel all promises on any exceptional completions
     * @param promises the input promises
     * @return the combined promise
     */
    default @NotNull Promise<Void> all(boolean link, @NotNull Promise<?>... promises) {
        return all(Arrays.asList(promises).iterator(), link);
    }

    /**
     * Combines an array of promises into a single promise that completes when all promises complete.
     * If any promise completes exceptionally, all other promises will be cancelled and the output
     * promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default @NotNull Promise<Void> all(@NotNull Promise<?>... promises) {
        return all(Arrays.asList(promises).iterator(), true);
    }

    /**
     * Combines an iterator of promises into a single promise that completes when the first promise
     * completes (successfully or exceptionally). If {@code cancelLosers} is {@code true}, all other
     * promises will be cancelled when the first promise
     * completes.
     *
     * @param promises     the input promises
     * @param cancelLosers whether to cancel the other promises when the first completes
     * @return the combined promise
     */
    <V> @NotNull Promise<V> race(@NotNull Iterator<Promise<V>> promises, boolean cancelLosers);

    /**
     * Combines an iterable of promises into a single promise that completes when the first promise
     * completes (successfully or exceptionally). All other promises will be cancelled when the first
     * promise completes.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <V> @NotNull Promise<V> race(@NotNull Iterable<Promise<V>> promises, boolean cancelLosers) {
        return race(promises.iterator(), cancelLosers);
    }

    /**
     * Combines an iterable of promises into a single promise that completes when the first promise
     * completes (successfully or exceptionally). All other promises will be cancelled when the first
     * promise completes.
     *
     * @param promises the input promises
     */
    default <V> @NotNull Promise<V> race(@NotNull Iterable<Promise<V>> promises) {
        return race(promises.iterator(), true);
    }

    /**
     * Combines a stream of promises into a single promise that completes when the first promise
     * completes (successfully or exceptionally). If {@code cancelLosers} is {@code true}, all other
     * promises will be cancelled when the first promise completes.
     *
     * @param promises     the input promises
     * @param cancelLosers whether to cancel the other promises when the first completes
     * @return the combined promise
     */
    default <V> @NotNull Promise<V> race(@NotNull Stream<Promise<V>> promises, boolean cancelLosers) {
        return race(promises.iterator(), cancelLosers);
    }

    /**
     * Combines a stream of promises into a single promise that completes when the first promise
     * completes (successfully or exceptionally). All other promises will be cancelled when the first
     * promise completes.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    default <V> @NotNull Promise<V> race(@NotNull Stream<Promise<V>> promises) {
        return race(promises.iterator(), true);
    }

}
