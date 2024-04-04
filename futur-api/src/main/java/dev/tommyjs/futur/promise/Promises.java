package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.function.ExceptionalFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * @deprecated Use PromiseFactory instance methods instead.
 */
@Deprecated
public class Promises {

    public static <K, V> @NotNull Promise<Map.Entry<K, V>> combine(@NotNull Promise<K> p1, @NotNull Promise<V> p2, PromiseFactory factory) {
        return factory.combine(p1, p2);
    }

    public static <K, V> @NotNull Promise<Map.Entry<K, V>> combine(@NotNull Promise<K> p1, @NotNull Promise<V> p2) {
        return combine(p1, p2, p1.getFactory());
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, long timeout, @Nullable BiConsumer<K, Throwable> exceptionHandler, PromiseFactory factory) {
        return factory.combine(promises, exceptionHandler).timeout(timeout);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, long timeout, boolean strict, PromiseFactory factory) {
        return combine(promises, timeout, strict ? null : (_k, _v) -> {}, factory);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, long timeout, PromiseFactory factory) {
        return combine(promises, timeout, true, factory);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, PromiseFactory factory) {
        return combine(promises, 1500L, true, factory);
    }

    public static <V> @NotNull Promise<List<V>> combine(@NotNull List<Promise<V>> promises, long timeout, boolean strict, PromiseFactory factory) {
        return factory.combine(promises, strict ? null : (_v) -> {}).timeout(timeout);
    }

    public static <V> @NotNull Promise<List<V>> combine(@NotNull List<Promise<V>> promises, long timeout, PromiseFactory factory) {
        return combine(promises, timeout, true, factory);
    }

    public static <V> @NotNull Promise<List<V>> combine(@NotNull List<Promise<V>> promises, PromiseFactory factory) {
        return combine(promises, 1500L, true, factory);
    }

    public static @NotNull Promise<Void> all(@NotNull List<Promise<?>> promises, PromiseFactory factory) {
        return factory.all(promises);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Collection<K> keys, @NotNull ExceptionalFunction<K, V> mapper, long timeout, boolean strict, PromiseFactory factory) {
        Map<K, Promise<V>> promises = new HashMap<>();
        for (K key : keys) {
            Promise<V> promise = factory.resolve(key).thenApplyAsync(mapper);
            promises.put(key, promise);
        }

        return combine(promises, timeout, strict, factory);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Collection<K> keys, @NotNull ExceptionalFunction<K, V> mapper, long timeout, PromiseFactory factory) {
        return combine(keys, mapper, timeout, true, factory);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Collection<K> keys, @NotNull ExceptionalFunction<K, V> mapper, PromiseFactory factory) {
        return combine(keys, mapper, 1500L, true, factory);
    }

    public static @NotNull Promise<Void> erase(@NotNull Promise<?> p, PromiseFactory factory) {
        return factory.erase(p);
    }

    public static @NotNull Promise<Void> erase(@NotNull Promise<?> p) {
        return erase(p, p.getFactory());
    }

    public static <T> @NotNull Promise<T> wrap(@NotNull CompletableFuture<T> future, PromiseFactory factory) {
        return factory.wrap(future);
    }

}