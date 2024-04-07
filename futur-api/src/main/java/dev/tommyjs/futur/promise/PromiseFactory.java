package dev.tommyjs.futur.promise;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RFuture;
import org.slf4j.Logger;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public interface PromiseFactory {

    @NotNull Logger getLogger();

    <T> @NotNull Promise<T> unresolved();

    <K, V> @NotNull Promise<Map.Entry<K, V>> combine(boolean propagateCancel, @NotNull Promise<K> p1, @NotNull Promise<V> p2);

    default <K, V> @NotNull Promise<Map.Entry<K, V>> combine(@NotNull Promise<K> p1, @NotNull Promise<V> p2) {
        return combine(false, p1, p2);
    }

    <K, V> @NotNull Promise<Map<K, V>> combine(boolean propagateCancel, @NotNull Map<K, Promise<V>> promises, @Nullable BiConsumer<K, Throwable> exceptionHandler);

    default <K, V> @NotNull Promise<Map<K, V>> combine(boolean propagateCancel, @NotNull Map<K, Promise<V>> promises) {
        return combine(propagateCancel, promises, null);
    }

    default <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, @Nullable BiConsumer<K, Throwable> exceptionHandler) {
        return combine(false, promises, exceptionHandler);
    }

    default <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises) {
        return combine(promises, null);
    }

    <V> @NotNull Promise<List<V>> combine(boolean propagateCancel, @NotNull Iterable<Promise<V>> promises, @Nullable BiConsumer<Integer, Throwable> exceptionHandler);

    default <V> @NotNull Promise<List<V>> combine(boolean propagateCancel, @NotNull Iterable<Promise<V>> promises) {
        return combine(propagateCancel, promises, null);
    }

    default <V> @NotNull Promise<List<V>> combine(@NotNull Iterable<Promise<V>> promises, @Nullable BiConsumer<Integer, Throwable> exceptionHandler) {
        return combine(false, promises, exceptionHandler);
    }

    default <V> @NotNull Promise<List<V>> combine(@NotNull Iterable<Promise<V>> promises) {
        return combine(promises, null);
    }

    @NotNull Promise<List<PromiseCompletion<?>>> allSettled(boolean propagateCancel, @NotNull Iterable<Promise<?>> promiseIterable);

    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Iterable<Promise<?>> promiseIterable) {
        return allSettled(false, promiseIterable);
    }

    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(boolean propagateCancel, @NotNull Promise<?>... promiseArray) {
        return allSettled(propagateCancel, Arrays.asList(promiseArray));
    }

    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Promise<?>... promiseArray) {
        return allSettled(false, promiseArray);
    }

    @NotNull Promise<Void> all(boolean propagateCancel, @NotNull Iterable<Promise<?>> promiseIterable);

    default @NotNull Promise<Void> all(@NotNull Iterable<Promise<?>> promiseIterable) {
        return all(false, promiseIterable);
    }

    default @NotNull Promise<Void> all(boolean propagateCancel, @NotNull Promise<?>... promiseArray) {
        return all(propagateCancel, Arrays.asList(promiseArray));
    }

    default @NotNull Promise<Void> all(@NotNull Promise<?>... promiseArray) {
        return all(false, promiseArray);
    }

    /**
     * @apiNote Even with cancelRaceLosers, it is not guaranteed that only one promise will complete.
     */
    <V> @NotNull Promise<V> race(boolean cancelRaceLosers, @NotNull Iterable<Promise<V>> promises);

    default <V> @NotNull Promise<V> race(@NotNull Iterable<Promise<V>> promises) {
        return race(false, promises);
    }

    <T> @NotNull Promise<T> wrapMono(@NotNull Mono<T> mono);

    <T> @NotNull Promise<T> wrapRedisson(@NotNull RFuture<T> future);

    <T> @NotNull Promise<T> wrap(@NotNull CompletableFuture<T> future);

    default @NotNull Promise<Void> start() {
        return resolve(null);
    }

    <T> @NotNull Promise<T> resolve(T value);

    <T> @NotNull Promise<T> error(@NotNull Throwable error);

}
