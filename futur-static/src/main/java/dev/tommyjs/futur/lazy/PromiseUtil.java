package dev.tommyjs.futur.lazy;

import dev.tommyjs.futur.promise.Promise;
import dev.tommyjs.futur.promise.PromiseCompletion;
import dev.tommyjs.futur.promise.PromiseFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class PromiseUtil {

    private static PromiseFactory pfac = StaticPromiseFactory.INSTANCE;

    public static @NotNull Logger getLogger() {
        return pfac.getLogger();
    }

    public static void setPromiseFactory(PromiseFactory pfac) {
        PromiseUtil.pfac = pfac;
    }

    public static @NotNull <T> Promise<T> unresolved() {
        return pfac.unresolved();
    }

    public static @NotNull <K, V> Promise<Map.Entry<K, V>> combine(@NotNull Promise<K> p1, @NotNull Promise<V> p2) {
        return pfac.combine(p1, p2);
    }

    public static @NotNull <K, V> Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, @Nullable BiConsumer<K, Throwable> exceptionHandler) {
        return pfac.combine(promises, exceptionHandler);
    }

    public static @NotNull <K, V> Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises) {
        return pfac.combine(promises);
    }

    public static @NotNull <V> Promise<List<V>> combine(@NotNull Iterable<Promise<V>> promises, @Nullable Consumer<Throwable> exceptionHandler) {
        return pfac.combine(promises, exceptionHandler);
    }

    public static @NotNull <V> Promise<List<V>> combine(@NotNull Iterable<Promise<V>> promises) {
        return pfac.combine(promises);
    }

    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Iterable<Promise<?>> promiseIterable) {
        return pfac.allSettled(promiseIterable);
    }

    public static @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Promise<?>... promiseArray) {
        return pfac.allSettled(promiseArray);
    }

    public static @NotNull Promise<Void> all(@NotNull Iterable<Promise<?>> promiseIterable) {
        return pfac.all(promiseIterable);
    }

    public static @NotNull Promise<Void> all(@NotNull Promise<?>... promiseArray) {
        return pfac.all(promiseArray);
    }

    public static @NotNull <T> Promise<T> wrap(@NotNull CompletableFuture<T> future) {
        return pfac.wrap(future);
    }

    public static @NotNull <T> Promise<T> wrap(@NotNull Mono<T> mono) {
        return pfac.wrap(mono);
    }

    public static @NotNull <T> Promise<T> resolve(T value) {
        return pfac.resolve(value);
    }

    public static @NotNull <T> Promise<T> error(@NotNull Throwable error) {
        return pfac.error(error);
    }

    public static @NotNull Promise<Void> erase(@NotNull Promise<?> p) {
        return pfac.erase(p);
    }

    public static @NotNull Promise<Void> start() {
        return pfac.start();
    }

}