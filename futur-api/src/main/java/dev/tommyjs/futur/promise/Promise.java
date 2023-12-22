package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.function.ExceptionalConsumer;
import dev.tommyjs.futur.function.ExceptionalFunction;
import dev.tommyjs.futur.function.ExceptionalRunnable;
import dev.tommyjs.futur.function.ExceptionalSupplier;
import dev.tommyjs.futur.trace.ExecutorTrace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public interface Promise<T> {

    static <T> @NotNull Promise<T> resolve(T value, PromiseFactory factory) {
        return factory.resolve(value);
    }

    static <T> @NotNull Promise<T> error(Throwable error, PromiseFactory factory) {
        return factory.error(error);
    }

    static @NotNull Promise<Void> start(PromiseFactory factory) {
        return factory.start();
    }

    static <T> @NotNull Promise<T> resolve(T value) {
        return resolve(value, UnpooledPromiseFactory.INSTANCE);
    }

    static <T> @NotNull Promise<T> error(Throwable error) {
        return error(error, UnpooledPromiseFactory.INSTANCE);
    }

    static @NotNull Promise<Void> start() {
        return start(UnpooledPromiseFactory.INSTANCE);
    }

    @Deprecated
    static <T> @NotNull Promise<T> start(T start) {
        return resolve(start);
    }

    PromiseFactory getFactory();

    T join(long interval, long timeout) throws TimeoutException;

    @NotNull Promise<Void> thenRunSync(@NotNull ExceptionalRunnable task);

    @NotNull Promise<Void> thenRunDelayedSync(@NotNull ExceptionalRunnable task, long delay, @NotNull TimeUnit unit);

    @NotNull Promise<Void> thenConsumeSync(@NotNull ExceptionalConsumer<T> task);

    @NotNull Promise<Void> thenConsumeDelayedSync(@NotNull ExceptionalConsumer<T> task, long delay, @NotNull TimeUnit unit);

    <V> @NotNull Promise<V> thenSupplySync(@NotNull ExceptionalSupplier<V> task);

    <V> @NotNull Promise<V> thenSupplyDelayedSync(@NotNull ExceptionalSupplier<V> task, long delay, @NotNull TimeUnit unit);

    <V> @NotNull Promise<V> thenApplySync(@NotNull ExceptionalFunction<T, V> task);

    <V> @NotNull Promise<V> thenApplyDelayedSync(@NotNull ExceptionalFunction<T, V> task, long delay, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace);

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

    <V> @NotNull Promise<V> thenApplyDelayedAsync(@NotNull ExceptionalFunction<T, V> task, long delay, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace);

    <V> @NotNull Promise<V> thenApplyDelayedAsync(@NotNull ExceptionalFunction<T, V> task, long delay, @NotNull TimeUnit unit);

    <V> @NotNull Promise<V> thenCompose(@NotNull ExceptionalFunction<T, Promise<V>> task);

    <V> @NotNull Promise<V> thenComposeAsync(@NotNull ExceptionalFunction<T, Promise<V>> task);

    @NotNull Promise<T> logExceptions();

    @NotNull Promise<T> addListener(@NotNull PromiseListener<T> listener);

    @NotNull Promise<T> timeout(long time, @NotNull TimeUnit unit);

    @NotNull Promise<T> timeout(long ms);

    void complete(@Nullable T result);

    void completeExceptionally(@NotNull Throwable result, boolean appendStacktrace);

    void completeExceptionally(@NotNull Throwable result);

    boolean isCompleted();

    @Nullable PromiseCompletion<T> getCompletion();

}
