package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.function.ExceptionalFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Promises {

    public static <K, V> @NotNull Promise<Map.Entry<K, V>> combine(@NotNull Promise<K> p1, @NotNull Promise<V> p2) {
        Promise<Map.Entry<K, V>> promise = new Promise<>();
        p1.addListener(ctx -> {
            if (ctx.isError()) {
                //noinspection ConstantConditions
                promise.completeExceptionally(ctx.getException());
                return;
            }

            p2.addListener(ctx1 -> {
                if (ctx1.isError()) {
                    //noinspection ConstantConditions
                    promise.completeExceptionally(ctx1.getException());
                    return;
                }

                Map.Entry<K, V> result = new AbstractMap.SimpleEntry<>(ctx.getResult(), ctx1.getResult());
                promise.complete(result);
            });
        });

        return promise;
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, long timeout, @Nullable BiConsumer<K, Throwable> exceptionHandler) {
        Map<K, V> map = new HashMap<>();
        if (promises.isEmpty()) return Promise.resolve(map);

        ReentrantLock lock = new ReentrantLock();

        Promise<Map<K, V>> promise = new Promise<>();
        for (Map.Entry<K, Promise<V>> entry : promises.entrySet()) {
            entry.getValue().addListener((ctx) -> {
                lock.lock();

                try {
                    if (ctx.isError()) {
                        if (exceptionHandler == null) {
                            //noinspection ConstantConditions
                            promise.completeExceptionally(ctx.getException());
                        } else {
                            exceptionHandler.accept(entry.getKey(), ctx.getException());
                            map.put(entry.getKey(), null);
                        }
                    } else {
                        map.put(entry.getKey(), ctx.getResult());
                    }
                    if (map.size() == promises.size()) promise.complete(map);
                } finally {
                    lock.unlock();
                }
            });
        }
        return promise.timeout(timeout);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, long timeout, boolean strict) {
        return combine(promises, timeout, strict ? null : (_k, _v) -> {});
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, long timeout) {
        return combine(promises, timeout, true);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises) {
        return combine(promises, 1500L, true);
    }

    public static <V> @NotNull Promise<List<V>> combine(@NotNull List<Promise<V>> promises, long timeout, boolean strict) {
        AtomicInteger index = new AtomicInteger();
        return combine(
            promises.stream()
                .collect(Collectors.toMap(s -> index.getAndIncrement(), v -> v)),
            timeout,
            strict
        ).thenApplySync(v ->
            v.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList())
        );
    }

    public static <V> @NotNull Promise<List<V>> combine(@NotNull List<Promise<V>> promises, long timeout) {
        return combine(promises, timeout, true);
    }

    public static <V> @NotNull Promise<List<V>> combine(@NotNull List<Promise<V>> promises) {
        return combine(promises, 1500L, true);
    }

    public static @NotNull Promise<Void> all(@NotNull List<Promise<?>> promises) {
        if (promises.isEmpty()) return Promise.start();

        Promise<Void> promise = new Promise<>();
        for (Promise<?> p : promises) {
            p.addListener((ctx) -> {
                if (ctx.isError()) {
                    //noinspection ConstantConditions
                    promise.completeExceptionally(ctx.getException());
                } else if (promises.stream().allMatch(Promise::isCompleted)) {
                    promise.complete(null);
                }
            });
        }
        return promise;
    }

    public static @NotNull Promise<Void> all(@NotNull Promise<?>... promises) {
        return all(Arrays.asList(promises));
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Collection<K> keys, @NotNull ExceptionalFunction<K, V> mapper, long timeout, boolean strict) {
        Map<K, Promise<V>> promises = new HashMap<>();
        for (K key : keys) {
            Promise<V> promise = Promise.resolve(key).thenApplyAsync(mapper);
            promises.put(key, promise);
        }

        return combine(promises, timeout, strict);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Collection<K> keys, @NotNull ExceptionalFunction<K, V> mapper, long timeout) {
        return combine(keys, mapper, timeout, true);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Collection<K> keys, @NotNull ExceptionalFunction<K, V> mapper) {
        return combine(keys, mapper, 1500L, true);
    }

    public static @NotNull Promise<Void> erase(@NotNull Promise<?> p) {
        Promise<Void> promise = new Promise<>();
        p.addListener(ctx -> {
            if (ctx.isError()) {
                //noinspection ConstantConditions
                promise.completeExceptionally(ctx.getException());
            } else {
                promise.complete(null);
            }
        });

        return promise;
    }

    public static <T> @NotNull Promise<T> wrap(@NotNull CompletableFuture<T> future) {
        Promise<T> promise = new Promise<>();
        future.whenComplete((result, e) -> {
            if (e != null) {
                promise.completeExceptionally(e);
            } else {
                promise.complete(result);
            }
        });

        return promise;
    }

}