package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.function.ExceptionalConsumer;
import dev.tommyjs.futur.function.ExceptionalFunction;
import dev.tommyjs.futur.function.ExceptionalRunnable;
import dev.tommyjs.futur.function.ExceptionalSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public interface Promise<T> {

    PromiseFactory getFactory();

    @NotNull Promise<Void> thenRunSync(@NotNull ExceptionalRunnable task);

    @NotNull Promise<Void> thenRunDelayedSync(@NotNull ExceptionalRunnable task, long delay, @NotNull TimeUnit unit);

    @NotNull Promise<Void> thenConsumeSync(@NotNull ExceptionalConsumer<T> task);

    @NotNull Promise<Void> thenConsumeDelayedSync(@NotNull ExceptionalConsumer<T> task, long delay, @NotNull TimeUnit unit);

    <V> @NotNull Promise<V> thenSupplySync(@NotNull ExceptionalSupplier<V> task);

    <V> @NotNull Promise<V> thenSupplyDelayedSync(@NotNull ExceptionalSupplier<V> task, long delay, @NotNull TimeUnit unit);

    <V> @NotNull Promise<V> thenApplySync(@NotNull ExceptionalFunction<T, V> task);

    <V> @NotNull Promise<V> thenApplyDelayedSync(@NotNull ExceptionalFunction<T, V> task, long delay, @NotNull TimeUnit unit);

    <V> @NotNull Promise<V> thenComposeSync(@NotNull ExceptionalFunction<T, @NotNull Promise<V>> task);

    @NotNull Promise<Void> thenRunAsync(@NotNull ExceptionalRunnable task);

    @NotNull Promise<Void> thenRunDelayedAsync(@NotNull ExceptionalRunnable task, long delay, @NotNull TimeUnit unit);

    @NotNull Promise<Void> thenConsumeAsync(@NotNull ExceptionalConsumer<T> task);

    @NotNull Promise<Void> thenConsumeDelayedAsync(@NotNull ExceptionalConsumer<T> task, long delay, @NotNull TimeUnit unit);

    <V> @NotNull Promise<V> thenSupplyAsync(@NotNull ExceptionalSupplier<V> task);

    <V> @NotNull Promise<V> thenSupplyDelayedAsync(@NotNull ExceptionalSupplier<V> task, long delay, @NotNull TimeUnit unit);

    @NotNull Promise<T> thenPopulateReference(@NotNull AtomicReference<T> reference);

    <V> @NotNull Promise<V> thenApplyAsync(@NotNull ExceptionalFunction<T, V> task);

    <V> @NotNull Promise<V> thenApplyDelayedAsync(@NotNull ExceptionalFunction<T, V> task, long delay, @NotNull TimeUnit unit);

    <V> @NotNull Promise<V> thenComposeAsync(@NotNull ExceptionalFunction<T, Promise<V>> task);

    @NotNull Promise<T> logExceptions(@NotNull String message);

    default @NotNull Promise<T> logExceptions() {
        return logExceptions("Exception caught in promise chain");
    }

    @NotNull Promise<T> addListener(@NotNull PromiseListener<T> listener);

    @NotNull Promise<T> addListener(@Nullable Consumer<T> successHandler, @Nullable Consumer<Throwable> errorHandler);

    @NotNull Promise<T> onSuccess(@NotNull Consumer<T> listener);

    @NotNull Promise<T> onError(@NotNull Consumer<Throwable> listener);

    <E extends Throwable> @NotNull Promise<T> onError(@NotNull Class<E> clazz, @NotNull Consumer<E> listener);

    @NotNull Promise<T> onCancel(@NotNull Consumer<CancellationException> listener);

    @Deprecated
    @NotNull Promise<T> timeout(long time, @NotNull TimeUnit unit);

    @Deprecated
    default @NotNull Promise<T> timeout(long ms) {
        return timeout(ms, TimeUnit.MILLISECONDS);
    }

    @NotNull Promise<T> maxWaitTime(long time, @NotNull TimeUnit unit);

    default @NotNull Promise<T> maxWaitTime(long ms) {
        return maxWaitTime(ms, TimeUnit.MILLISECONDS);
    }

    void addChild(@NotNull Promise<?> child);

    void propagateResult(@NotNull Promise<T> target);

    void cancel(@Nullable String reason);

    default void cancel() {
        cancel(null);
    }

    void complete(@Nullable T result);

    void completeExceptionally(@NotNull Throwable result);

    T join(long timeout) throws TimeoutException;

    @Nullable PromiseCompletion<T> getCompletion();

    boolean isCompleted();

}
