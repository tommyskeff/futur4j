package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.executor.PromiseExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class AbstractPromiseFactory<F> implements PromiseFactory {

    public abstract @NotNull PromiseExecutor<F> getExecutor();

    @Override
    public <K, V> @NotNull Promise<Map.Entry<K, V>> combine(@NotNull Promise<K> p1, @NotNull Promise<V> p2) {
        List<Promise<?>> promises = List.of(p1, p2);
        return all(promises)
            .thenApplyAsync((res) -> new AbstractMap.SimpleImmutableEntry<>(
                Objects.requireNonNull(p1.getCompletion()).getResult(),
                Objects.requireNonNull(p2.getCompletion()).getResult()
            ));
    }

    @Override
    public <K, V> @NotNull Promise<Map<K, V>> combine(@NotNull Map<K, Promise<V>> promises, @Nullable BiConsumer<K, Throwable> exceptionHandler) {
        if (promises.isEmpty()) return resolve(Collections.emptyMap());

        Map<K, V> map = new HashMap<>();
        Promise<Map<K, V>> promise = unresolved();
        for (Map.Entry<K, Promise<V>> entry : promises.entrySet()) {
            entry.getValue().addListener((ctx) -> {
                synchronized (map) {
                    if (ctx.getException() != null) {
                        if (exceptionHandler == null) {
                            promise.completeExceptionally(ctx.getException());
                        } else {
                            exceptionHandler.accept(entry.getKey(), ctx.getException());
                            map.put(entry.getKey(), null);
                        }
                    } else {
                        map.put(entry.getKey(), ctx.getResult());
                    }

                    if (map.size() == promises.size()) {
                        promise.complete(map);
                    }
                }
            });
        }

        return promise;
    }

    @Override
    public <V> @NotNull Promise<List<V>> combine(@NotNull Iterable<Promise<V>> promises, @Nullable Consumer<Throwable> exceptionHandler) {
        AtomicInteger index = new AtomicInteger();
        return this.combine(
            StreamSupport.stream(promises.spliterator(), false)
                .collect(Collectors.toMap(k -> index.getAndIncrement(), v -> v)),
            exceptionHandler != null ? (i, e) -> exceptionHandler.accept(e) : null
        ).thenApplyAsync(v ->
            v.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList())
        );
    }

    @Override
    public <V> @NotNull Promise<List<V>> combine(@NotNull Iterable<Promise<V>> promises) {
        return combine(promises, null);
    }

    @Override
    public @NotNull Promise<List<PromiseCompletion<?>>> allSettled(@NotNull Iterable<Promise<?>> promiseIterable) {
        List<Promise<?>> promises = new ArrayList<>();
        promiseIterable.iterator().forEachRemaining(promises::add);

        if (promises.isEmpty()) return resolve(Collections.emptyList());
        PromiseCompletion<?>[] results = new PromiseCompletion<?>[promises.size()];

        Promise<List<PromiseCompletion<?>>> promise = unresolved();
        var iter = promises.listIterator();

        while (iter.hasNext()) {
            int index = iter.nextIndex();
            iter.next().addListener((res) -> {
                synchronized (results) {
                    results[index] = res;
                    if (Arrays.stream(results).allMatch(Objects::nonNull))
                        promise.complete(Arrays.asList(results));
                }
            });
        }

        return promise;
    }

    @Override
    public @NotNull Promise<Void> all(@NotNull Iterable<Promise<?>> promiseIterable) {
        List<Promise<?>> promises = new ArrayList<>();
        promiseIterable.iterator().forEachRemaining(promises::add);

        if (promises.isEmpty()) return resolve(null);
        AtomicInteger completed = new AtomicInteger();
        Promise<Void> promise = unresolved();

        for (Promise<?> p : promises) {
            p.addListener((res) -> {
                if (res.getException() != null) {
                    promise.completeExceptionally(res.getException());
                }

                if (completed.incrementAndGet() == promises.size()) {
                    promise.complete(null);
                }
            });
        }

        return promise;
    }

    @Override
    public <T> @NotNull Promise<T> wrap(@NotNull CompletableFuture<T> future) {
        Promise<T> promise = unresolved();

        future.whenComplete((v, e) -> {
            if (e != null) {
                promise.completeExceptionally(e);
            } else {
                promise.complete(v);
            }
        });

        promise.onCancel((e) -> future.cancel(true));
        return promise;
    }

    @Override
    public <T> @NotNull Promise<T> wrap(@NotNull Mono<T> mono) {
        Promise<T> promise = this.unresolved();
        Disposable disposable = mono.subscribe(promise::complete, promise::completeExceptionally);
        promise.onCancel((e) -> disposable.dispose());
        return promise;
    }

    @Override
    public <T> @NotNull Promise<T> resolve(T value) {
        Promise<T> promise = unresolved();
        promise.complete(value);
        return promise;
    }

    @Override
    public <T> @NotNull Promise<T> error(@NotNull Throwable error) {
        Promise<T> promise = unresolved();
        promise.completeExceptionally(error);
        return promise;
    }

    @Override
    public @NotNull Promise<Void> erase(@NotNull Promise<?> p) {
        Promise<Void> promise = unresolved();
        p.addListener(ctx -> {
            if (ctx.getException() != null) {
                promise.completeExceptionally(ctx.getException());
            } else {
                promise.complete(null);
            }
        });
        return promise;
    }

}
