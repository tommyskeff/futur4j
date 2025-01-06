package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.function.ExceptionalConsumer;
import dev.tommyjs.futur.function.ExceptionalFunction;
import dev.tommyjs.futur.function.ExceptionalRunnable;
import dev.tommyjs.futur.function.ExceptionalSupplier;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public interface Promise<T> {

    @NotNull PromiseFactory getFactory();

    @NotNull Promise<Void> thenRun(@NotNull ExceptionalRunnable task);

    @NotNull Promise<Void> thenConsume(@NotNull ExceptionalConsumer<T> task);

    <V> @NotNull Promise<V> thenSupply(@NotNull ExceptionalSupplier<V> task);

    <V> @NotNull Promise<V> thenApply(@NotNull ExceptionalFunction<T, V> task);

    <V> @NotNull Promise<V> thenCompose(@NotNull ExceptionalFunction<T, Promise<V>> task);

    @NotNull Promise<Void> thenRunSync(@NotNull ExceptionalRunnable task);

    @NotNull Promise<Void> thenRunDelayedSync(@NotNull ExceptionalRunnable task, long delay, @NotNull TimeUnit unit);

    @NotNull Promise<Void> thenConsumeSync(@NotNull ExceptionalConsumer<T> task);

    @NotNull Promise<Void> thenConsumeDelayedSync(@NotNull ExceptionalConsumer<T> task, long delay, @NotNull TimeUnit unit);

    <V> @NotNull Promise<V> thenSupplySync(@NotNull ExceptionalSupplier<V> task);

    <V> @NotNull Promise<V> thenSupplyDelayedSync(@NotNull ExceptionalSupplier<V> task, long delay, @NotNull TimeUnit unit);

    <V> @NotNull Promise<V> thenApplySync(@NotNull ExceptionalFunction<T, V> task);

    <V> @NotNull Promise<V> thenApplyDelayedSync(@NotNull ExceptionalFunction<T, V> task, long delay, @NotNull TimeUnit unit);

    <V> @NotNull Promise<V> thenComposeSync(@NotNull ExceptionalFunction<T, Promise<V>> task);

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

    @NotNull Promise<Void> erase();

    default @NotNull Promise<T> logExceptions() {
        return logExceptions("Exception caught in promise chain");
    }

    @NotNull Promise<T> logExceptions(@NotNull String message);

    /**
     * @apiNote Direct listeners run on the same thread as the completion.
     */
    @NotNull Promise<T> addDirectListener(@NotNull PromiseListener<T> listener);

    /**
     * @apiNote Direct listeners run on the same thread as the completion.
     */
    @NotNull Promise<T> addDirectListener(@Nullable Consumer<T> successHandler, @Nullable Consumer<Throwable> errorHandler);

    /**
     * @apiNote Async listeners are run in parallel.
     */
    @NotNull Promise<T> addAsyncListener(@NotNull AsyncPromiseListener<T> listener);

    /**
     * @apiNote Same as addAsyncListener.
     */
    default @NotNull Promise<T> addListener(@NotNull AsyncPromiseListener<T> listener) {
        return addAsyncListener(listener);
    }

    /**
     * @apiNote Async listeners are run in parallel.
     */
    @NotNull Promise<T> addAsyncListener(@Nullable Consumer<T> successHandler, @Nullable Consumer<Throwable> errorHandler);

    @NotNull Promise<T> onSuccess(@NotNull Consumer<T> listener);

    @NotNull Promise<T> onError(@NotNull Consumer<Throwable> listener);

    <E extends Throwable> @NotNull Promise<T> onError(@NotNull Class<E> clazz, @NotNull Consumer<E> listener);

    @NotNull Promise<T> onCancel(@NotNull Consumer<CancellationException> listener);

    /**
     * Cancels the promise with a TimeoutException after the specified time.
     */
    @NotNull Promise<T> timeout(long time, @NotNull TimeUnit unit);

    /**
     * Cancels the promise with a TimeoutException after the specified time.
     */
    default @NotNull Promise<T> timeout(long ms) {
        return timeout(ms, TimeUnit.MILLISECONDS);
    }

    /**
     * Completes the promise exceptionally with a TimeoutException after the specified time.
     */
    @NotNull Promise<T> maxWaitTime(long time, @NotNull TimeUnit unit);

    /**
     * Completes the promise exceptionally with a TimeoutException after the specified time.
     */
    default @NotNull Promise<T> maxWaitTime(long ms) {
        return maxWaitTime(ms, TimeUnit.MILLISECONDS);
    }

    void cancel(@NotNull CancellationException exception);

    default void cancel(@NotNull String reason) {
        cancel(new CancellationException(reason));
    };

    default void cancel() {
        cancel(new CancellationException());
    }

    /**
     * Waits if necessary for this promise to complete, and then returns its result.
     * @throws CancellationException if the computation was cancelled
     * @throws CompletionException if this promise completed exceptionally
     */
    @Blocking
    T await();

    /**
     * Waits if necessary for this promise to complete, and then returns its result.
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if this promise completed exceptionally
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    @Blocking
    T get() throws InterruptedException, ExecutionException;

    /**
     * Waits if necessary for at most the given time for this future to complete, and then returns its result, if available.
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if this promise completed exceptionally
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws TimeoutException if the wait timed out
     */
    @Blocking
    T get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Stops this promise from propagating up cancellations.
     */
    @NotNull Promise<T> fork();

    @Nullable PromiseCompletion<T> getCompletion();

    boolean isCompleted();

    @NotNull CompletableFuture<T> toFuture();

}
