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

    public static <K, V> @NotNull Promise<Map.Entry<K, V>> combine(@NotNull Promise<K> p1, @NotNull Promise<V> p2, PromiseFactory factory) {
        Promise<Map.Entry<K, V>> promise = factory.unresolved();
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

    public static <K, V> @NotNull Promise<Map.Entry<K, V>> combine(@NotNull Promise<K> p1, @NotNull Promise<V> p2) {
        return combine(p1, p2, p1.getFactory());
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, long timeout, @Nullable BiConsumer<K, Throwable> exceptionHandler, PromiseFactory factory) {
        Map<K, V> map = new HashMap<>();
        ReentrantLock lock = new ReentrantLock();

        Promise<Map<K, V>> promise = factory.unresolved();
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

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, long timeout, @Nullable BiConsumer<K, Throwable> exceptionHandler) {
        return combine(promises, timeout, exceptionHandler, obtainFactory(promises.values()));
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, long timeout, boolean strict, PromiseFactory factory) {
        return combine(promises, timeout, strict ? null : (_k, _v) -> {}, factory);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, long timeout, boolean strict) {
        return combine(promises, timeout, strict, obtainFactory(promises.values()));
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, long timeout, PromiseFactory factory) {
        return combine(promises, timeout, true, factory);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, long timeout) {
        return combine(promises, timeout, true, obtainFactory(promises.values()));
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, PromiseFactory factory) {
        return combine(promises, 1500L, true, factory);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises) {
        return combine(promises, obtainFactory(promises.values()));
    }

    public static <V> @NotNull Promise<List<V>> combine(@NotNull List<Promise<V>> promises, long timeout, boolean strict, PromiseFactory factory) {
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

    public static <V> @NotNull Promise<List<V>> combine(@NotNull List<Promise<V>> promises, long timeout, boolean strict) {
        return combine(promises, timeout, strict, obtainFactory(promises));
    }

    public static <V> @NotNull Promise<List<V>> combine(@NotNull List<Promise<V>> promises, long timeout, PromiseFactory factory) {
        return combine(promises, timeout, true, factory);
    }

    public static <V> @NotNull Promise<List<V>> combine(@NotNull List<Promise<V>> promises, long timeout) {
        return combine(promises, timeout, obtainFactory(promises));
    }

    public static <V> @NotNull Promise<List<V>> combine(@NotNull List<Promise<V>> promises, PromiseFactory factory) {
        return combine(promises, 1500L, true);
    }

    public static <V> @NotNull Promise<List<V>> combine(@NotNull List<Promise<V>> promises) {
        return combine(promises, obtainFactory(promises));
    }

    public static @NotNull Promise<Void> all(@NotNull List<Promise<?>> promises, PromiseFactory factory) {
        Promise<Void> promise = factory.unresolved();
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

    public static @NotNull Promise<Void> all(@NotNull List<Promise<?>> promises) {
        PromiseFactory factory;
        if (promises.isEmpty()) {
            factory = UnpooledPromiseFactory.INSTANCE;
        } else {
            factory = promises.get(0).getFactory();
        }

        return all(promises, factory);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Collection<K> keys, @NotNull ExceptionalFunction<K, V> mapper, long timeout, boolean strict, PromiseFactory factory) {
        Map<K, Promise<V>> promises = new HashMap<>();
        for (K key : keys) {
            Promise<V> promise = factory.resolve(key).thenApplyAsync(mapper);
            promises.put(key, promise);
        }

        return combine(promises, timeout, strict);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Collection<K> keys, @NotNull ExceptionalFunction<K, V> mapper, long timeout, boolean strict) {
        return combine(keys, mapper, timeout, strict, UnpooledPromiseFactory.INSTANCE);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Collection<K> keys, @NotNull ExceptionalFunction<K, V> mapper, long timeout, PromiseFactory factory) {
        return combine(keys, mapper, timeout, true, factory);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Collection<K> keys, @NotNull ExceptionalFunction<K, V> mapper, long timeout) {
        return combine(keys, mapper, timeout, UnpooledPromiseFactory.INSTANCE);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Collection<K> keys, @NotNull ExceptionalFunction<K, V> mapper, PromiseFactory factory) {
        return combine(keys, mapper, 1500L, true, factory);
    }

    public static <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Collection<K> keys, @NotNull ExceptionalFunction<K, V> mapper) {
        return combine(keys, mapper, UnpooledPromiseFactory.INSTANCE);
    }

    public static @NotNull Promise<Void> erase(@NotNull Promise<?> p, PromiseFactory factory) {
        Promise<Void> promise = factory.unresolved();
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

    public static @NotNull Promise<Void> erase(@NotNull Promise<?> p) {
        return erase(p, p.getFactory());
    }

    public static <T> @NotNull Promise<T> wrap(@NotNull CompletableFuture<T> future, PromiseFactory factory) {
        Promise<T> promise = factory.unresolved();
        future.whenComplete((result, e) -> {
            if (e != null) {
                promise.completeExceptionally(e);
            } else {
                promise.complete(result);
            }
        });

        return promise;
    }

    public static <T> @NotNull Promise<T> wrap(@NotNull CompletableFuture<T> future) {
        return wrap(future, UnpooledPromiseFactory.INSTANCE);
    }

    public static <T> PromiseFactory obtainFactory(Collection<Promise<T>> promises) {
        PromiseFactory factory;
        if (promises.isEmpty()) {
            factory = UnpooledPromiseFactory.INSTANCE;
        } else {
            factory = promises.stream().findFirst().get().getFactory();
        }

        return factory;
    }

}