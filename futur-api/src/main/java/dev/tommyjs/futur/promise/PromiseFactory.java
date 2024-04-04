package dev.tommyjs.futur.promise;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface PromiseFactory {

    @NotNull Logger getLogger();

    <T> @NotNull Promise<T> unresolved();

    <K, V> @NotNull Promise<Map.Entry<K, V>> combine(@NotNull Promise<K> p1, @NotNull Promise<V> p2);

    <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, @Nullable BiConsumer<K, Throwable> exceptionHandler);

    default <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises) {
        return combine(promises, null);
    }

    <V> @NotNull Promise<List<V>> combine(@NotNull Iterable<Promise<V>> promises, @Nullable Consumer<Throwable> exceptionHandler);

    default <V> @NotNull Promise<List<V>> combine(@NotNull Iterable<Promise<V>> promises) {
        return combine(promises, null);
    }

    @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Iterable<Promise<?>> promiseIterable);

    default @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Promise<?>... promiseArray) {
        return allSettled(Arrays.asList(promiseArray));
    }

    @NotNull Promise<Void> all(@NotNull Iterable<Promise<?>> promiseIterable);

    default @NotNull Promise<Void> all(@NotNull Promise<?>... promiseArray) {
        return all(Arrays.asList(promiseArray));
    }

    <T> @NotNull Promise<T> wrap(@NotNull CompletableFuture<T> future);

    <T> @NotNull Promise<T> wrap(@NotNull Mono<T> mono);

    <T> @NotNull Promise<T> resolve(T value);

    default @NotNull Promise<Void> start() {
        return resolve(null);
    }

    <T> @NotNull Promise<T> error(@NotNull Throwable error);

    @NotNull Promise<Void> erase(@NotNull Promise<?> p);

}
