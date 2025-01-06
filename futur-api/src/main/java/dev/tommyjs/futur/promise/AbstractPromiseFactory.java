package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.executor.PromiseExecutor;
import dev.tommyjs.futur.joiner.CompletionJoiner;
import dev.tommyjs.futur.joiner.MappedResultJoiner;
import dev.tommyjs.futur.joiner.ResultJoiner;
import dev.tommyjs.futur.joiner.VoidJoiner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public abstract class AbstractPromiseFactory<FS, FA> implements PromiseFactory {

    public abstract @NotNull PromiseExecutor<FS> getSyncExecutor();

    public abstract @NotNull PromiseExecutor<FA> getAsyncExecutor();

    @Override
    public <K, V> @NotNull Promise<Map.Entry<K, V>> combine(
        @NotNull Promise<K> p1,
        @NotNull Promise<V> p2,
        boolean dontFork
    ) {
        return all(dontFork, p1, p2).thenApply((_) -> new AbstractMap.SimpleImmutableEntry<>(
            Objects.requireNonNull(p1.getCompletion()).getResult(),
            Objects.requireNonNull(p2.getCompletion()).getResult()
        ));
    }

    @Override
    public <K, V> @NotNull Promise<Map<K, V>> combine(
        @NotNull Map<K, Promise<V>> promises,
        @Nullable BiConsumer<K, Throwable> exceptionHandler,
        boolean link
    ) {
        if (promises.isEmpty()) return resolve(Collections.emptyMap());
        return new MappedResultJoiner<>(this,
            promises.entrySet().iterator(), exceptionHandler, promises.size(), link).joined();
    }

    @Override
    public <V> @NotNull Promise<List<V>> combine(
        @NotNull Iterator<Promise<V>> promises, int expectedSize,
        @Nullable BiConsumer<Integer, Throwable> exceptionHandler, boolean link
    ) {
        if (!promises.hasNext()) return resolve(Collections.emptyList());
        return new ResultJoiner<>(
            this, promises, exceptionHandler, expectedSize, link).joined();
    }

    @Override
    public @NotNull Promise<List<PromiseCompletion<?>>> allSettled(
        @NotNull Iterator<Promise<?>> promises,
        int expectedSize,
        boolean link
    ) {
        if (!promises.hasNext()) return resolve(Collections.emptyList());
        return new CompletionJoiner(this, promises, expectedSize, link).joined();
    }

    @Override
    public @NotNull Promise<Void> all(@NotNull Iterator<Promise<?>> promises, boolean link) {
        if (!promises.hasNext()) return resolve(null);
        return new VoidJoiner(this, promises, link).joined();
    }

    @Override
    public <V> @NotNull Promise<V> race(@NotNull Iterator<Promise<V>> promises, boolean link) {
        CompletablePromise<V> promise = unresolved();
        promises.forEachRemaining(p -> {
            if (link) AbstractPromise.cancelOnFinish(p, promise);
            if (!promise.isCompleted())
                AbstractPromise.propagateResult(p, promise);
        });

        return promise;
    }

    @Override
    public <V> @NotNull Promise<V> race(@NotNull Iterable<Promise<V>> promises, boolean link) {
        return race(promises.iterator(), link);
    }

    @Override
    public <V> @NotNull Promise<V> race(@NotNull Stream<Promise<V>> promises, boolean link) {
        return race(promises.iterator(), link);
    }

    @Override
    public <T> @NotNull Promise<T> wrap(@NotNull CompletableFuture<T> future) {
        return wrap(future, future);
    }

    private <T> @NotNull Promise<T> wrap(@NotNull CompletionStage<T> completion, Future<T> future) {
        CompletablePromise<T> promise = unresolved();

        completion.whenComplete((v, e) -> {
            if (e != null) {
                promise.completeExceptionally(e);
            } else {
                promise.complete(v);
            }
        });

        promise.onCancel(_ -> future.cancel(true));
        return promise;
    }

    @Override
    public <T> @NotNull Promise<T> resolve(T value) {
        CompletablePromise<T> promise = unresolved();
        promise.complete(value);
        return promise;
    }

    @Override
    public <T> @NotNull Promise<T> error(@NotNull Throwable error) {
        CompletablePromise<T> promise = unresolved();
        promise.completeExceptionally(error);
        return promise;
    }

}
