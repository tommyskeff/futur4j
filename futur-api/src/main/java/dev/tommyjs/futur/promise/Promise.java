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

/**
 * <p>
 * A promise represents the result of an asynchronous computation. A promise will transition from a
 * pending state to a completed state at most once, but may remain in a pending state indefinitely.
 * </p>
 *
 * <p>
 * Promises are created by a {@link PromiseFactory} and support chaining operations to be executed
 * upon completion. These operations can be synchronous or asynchronous, and can be composed in a
 * variety of ways. Promises can be listened to for completions, either with a result or with an
 * exception. Promises can be cancelled, which will propagate a cancellation signal through the
 * chain, but a promise can also be forked, which will prevent propagation of cancellations.
 * </p>
 *
 * @see #cancel()
 * @see #fork()
 */
public interface Promise<T> {

    /**
     * Returns the factory that created this promise. This factory can be used to create new promises.
     */
    @NotNull PromiseFactory getFactory();

    /**
     * Chains a task to be executed after this promise completes. The task will be executed immediately
     * when this promise completes.
     *
     * @param task the task to execute
     * @return a new promise that completes after the task is executed
     */
    @NotNull Promise<Void> thenRun(@NotNull ExceptionalRunnable task);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed immediately
     * when this promise completes and will be passed the result of this promise.
     *
     * @param task the task to execute
     * @return a new promise that completes after the task is executed
     */
    @NotNull Promise<Void> thenConsume(@NotNull ExceptionalConsumer<T> task);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed immediately
     * when this promise completes, and will supply a value to the next promise in the chain.
     *
     * @param task the task to execute
     * @return a new promise that completes, after the task is executed, with the task result
     */
    <V> @NotNull Promise<V> thenSupply(@NotNull ExceptionalSupplier<V> task);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed immediately
     * when this promise completes, and will apply the specified function to the result of this promise
     * in order to supply a value to the next promise in the chain.
     *
     * @param task the task to execute
     * @return a new promise that completes, after the task is executed, with the task result
     */
    <V> @NotNull Promise<V> thenApply(@NotNull ExceptionalFunction<T, V> task);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed immediately
     * when this promise completes, and will compose the next promise in the chainfrom the result of
     * this promise.
     *
     * @param task the task to execute
     * @return a new promise that completes, once this promise and the promise returned by
     * the task are complete, with the result of the task promise
     */
    <V> @NotNull Promise<V> thenCompose(@NotNull ExceptionalFunction<T, Promise<V>> task);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed by the
     * sync executor of the factory that created this promise, immediately after this promise completes.
     *
     * @param task the task to execute
     * @return a new promise that completes after the task is executed
     */
    @NotNull Promise<Void> thenRunSync(@NotNull ExceptionalRunnable task);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed by the
     * sync executor of the factory that created this promise, after the specified delay after this
     * promise completes.
     *
     * @param task  the task to execute
     * @param delay the amount of time to wait before executing the task
     * @param unit  the time unit of the delay
     * @return a new promise that completes after the task is executed
     */
    @NotNull Promise<Void> thenRunDelayedSync(@NotNull ExceptionalRunnable task, long delay, @NotNull TimeUnit unit);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed by the
     * sync executor of the factory that created this promise immediately after this promise completes,
     * and will be passed the result of this promise.
     *
     * @param task the task to execute
     * @return a new promise that completes after the task is executed
     */
    @NotNull Promise<Void> thenConsumeSync(@NotNull ExceptionalConsumer<T> task);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed by the
     * sync executor of the factory that created this promise after the specified delay after this
     * promise completes, and will be passed the result of this promise.
     *
     * @param task  the task to execute
     * @param delay the amount of time to wait before executing the task
     * @param unit  the time unit of the delay
     * @return a new promise that completes after the task is executed
     */
    @NotNull Promise<Void> thenConsumeDelayedSync(@NotNull ExceptionalConsumer<T> task, long delay, @NotNull TimeUnit unit);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed immediately
     * by the sync executor of the factory that created this promise when this promise completes, and
     * will supply a value to the next promise in the chain.
     *
     * @param task the task to execute
     * @return a new promise that completes, after the task is executed, with the task result
     */
    <V> @NotNull Promise<V> thenSupplySync(@NotNull ExceptionalSupplier<V> task);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed by the sync
     * executor of the factory that created this promise after the specified delay after this promise
     * completes, and will supply a value to the next promise in the chain.
     *
     * @param task  the task to execute
     * @param delay the amount of time to wait before executing the task
     * @param unit  the time unit of the delay
     * @return a new promise that completes, after the task is executed, with the task result
     */
    <V> @NotNull Promise<V> thenSupplyDelayedSync(@NotNull ExceptionalSupplier<V> task, long delay, @NotNull TimeUnit unit);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed by the sync
     * executor of the factory that created this promise immediately after this promise completes, and
     * will apply the specified function to the result of this promise in order to supply a value to the
     * next promise in the chain.
     *
     * @param task the task to execute
     * @return a new promise that completes, after the task is executed, with the task result
     */
    <V> @NotNull Promise<V> thenApplySync(@NotNull ExceptionalFunction<T, V> task);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed by the sync
     * executor of the factory that created this promise after the specified delay after this promise
     * completes, and will apply the specified function to the result of this promise in order to supply
     * a value to the next promise in the chain.
     *
     * @param task  the task to execute
     * @param delay the amount of time to wait before executing the task
     * @param unit  the time unit of the delay
     * @return a new promise that completes, after the task is executed, with the task result
     */
    <V> @NotNull Promise<V> thenApplyDelayedSync(@NotNull ExceptionalFunction<T, V> task, long delay, @NotNull TimeUnit unit);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed by the sync
     * executor of the factory that created this promise immediately after this promise completes, and
     * will compose the next promise in the chain from the result of this promise.
     *
     * @param task the task to execute
     * @return a new promise that completes, once this promise and the promise returned by the task are
     * complete, with the result of the task promise
     */
    <V> @NotNull Promise<V> thenComposeSync(@NotNull ExceptionalFunction<T, Promise<V>> task);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed by the
     * async executor of the factory that created this promise, immediately after this promise completes.
     *
     * @param task the task to execute
     * @return a new promise that completes after the task is executed
     */
    @NotNull Promise<Void> thenRunAsync(@NotNull ExceptionalRunnable task);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed by the
     * async executor of the factory that created this promise after the specified delay after this
     * promise completes.
     *
     * @param task  the task to execute
     * @param delay the amount of time to wait before executing the task
     * @param unit  the time unit of the delay
     * @return a new promise that completes after the task is executed
     */
    @NotNull Promise<Void> thenRunDelayedAsync(@NotNull ExceptionalRunnable task, long delay, @NotNull TimeUnit unit);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed by the
     * async executor of the factory that created this promise immediately after this promise completes,
     * and will be passed the result of this promise.
     *
     * @param task the task to execute
     * @return a new promise that completes after the task is executed
     */
    @NotNull Promise<Void> thenConsumeAsync(@NotNull ExceptionalConsumer<T> task);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed by the
     * async executor of the factory that created this promise after the specified delay after this
     * promise completes, and will be passed the result of this promise.
     *
     * @param task  the task to execute
     * @param delay the amount of time to wait before executing the task
     * @param unit  the time unit of the delay
     * @return a new promise that completes after the task is executed
     */
    @NotNull Promise<Void> thenConsumeDelayedAsync(@NotNull ExceptionalConsumer<T> task, long delay, @NotNull TimeUnit unit);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed by the
     * async executor of the factory that created this promise immediately after this promise completes,
     * and will supply a value to the next promise in the chain.
     *
     * @param task the task to execute
     * @return a new promise that completes, after the task is executed, with the task result
     */
    <V> @NotNull Promise<V> thenSupplyAsync(@NotNull ExceptionalSupplier<V> task);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed by the async
     * executor of the factory that created this promise after the specified delay after this promise
     * completes, and will supply a value to the next promise in the chain.
     *
     * @param task  the task to execute
     * @param delay the amount of time to wait before executing the task
     * @param unit  the time unit of the delay
     * @return a new promise that completes, after the task is executed, with the task result
     */
    <V> @NotNull Promise<V> thenSupplyDelayedAsync(@NotNull ExceptionalSupplier<V> task, long delay, @NotNull TimeUnit unit);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed by the async
     * executor of the factory that created this promise immediately after this promise completes, and
     * will apply the specified function to the result of this promise in order to supply a value to the
     * next promise in the chain.
     *
     * @param task the task to execute
     * @return a new promise that completes, after the task is executed, with the task result
     */
    <V> @NotNull Promise<V> thenApplyAsync(@NotNull ExceptionalFunction<T, V> task);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed by the async
     * executor of the factory that created this promise after the specified delay after this promise
     * completes, and will apply the specified function to the result of this promise in order to supply
     * a value to the next promise in the chain.
     *
     * @param task  the task to execute
     * @param delay the amount of time to wait before executing the task
     * @param unit  the time unit of the delay
     * @return a new promise that completes, after the task is executed, with the task result
     */
    <V> @NotNull Promise<V> thenApplyDelayedAsync(@NotNull ExceptionalFunction<T, V> task, long delay, @NotNull TimeUnit unit);

    /**
     * Chains a task to be executed after this promise completes. The task will be executed by the async
     * executor of the factory that created this promise immediately after this promise completes, and
     * will compose the next promise in the chain from the result of this promise.
     *
     * @param task the task to execute
     * @return a new promise that completes, once this promise and the promise returned by the task are
     * complete, with the result of the task promise
     */
    <V> @NotNull Promise<V> thenComposeAsync(@NotNull ExceptionalFunction<T, Promise<V>> task);

    /**
     * Adds a listener to this promise that will populate the specified reference with the result of this
     * promise upon successful completion.
     *
     * @param reference the reference to populate
     * @return continuation of the promise chain
     */
    @NotNull Promise<T> thenPopulateReference(@NotNull AtomicReference<T> reference);

    /**
     * Returns a promise backed by this promise that will complete with {@code null} if this promise
     * completes successfully, or with the exception if this promise completes exceptionally.
     */
    @NotNull Promise<Void> erase();

    /**
     * Logs any exceptions that occur in the promise chain.
     *
     * @return continuation of the promise chain
     */
    default @NotNull Promise<T> logExceptions() {
        return logExceptions("Exception caught in promise chain");
    }

    /**
     * Logs any exceptions that occur in the promise chain with the specified message.
     *
     * @param message the message to log
     * @return continuation of the promise chain
     */
    @NotNull Promise<T> logExceptions(@NotNull String message);

    /**
     * Adds a listener to this promise that will be executed immediately when this promise completes,
     * on the same thread as the completion call.
     *
     * @param listener the listener to add
     * @return continuation of the promise chain
     */
    @NotNull Promise<T> addDirectListener(@NotNull PromiseListener<T> listener);

    /**
     * Adds a listener to this promise that will be executed immediately when this promise completes,
     * on the same thread as the completion call. One of {@code successHandler} and {@code errorHandler} will be
     * called when the promise completes successfully or exceptionally, respectively.
     *
     * @param successHandler the function to call on success
     * @param errorHandler   the function to call on error
     * @return continuation of the promise chain
     */
    @NotNull Promise<T> addDirectListener(@Nullable Consumer<T> successHandler, @Nullable Consumer<Throwable> errorHandler);

    /**
     * Adds a listener to this promise that will be executed immediately when this promise completes,
     * by the async executor of the factory that created this promise.
     *
     * @param listener the listener to add
     * @return continuation of the promise chain
     */
    @NotNull Promise<T> addAsyncListener(@NotNull AsyncPromiseListener<T> listener);

    /**
     * Adds a listener to this promise that will be executed immediately when this promise completes.
     *
     * @param listener the listener to add
     * @return continuation of the promise chain
     */
    default @NotNull Promise<T> addListener(@NotNull AsyncPromiseListener<T> listener) {
        return addAsyncListener(listener);
    }

    /**
     * Adds a listener to this promise that will be executed immediately when this promise completes,
     * by the async executor of the factory that created this promise. One of {@code successHandler} and
     * {@code errorHandler} will be called when the promise completes successfully or exceptionally, respectively.
     *
     * @param successHandler the function to call on success
     * @param errorHandler   the function to call on error
     */
    @NotNull Promise<T> addAsyncListener(@Nullable Consumer<T> successHandler, @Nullable Consumer<Throwable> errorHandler);

    /**
     * Adds a listener to this promise that will be called if the promise is completed successfully.
     *
     * @param listener the listener to add
     * @return continuation of the promise chain
     */
    @NotNull Promise<T> onSuccess(@NotNull Consumer<T> listener);

    /**
     * Adds a listener to this promise that will be called if the promise is completed exceptionally.
     *
     * @param listener the listener to add
     * @return continuation of the promise chain
     */
    @NotNull Promise<T> onError(@NotNull Consumer<Throwable> listener);

    /**
     * Adds a listener to this promise that will be called if the promise is completed exceptionally
     * with an exception of the specified type.
     *
     * @param listener the listener to add
     * @param type     the class of the exception to listen for
     * @return continuation of the promise chain
     */
    <E extends Throwable> @NotNull Promise<T> onError(@NotNull Class<E> type, @NotNull Consumer<E> listener);

    /**
     * Adds a listener to this promise that will be called if the promise is cancelled.
     *
     * @param listener the listener to add
     * @return continuation of the promise chain
     */
    @NotNull Promise<T> onCancel(@NotNull Consumer<CancellationException> listener);

    /**
     * Cancels the promise if not already completed after the specified timeout. This will result in
     * an exceptional completion with a {@link CancellationException}.
     *
     * @param time the amount of time to wait before cancelling the promise
     * @param unit the time unit of the delay
     * @return continuation of the promise chain
     */
    @NotNull Promise<T> timeout(long time, @NotNull TimeUnit unit);

    /**
     * Cancels the promise if not already completed after the specified timeout. This will result in
     * an exceptional completion with a {@link CancellationException}.
     * @param ms the amount of time to wait before cancelling the promise (in milliseconds)
     * @return continuation of the promise chain
     */
    default @NotNull Promise<T> timeout(long ms) {
        return timeout(ms, TimeUnit.MILLISECONDS);
    }

    /**
     * Times out the promise if not already completed after the specified timeout. This will result
     * in an exceptional completion with a {@link TimeoutException}. This will not result in the
     * promise being cancelled.
     *
     * @param time the amount of time to wait before timing out the promise
     * @param unit the time unit of the delay
     * @return continuation of the promise chain
     */
    @NotNull Promise<T> maxWaitTime(long time, @NotNull TimeUnit unit);

    /**
     * Times out the promise if not already completed after the specified timeout. This will result
     * in an exceptional completion with a {@link TimeoutException}. This will not result in the
     * promise being cancelled.
     * @param ms the amount of time to wait before timing out the promise (in milliseconds)
     * @return continuation of the promise chain
     */
    default @NotNull Promise<T> maxWaitTime(long ms) {
        return maxWaitTime(ms, TimeUnit.MILLISECONDS);
    }

    /**
     * Cancels the promise if not already completed after the specified timeout. This will result in
     * an exceptional completion with the specified cancellation.
     * @param exception the cancellation exception to complete the promise with
     */
    void cancel(@NotNull CancellationException exception);

    /**
     * Cancels the promise if not already completed after the specified timeout. This will result in
     * an exceptional completion with a {@link CancellationException}.
     * @param reason the reason for the cancellation
     */
    default void cancel(@NotNull String reason) {
        cancel(new CancellationException(reason));
    }

    /**
     * Cancels the promise if not already completed after the specified timeout. This will result in
     * an exceptional completion with a {@link CancellationException}.
     */
    default void cancel() {
        cancel(new CancellationException());
    }

    /**
     * Blocks until this promise has completed, and then returns its result.
     * @throws CancellationException if the promise was cancelled
     * @throws CompletionException   if the promise completed exceptionally
     * @return the result of the promise
     */
    @Blocking
    T await();

    /**
     * Blocks until this promise has completed, and then returns its result.
     * @throws CancellationException if the promise was cancelled
     * @throws ExecutionException    if the promise completed exceptionally
     * @throws InterruptedException  if the current thread was interrupted while waiting
     * @return the result of the promise
     */
    @Blocking
    T get() throws InterruptedException, ExecutionException;

    /**
     *  Blocks until either this promise has completed or the timeout has been exceeded, and then
     *  returns its result, if available.
     * @throws CancellationException if the promise was cancelled
     * @throws ExecutionException    if the promise completed exceptionally
     * @throws InterruptedException  if the current thread was interrupted while waiting
     * @throws TimeoutException      if the timeout was exceeded
     * @return the result of the promise
     */
    @Blocking
    T get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Returns a new promise, backed by this promise, that will not propagate cancellations. This means
     * that if the returned promise is cancelled, the cancellation will not be propagated to this promise
     * or any other promises that share this promise as a parent.
     * @return continuation the promise chain that will not propagate cancellations
     */
    @NotNull Promise<T> fork();

    /**
     * Returns the current completion state of this promise. If the promise has not completed, this method
     * will return {@code null}.
     * @return the completion state of this promise, or {@code null} if the promise has not completed
     */
    @Nullable PromiseCompletion<T> getCompletion();

    /**
     * Returns whether this promise has completed.
     * @return {@code true} if the promise has completed, {@code false} otherwise
     */
    boolean isCompleted();

    /**
     * Converts this promise to a {@link CompletableFuture}. The returned future will complete with the
     * result of this promise when it completes.
     * @return a future that will complete with the result of this promise
     */
    @NotNull CompletableFuture<T> toFuture();

}
