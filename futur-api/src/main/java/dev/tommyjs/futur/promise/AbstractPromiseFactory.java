package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.executor.PromiseExecutor;
import dev.tommyjs.futur.joiner.CompletionJoiner;
import dev.tommyjs.futur.joiner.MappedResultJoiner;
import dev.tommyjs.futur.joiner.ResultJoiner;
import dev.tommyjs.futur.joiner.VoidJoiner;
import dev.tommyjs.futur.util.PromiseUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

public abstract class AbstractPromiseFactory<FS, FA> implements PromiseFactory {

    public abstract @NotNull Logger getLogger();

    public abstract @NotNull PromiseExecutor<FS> getSyncExecutor();

    public abstract @NotNull PromiseExecutor<FA> getAsyncExecutor();

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
    public <K, V> @NotNull Promise<Map.Entry<K, V>> combine(
        @NotNull Promise<K> p1,
        @NotNull Promise<V> p2,
        boolean link
    ) {
        return all(link, p1, p2).thenApply(_ -> new AbstractMap.SimpleImmutableEntry<>(
            Objects.requireNonNull(p1.getCompletion()).getResult(),
            Objects.requireNonNull(p2.getCompletion()).getResult()
        ));
    }

    @Override
    public @NotNull <K, V> Promise<Map<K, V>> combineMapped(@NotNull Iterator<Map.Entry<K, Promise<V>>> promises, int expectedSize, boolean link) {
        if (!promises.hasNext()) return resolve(Collections.emptyMap());
        return new MappedResultJoiner<>(this, promises, expectedSize, link).joined();
    }

    @Override
    public <V> @NotNull Promise<List<V>> combine(
        @NotNull Iterator<Promise<V>> promises,
        int expectedSize, boolean link
    ) {
        if (!promises.hasNext()) return resolve(Collections.emptyList());
        return new ResultJoiner<>(this, promises, expectedSize, link).joined();
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
    public <V> @NotNull Promise<V> race(@NotNull Iterator<Promise<V>> promises, boolean cancelLosers) {
        CompletablePromise<V> promise = unresolved();
        promises.forEachRemaining(p -> {
            if (cancelLosers) PromiseUtil.cancelOnComplete(promise, p);
            if (!promise.isCompleted()) {
                PromiseUtil.propagateCompletion(p, promise);
            }
        });

        return promise;
    }

}
