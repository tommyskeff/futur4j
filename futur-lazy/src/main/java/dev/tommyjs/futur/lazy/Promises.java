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
     * Creates a new promise backed by the given future. The promise will be completed upon completion
     * of the future.
     *
     * @param future the future to wrap
     * @return the new promise
     */
    public static <T> @NotNull Promise<T> wrap(@NotNull CompletableFuture<T> future) {
        return factory.wrap(future);
    }

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
    public static <K, V> @NotNull Promise<Map.Entry<K, V>> combine(@NotNull Promise<K> p1, @NotNull Promise<V> p2,
                                                                   boolean link) {
        return factory.combine(p1, p2, link);
    }

    /**
     * Combines two promises into a single promise that completes when both promises complete. If either
     * input promise completes exceptionally, the other promise will be cancelled and the output promise
     * will complete exceptionally.
     *
     * @param p1 the first promise
     * @param p2 the second promise
     * @return the combined promise
     */
    public static <K, V> @NotNull Promise<Map.Entry<K, V>> combine(@NotNull Promise<K> p1, @NotNull Promise<V> p2) {
        return factory.combine(p1, p2);
    }

    /**
     * Combines key-value pairs of inputs to promises into a single promise that completes with key-value
     * pairs of inputs to outputs when all promises complete. If {@code link} is {@code true}
     * and any promise completes exceptionally, the other promises will be cancelled and the output
     * promise will complete exceptionally. If an exception handler is present, promises that fail
     * will not cause this behaviour, and instead the exception handler will be called with the key
     * that failed and the exception.
     *
     * @param promises         the input promises
     * @param exceptionHandler the exception handler
     * @param link             whether to cancel all promises on any exceptional completions
     * @return the combined promise
     */
    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises,
                                                             @Nullable BiConsumer<K, Throwable> exceptionHandler,
                                                             boolean link) {
        return factory.combine(promises, exceptionHandler, link);
    }

    /**
     * Combines key-value pairs of inputs to promises into a single promise that completes with key-value
     * pairs of inputs to outputs when all promises complete. If any promise completes exceptionally,
     * the exception handler will be called with the key that failed and the exception. The output promise
     * will always complete successfully regardless of whether input promises fail.
     *
     * @param promises         the input promises
     * @param exceptionHandler the exception handler
     * @return the combined promise
     */
    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises,
                                                             @NotNull BiConsumer<K, Throwable> exceptionHandler) {
        return factory.combine(promises, exceptionHandler);
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
    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, boolean link) {
        return factory.combine(promises, link);
    }

    /**
     * Combines key-value pairs of inputs to promises into a single promise that completes with key-value
     * pairs of inputs to outputs when all promises complete. If any promise completes exceptionally,
     * the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises) {
        return factory.combine(promises);
    }

    /**
     * Combines an iterator of promises into a single promise that completes with a list of results when all
     * promises complete. If {@code link} is {@code true} and any promise completes exceptionally, all
     * other promises will be cancelled and the output promise will complete exceptionally. If an exception
     * handler is present, promises that fail will not cause this behaviour, and instead the exception
     * handler will be called with the index that failed and the exception.
     *
     * @param promises         the input promises
     * @param exceptionHandler the exception handler
     * @param link             whether to cancel all promises on any exceptional completions
     * @return the combined promise
     */
    public static <V> @NotNull Promise<List<V>> combine(@NotNull Iterator<Promise<V>> promises, int expectedSize,
                                                        @Nullable BiConsumer<Integer, Throwable> exceptionHandler,
                                                        boolean link) {
        return factory.combine(promises, expectedSize, exceptionHandler, link);
    }

    /**
     * Combines a collection of promises into a single promise that completes with a list of results when all
     * promises complete. If any promise completes exceptionally, the exception handler will be called with
     * the index that failed and the exception. The output promise will always complete successfully regardless
     * of whether input promises fail.
     *
     * @param promises         the input promises
     * @param exceptionHandler the exception handler
     * @return the combined promise
     */
    public static <V> @NotNull Promise<List<V>> combine(@NotNull Collection<Promise<V>> promises,
                                                        @NotNull BiConsumer<Integer, Throwable> exceptionHandler,
                                                        boolean link) {
        return factory.combine(promises, exceptionHandler, link);
    }

    /**
     * Combines a collection of promises into a single promise that completes with a list of results when all
     * promises complete. If any promise completes exceptionally, the exception handler will be called with
     * the index that failed and the exception. The output promise will always complete successfully regardless
     * of whether input promises fail.
     *
     * @param promises         the input promises
     * @param exceptionHandler the exception handler
     * @return the combined promise
     */
    public static <V> @NotNull Promise<List<V>> combine(@NotNull Collection<Promise<V>> promises,
                                                        @NotNull BiConsumer<Integer, Throwable> exceptionHandler) {
        return factory.combine(promises, exceptionHandler);
    }

    /**
     * Combines a collection of promises into a single promise that completes with a list of results when all
     * promises complete. If {@code link} is {@code true} and any promise completes exceptionally, all
     * other promises will be cancelled and the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @param link     whether to cancel all promises on any exceptional completions
     * @return the combined promise
     */
    public static <V> @NotNull Promise<List<V>> combine(@NotNull Collection<Promise<V>> promises, boolean link) {
        return factory.combine(promises, link);
    }

    /**
     * Combines a collection of promises into a single promise that completes with a list of results when all
     * promises complete. If any promise completes exceptionally, the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static <V> @NotNull Promise<List<V>> combine(@NotNull Collection<Promise<V>> promises) {
        return factory.combine(promises);
    }

    /**
     * Combines a stream of promises into a single promise that completes with a list of results when all
     * promises complete. If {@code link} is {@code true} and any promise completes exceptionally, all
     * other promises will be cancelled and the output promise will complete exceptionally. If an exception
     * handler is present, promises that fail will not cause this behaviour, and instead the exception
     * handler will be called with the index that failed and the exception.
     *
     * @param promises         the input promises
     * @param exceptionHandler the exception handler
     * @param link             whether to cancel all promises on any exceptional completions
     * @return the combined promise
     */
    public static <V> @NotNull Promise<List<V>> combine(@NotNull Stream<Promise<V>> promises,
                                                        @NotNull BiConsumer<Integer, Throwable> exceptionHandler,
                                                        boolean link) {
        return factory.combine(promises, exceptionHandler, link);
    }

    /**
     * Combines a stream of promises into a single promise that completes with a list of results when all
     * promises complete. If any promise completes exceptionally, the exception handler will be called with
     * the index that failed and the exception. The output promise will always complete successfully regardless
     * of whether input promises fail.
     *
     * @param promises         the input promises
     * @param exceptionHandler the exception handler
     * @return the combined promise
     */
    public static <V> @NotNull Promise<List<V>> combine(@NotNull Stream<Promise<V>> promises,
                                                        @NotNull BiConsumer<Integer, Throwable> exceptionHandler) {
        return factory.combine(promises, exceptionHandler);
    }

    /**
     * Combines a stream of promises into a single promise that completes with a list of results when all
     * promises complete. If {@code link} is {@code true} and any promise completes exceptionally, all
     * other promises will be cancelled and the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @param link     whether to cancel all promises on any exceptional completions
     * @return the combined promise
     */
    public static <V> @NotNull Promise<List<V>> combine(@NotNull Stream<Promise<V>> promises, boolean link) {
        return factory.combine(promises, link);
    }

    /**
     * Combines a stream of promises into a single promise that completes with a list of results when all
     * promises complete. If any promise completes exceptionally, the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static <V> @NotNull Promise<List<V>> combine(@NotNull Stream<Promise<V>> promises) {
        return factory.combine(promises);
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
    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Iterator<Promise<?>> promises,
                                                                          int expectedSize, boolean link) {
        return factory.allSettled(promises, expectedSize, link);
    }

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
    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Collection<Promise<?>> promises,
                                                                          boolean link) {
        return factory.allSettled(promises, link);
    }

    /**
     * Combines a collection of promises into a single promise that completes with a list of results when all
     * promises complete. If any promise completes exceptionally, all other promises will be cancelled.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Collection<Promise<?>> promises) {
        return factory.allSettled(promises);
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
    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Stream<Promise<?>> promises,
                                                                          boolean link) {
        return factory.allSettled(promises, link);
    }

    /**
     * Combines a stream of promises into a single promise that completes with a list of results when all
     * promises complete. If any promise completes exceptionally, all other promises will be cancelled.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Stream<Promise<?>> promises) {
        return factory.allSettled(promises);
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
    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(boolean link,
                                                                          @NotNull Promise<?>... promises) {
        return factory.allSettled(link, promises);
    }

    /**
     * Combines an array of promises into a single promise that completes with a list of results when all
     * promises complete. If any promise completes exceptionally, all other promises will be cancelled.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Promise<?>... promises) {
        return factory.allSettled(promises);
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
    public static @NotNull Promise<Void> all(@NotNull Iterator<Promise<?>> promises, boolean link) {
        return factory.all(promises, link);
    }

    /**
     * Combines an iterable of promises into a single promise that completes when all promises complete.
     * If {@code link} is {@code true} and any promise completes exceptionally, all other promises will
     * be cancelled and the output promise will complete exceptionally.
     *
     * @param promises the input promises
     * @param link     whether to cancel all promises on any exceptional completions
     * @return the combined promise
     */
    public static @NotNull Promise<Void> all(@NotNull Iterable<Promise<?>> promises, boolean link) {
        return factory.all(promises, link);
    }

    /**
     * Combines an iterable of promises into a single promise that completes when all promises complete.
     * If any promise completes exceptionally, all other promises will be cancelled and the output
     * promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static @NotNull Promise<Void> all(@NotNull Iterable<Promise<?>> promises) {
        return factory.all(promises);
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
    public static @NotNull Promise<Void> all(@NotNull Stream<Promise<?>> promises, boolean link) {
        return factory.all(promises, link);
    }

    /**
     * Combines a stream of promises into a single promise that completes when all promises complete.
     * If any promise completes exceptionally, all other promises will be cancelled and the output
     * promise willcomplete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static @NotNull Promise<Void> all(@NotNull Stream<Promise<?>> promises) {
        return factory.all(promises);
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
    public static @NotNull Promise<Void> all(boolean link, @NotNull Promise<?>... promises) {
        return factory.all(link, promises);
    }

    /**
     * Combines an array of promises into a single promise that completes when all promises complete.
     * If any promise completes exceptionally, all other promises will be cancelled and the output
     * promise will complete exceptionally.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static @NotNull Promise<Void> all(@NotNull Promise<?>... promises) {
        return factory.all(promises);
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
    public static <V> @NotNull Promise<V> race(@NotNull Iterator<Promise<V>> promises, boolean cancelLosers) {
        return factory.race(promises, cancelLosers);
    }

    /**
     * Combines an iterable of promises into a single promise that completes when the first promise
     * completes (successfully or exceptionally). All other promises will be cancelled when the first
     * promise completes.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static <V> @NotNull Promise<V> race(@NotNull Iterable<Promise<V>> promises, boolean cancelLosers) {
        return factory.race(promises, cancelLosers);
    }

    /**
     * Combines an iterable of promises into a single promise that completes when the first promise
     * completes (successfully or exceptionally). All other promises will be cancelled when the first
     * promise completes.
     *
     * @param promises the input promises
     */
    public static <V> @NotNull Promise<V> race(@NotNull Iterable<Promise<V>> promises) {
        return factory.race(promises);
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
    public static <V> @NotNull Promise<V> race(@NotNull Stream<Promise<V>> promises, boolean cancelLosers) {
        return factory.race(promises, cancelLosers);
    }

    /**
     * Combines a stream of promises into a single promise that completes when the first promise
     * completes (successfully or exceptionally). All other promises will be cancelled when the first
     * promise completes.
     *
     * @param promises the input promises
     * @return the combined promise
     */
    public static <V> @NotNull Promise<V> race(@NotNull Stream<Promise<V>> promises) {
        return factory.race(promises);
    }

}