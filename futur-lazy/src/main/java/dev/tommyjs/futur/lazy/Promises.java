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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public final class Promises {

    private static final Logger LOGGER = LoggerFactory.getLogger(Promises.class);
    private static PromiseFactory factory = PromiseFactory.of(LOGGER, PromiseExecutor.virtualThreaded());

    public static void useFactory(@NotNull PromiseFactory factory) {
        Promises.factory = factory;
    }

    // Generated delegates to static factory

    /**
     * Creates a new uncompleted promise.
     *
     * @return the new promise
     */
    public static <T> @NotNull CompletablePromise<T> unresolved() {
        return factory.unresolved();
    }

    /**
     * Creates a new promise, completed with the given value.
     *
     * @param value the value to complete the promise with
     * @return the new promise
     */
    public static <T> @NotNull Promise<T> resolve(T value) {
        return factory.resolve(value);
    }

    /**
     * Creates a new promise, completed with {@code null}.
     *
     * @return the new promise
     * @apiNote This method is often useful for starting promise chains.
     */
    public static @NotNull Promise<Void> start() {
        return factory.start();
    }

    /**
     * Creates a new promise, completed exceptionally with the given error.
     *
     * @param error the error to complete the promise with
     * @return the new promise
     */
    public static <T> @NotNull Promise<T> error(@NotNull Throwable error) {
        return factory.error(error);
    }

    /**
     * Creates a new promise backed by the given completion and future.
     * The promise will be completed upon completion of the {@link CompletionStage}
     * and the {@link Future} will be cancelled upon cancellation of the promise.
     *
     * @param completion the completion stage to wrap
     * @param future     the future to wrap
     * @return the new promise
     */
    public static <T> @NotNull Promise<T> wrap(@NotNull CompletionStage<T> completion, @Nullable Future<T> future) {
        return factory.wrap(completion, future);
    }

    /**
     * Creates a new promise backed by the given future.
     * The promise will be completed upon completion of the {@link CompletableFuture}
     * and the {@link CompletableFuture} will be cancelled upon cancellation of the promise.
     *
     * @param future the future to wrap
     * @return the new promise
     */
    public static <T> @NotNull Promise<T> wrap(@NotNull CompletableFuture<T> future) {
        return factory.wrap(future);
    }

    /**
     * Combines two promises into a single promise that resolves when both promises are completed.
     * If either input promise completes exceptionally, the other promise will be cancelled
     * and the output promise will complete exceptionally.
     *
     * @param p1 the first promise
     * @param p2 the second promise
     * @return the combined promise
     */
    public static <K, V> @NotNull Promise<Map.Entry<K, V>> combine(@NotNull Promise<K> p1, @NotNull Promise<V> p2) {
        return factory.combine(p1, p2);
    }

    /**
     * Combines key-value pairs of promises into a single promise that completes
     * when all promises are completed, with the results mapped by their keys.
     * If any promise completes exceptionally, the other promises will be cancelled
     * and the combined promise will complete exceptionally.
     *
     * @param promises     the input promises
     * @param expectedSize the expected size of the iterator (used for optimization)
     * @return the combined promise
     */
    public static <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Iterator<Map.Entry<K, Promise<V>>> promises,
                                                                   int expectedSize) {
        return factory.combineMapped(promises, expectedSize);
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
    public static <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Spliterator<Map.Entry<K, Promise<V>>> promises) {
        return factory.combineMapped(promises);
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
    public static <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Stream<Map.Entry<K, Promise<V>>> promises) {
        return factory.combineMapped(promises);
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
    public static <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Iterable<Map.Entry<K, Promise<V>>> promises) {
        return factory.combineMapped(promises);
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
    public static <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Map.Entry<K, Promise<V>>... promises) {
        return factory.combineMapped(promises);
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
    public static <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Map<K, Promise<V>> promises) {
        return factory.combineMapped(promises);
    }

    /**
     * Combines key-value pairs of promises into a single promise that completes
     * when all promises are completed, with the results mapped by their keys.
     * If any promise completes exceptionally, the other promises will be cancelled
     * and the combined promise will complete exceptionally.
     *
     * @param keys   the keys to map to promises
     * @param mapper the function to map keys to promises
     * @return the combined promise
     */
    public static <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Stream<K> keys,
                                                                   @NotNull Function<K, Promise<V>> mapper) {
        return factory.combineMapped(keys, mapper);
    }

    /**
     * Combines key-value pairs of promises into a single promise that completes
     * when all promises are completed, with the results mapped by their keys.
     * If any promise completes exceptionally, the other promises will be cancelled
     * and the combined promise will complete exceptionally.
     *
     * @param keys   the keys to map to promises
     * @param mapper the function to map keys to promises
     * @return the combined promise
     */
    public static <K, V> @NotNull Promise<Map<K, V>> combineMapped(@NotNull Iterable<K> keys,
                                                                   @NotNull Function<K, Promise<V>> mapper) {
        return factory.combineMapped(keys, mapper);
    }

    /**
     * @deprecated Use combineMapped instead.
     */
    @Deprecated
    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises) {
        return factory.combine(promises);
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises
     * are completed, with a list of results in the original order.
     * If any promise completes exceptionally, all other promises will be cancelled
     * and the combined promise will complete exceptionally.
     *
     * @param promises     the input promises
     * @param expectedSize the expected size of the iterator (used for optimization)
     * @return the combined promise
     */
    public static <V> @NotNull Promise<List<V>> combine(@NotNull Iterator<Promise<V>> promises, int expectedSize) {
        return factory.combine(promises, expectedSize);
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
    public static <V> @NotNull Promise<List<V>> combine(@NotNull Spliterator<Promise<V>> promises) {
        return factory.combine(promises);
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
    public static <V> @NotNull Promise<List<V>> combine(@NotNull Stream<Promise<V>> promises) {
        return factory.combine(promises);
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
    public static <V> @NotNull Promise<List<V>> combine(@NotNull Iterable<Promise<V>> promises) {
        return factory.combine(promises);
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
    public static <V> @NotNull Promise<List<V>> combine(@NotNull Promise<V>... promises) {
        return factory.combine(promises);
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises
     * are completed, with a list of completions in the original order.
     *
     * @param promises     the input promises
     * @param expectedSize the expected size of the iterator (used for optimization)
     * @return the combined promise
     */
    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Iterator<Promise<?>> promises,
                                                                          int expectedSize) {
        return factory.allSettled(promises, expectedSize);
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises
     * are completed, with a list of completions in the original order.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Spliterator<Promise<?>> promises) {
        return factory.allSettled(promises);
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises
     * are completed, with a list of completions in the original order.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Stream<Promise<?>> promises) {
        return factory.allSettled(promises);
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises
     * are completed, with a list of completions in the original order.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Iterable<Promise<?>> promises) {
        return factory.allSettled(promises);
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises
     * are completed, with a list of completions in the original order.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Promise<?>... promises) {
        return factory.allSettled(promises);
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises complete.
     * If any promise completes exceptionally, all other promises will be cancelled
     * and the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static @NotNull Promise<Void> all(@NotNull Iterator<Promise<?>> promises) {
        return factory.all(promises);
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises complete.
     * If any promise completes exceptionally, all other promises will be cancelled
     * and the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static @NotNull Promise<Void> all(@NotNull Stream<Promise<?>> promises) {
        return factory.all(promises);
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises complete.
     * If any promise completes exceptionally, all other promises will be cancelled
     * and the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static @NotNull Promise<Void> all(@NotNull Iterable<Promise<?>> promises) {
        return factory.all(promises);
    }

    /**
     * Combines multiple promises into a single promise that completes when all promises complete.
     * If any promise completes exceptionally, all other promises will be cancelled
     * and the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static @NotNull Promise<Void> all(@NotNull Promise<?>... promises) {
        return factory.all(promises);
    }

    /**
     * Combines multiple promises into a single promise that completes when any promise is completed.
     * If {@code ignoreErrors} is {@code false} and the first promise completed exceptionally, the
     * combined promise will also complete exceptionally. Otherwise, the combined promise will wait for a
     * successful completion or complete with {@code null} if all promises complete exceptionally.
     * Additionally, if {@code cancelLosers} is {@code true}, the other promises will be cancelled
     * once the combined promise is completed.
     *
     * @param promises     the input promises
     * @param ignoreErrors whether to ignore promises that complete exceptionally
     * @return the combined promise
     */
    public static <V> @NotNull Promise<V> race(@NotNull Iterator<Promise<V>> promises, boolean ignoreErrors) {
        return factory.race(promises, ignoreErrors);
    }

    /**
     * Combines multiple promises into a single promise that completes when any promise is completed.
     * If {@code ignoreErrors} is {@code false} and the first promise completed exceptionally, the
     * combined promise will also complete exceptionally. Otherwise, the combined promise will wait for a
     * successful completion or complete with {@code null} if all promises complete exceptionally.
     * Additionally, The other promises will be cancelled once the combined promise is completed.
     *
     * @param promises     the input promises
     * @param ignoreErrors whether to ignore promises that complete exceptionally
     * @return the combined promise
     */
    public static <V> @NotNull Promise<V> race(@NotNull Stream<Promise<V>> promises, boolean ignoreErrors) {
        return factory.race(promises, ignoreErrors);
    }

    /**
     * Combines multiple promises into a single promise that completes when any promise is completed.
     * If the first promise completed exceptionally, the combined promise will also complete exceptionally.
     * Additionally, the other promises will be cancelled once the combined promise is completed.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static <V> @NotNull Promise<V> race(@NotNull Stream<Promise<V>> promises) {
        return factory.race(promises);
    }

    /**
     * Combines multiple promises into a single promise that completes when any promise is completed.
     * If {@code ignoreErrors} is {@code false} and the first promise completed exceptionally, the
     * combined promise will also complete exceptionally. Otherwise, the combined promise will wait for a
     * successful completion or complete with {@code null} if all promises complete exceptionally.
     * Additionally, The other promises will be cancelled once the combined promise is completed.
     *
     * @param promises     the input promises
     * @param ignoreErrors whether to ignore promises that complete exceptionally
     * @return the combined promise
     */
    public static <V> @NotNull Promise<V> race(@NotNull Iterable<Promise<V>> promises, boolean ignoreErrors) {
        return factory.race(promises, ignoreErrors);
    }

    /**
     * Combines multiple promises into a single promise that completes when any promise is completed.
     * If the first promise completed exceptionally, the combined promise will also complete exceptionally.
     * Additionally, the other promises will be cancelled once the combined promise is completed.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static <V> @NotNull Promise<V> race(@NotNull Iterable<Promise<V>> promises) {
        return factory.race(promises);
    }

    /**
     * Combines multiple promises into a single promise that completes when any promise is completed.
     * If {@code ignoreErrors} is {@code false} and the first promise completed exceptionally, the
     * combined promise will also complete exceptionally. Otherwise, the combined promise will wait for a
     * successful completion or complete with {@code null} if all promises complete exceptionally.
     * Additionally, The other promises will be cancelled once the combined promise is completed.
     *
     * @param promises     the input promises
     * @param ignoreErrors whether to ignore promises that complete exceptionally
     * @return the combined promise
     */
    public static <V> @NotNull Promise<V> race(boolean ignoreErrors, @NotNull Promise<V>... promises) {
        return factory.race(ignoreErrors, promises);
    }

    /**
     * Combines multiple promises into a single promise that completes when any promise is completed.
     * If the first promise completed exceptionally, the combined promise will also complete exceptionally.
     * Additionally, the other promises will be cancelled once the combined promise is completed.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static <V> @NotNull Promise<V> race(@NotNull Promise<V>... promises) {
        return factory.race(promises);
    }

}