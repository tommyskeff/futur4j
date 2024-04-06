package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.executor.PromiseExecutor;
import dev.tommyjs.futur.function.ExceptionalConsumer;
import dev.tommyjs.futur.function.ExceptionalFunction;
import dev.tommyjs.futur.function.ExceptionalRunnable;
import dev.tommyjs.futur.function.ExceptionalSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public abstract class AbstractPromise<T, F> implements Promise<T> {

    private final Collection<PromiseListener<T>> listeners;
    private final AtomicReference<PromiseCompletion<T>> completion;

    public AbstractPromise() {
        this.listeners = new ConcurrentLinkedQueue<>();
        this.completion = new AtomicReference<>();
    }

    protected static <V> void propagateResult(Promise<V> from, Promise<V> to) {
        from.addListener(to::complete, to::completeExceptionally);
    }

    protected static void propagateCancel(Promise<?> from, Promise<?> to) {
        from.onCancel(to::completeExceptionally);
    }

    private <V> @NotNull Runnable createRunnable(T result, @NotNull Promise<V> promise, @NotNull ExceptionalFunction<T, V> task) {
        return () -> {
            if (promise.isCompleted()) return;

            try {
                V nextResult = task.apply(result);
                promise.complete(nextResult);
            } catch (Throwable e) {
                promise.completeExceptionally(e);
            }
        };
    }

    public abstract @NotNull AbstractPromiseFactory<F> getFactory();

    protected @NotNull PromiseExecutor<F> getExecutor() {
        return getFactory().getExecutor();
    }

    protected @NotNull Logger getLogger() {
        return getFactory().getLogger();
    }

    @Override
    public T join(long timeoutMillis) throws TimeoutException {
        PromiseCompletion<T> completion;
        long start = System.currentTimeMillis();
        long remainingTimeout = timeoutMillis;

        synchronized (this.completion) {
            completion = this.completion.get();
            while (completion == null && remainingTimeout > 0) {
                try {
                    this.completion.wait(remainingTimeout);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                completion = this.completion.get();
                remainingTimeout = timeoutMillis - (System.currentTimeMillis() - start);
            }
        }

        if (completion == null)
            throw new TimeoutException("Promise stopped waiting after " + timeoutMillis + "ms");

        return joinCompletion(completion);
    }

    private T joinCompletion(PromiseCompletion<T> completion) {
        if (completion.isError())
            throw new RuntimeException(completion.getException());

        return completion.getResult();
    }

    @Override
    public @NotNull Promise<Void> thenRunSync(@NotNull ExceptionalRunnable task) {
        return thenApplySync(result -> {
            task.run();
            return null;
        });
    }

    @Override
    public @NotNull Promise<Void> thenRunDelayedSync(@NotNull ExceptionalRunnable task, long delay, @NotNull TimeUnit unit) {
        return thenApplyDelayedSync(result -> {
            task.run();
            return null;
        }, delay, unit);
    }

    @Override
    public @NotNull Promise<Void> thenConsumeSync(@NotNull ExceptionalConsumer<T> task) {
        return thenApplySync(result -> {
            task.accept(result);
            return null;
        });
    }

    @Override
    public @NotNull Promise<Void> thenConsumeDelayedSync(@NotNull ExceptionalConsumer<T> task, long delay, @NotNull TimeUnit unit) {
        return thenApplyDelayedSync(result -> {
            task.accept(result);
            return null;
        }, delay, unit);
    }

    @Override
    public <V> @NotNull Promise<V> thenSupplySync(@NotNull ExceptionalSupplier<V> task) {
        return thenApplySync(result -> task.get());
    }

    @Override
    public <V> @NotNull Promise<V> thenSupplyDelayedSync(@NotNull ExceptionalSupplier<V> task, long delay, @NotNull TimeUnit unit) {
        return thenApplyDelayedSync(result -> task.get(), delay, unit);
    }

    @Override
    public <V> @NotNull Promise<V> thenApplySync(@NotNull ExceptionalFunction<T, V> task) {
        Promise<V> promise = getFactory().unresolved();
        addListener(
            res -> {
                Runnable runnable = createRunnable(res, promise, task);
                F future = getExecutor().runSync(runnable);
                promise.onCancel((e) -> getExecutor().cancel(future));
            },
            promise::completeExceptionally
        );

        propagateCancel(promise, this);
        return promise;
    }

    @Override
    public <V> @NotNull Promise<V> thenApplyDelayedSync(@NotNull ExceptionalFunction<T, V> task, long delay, @NotNull TimeUnit unit) {
        Promise<V> promise = getFactory().unresolved();
        addListener(
            res -> {
                Runnable runnable = createRunnable(res, promise, task);
                F future = getExecutor().runSync(runnable, delay, unit);
                promise.onCancel((e) -> getExecutor().cancel(future));
            },
            promise::completeExceptionally
        );

        propagateCancel(promise, this);
        return promise;
    }

    @Override
    public <V> @NotNull Promise<V> thenComposeSync(@NotNull ExceptionalFunction<T, @NotNull Promise<V>> task) {
        Promise<V> promise = getFactory().unresolved();
        thenApplySync(task).addListener(
            nestedPromise -> {
                propagateResult(nestedPromise, promise);
                propagateCancel(promise, nestedPromise);
            },
            promise::completeExceptionally
        );

        propagateCancel(promise, this);
        return promise;
    }

    @Override
    public @NotNull Promise<Void> thenRunAsync(@NotNull ExceptionalRunnable task) {
        return thenApplyAsync(result -> {
            task.run();
            return null;
        });
    }

    @Override
    public @NotNull Promise<Void> thenRunDelayedAsync(@NotNull ExceptionalRunnable task, long delay, @NotNull TimeUnit unit) {
        return thenApplyDelayedAsync(result -> {
            task.run();
            return null;
        }, delay, unit);
    }

    @Override
    public @NotNull Promise<Void> thenConsumeAsync(@NotNull ExceptionalConsumer<T> task) {
        return thenApplyAsync(result -> {
            task.accept(result);
            return null;
        });
    }

    @Override
    public @NotNull Promise<Void> thenConsumeDelayedAsync(@NotNull ExceptionalConsumer<T> task, long delay, @NotNull TimeUnit unit) {
        return thenApplyDelayedAsync(result -> {
            task.accept(result);
            return null;
        }, delay, unit);
    }

    @Override
    public <V> @NotNull Promise<V> thenSupplyAsync(@NotNull ExceptionalSupplier<V> task) {
        return thenApplyAsync(result -> task.get());
    }

    @Override
    public <V> @NotNull Promise<V> thenSupplyDelayedAsync(@NotNull ExceptionalSupplier<V> task, long delay, @NotNull TimeUnit unit) {
        return thenApplyDelayedAsync(result -> task.get(), delay, unit);
    }

    @Override
    public @NotNull Promise<T> thenPopulateReference(@NotNull AtomicReference<T> reference) {
        return thenApplyAsync((result) -> {
            reference.set(result);
            return result;
        });
    }

    @Override
    public <V> @NotNull Promise<V> thenApplyAsync(@NotNull ExceptionalFunction<T, V> task) {
        Promise<V> promise = getFactory().unresolved();
        addListener(
            (res) -> {
                Runnable runnable = createRunnable(res, promise, task);
                F future = getExecutor().runAsync(runnable);
                promise.onCancel((e) -> getExecutor().cancel(future));
            },
            promise::completeExceptionally
        );

        propagateCancel(promise, this);
        return promise;
    }

    @Override
    public <V> @NotNull Promise<V> thenApplyDelayedAsync(@NotNull ExceptionalFunction<T, V> task, long delay, @NotNull TimeUnit unit) {
        Promise<V> promise = getFactory().unresolved();
        addListener(
            res -> {
                Runnable runnable = createRunnable(res, promise, task);
                F future = getExecutor().runAsync(runnable, delay, unit);
                promise.onCancel((e) -> getExecutor().cancel(future));
            },
            promise::completeExceptionally
        );

        propagateCancel(promise, this);
        return promise;
    }

    @Override
    public <V> @NotNull Promise<V> thenComposeAsync(@NotNull ExceptionalFunction<T, Promise<V>> task) {
        Promise<V> promise = getFactory().unresolved();
        thenApplyAsync(task).addListener(
            nestedPromise -> {
                propagateResult(nestedPromise, promise);
                propagateCancel(promise, nestedPromise);
            },
            promise::completeExceptionally
        );

        propagateCancel(promise, this);
        return promise;
    }

    @Override
    public @NotNull Promise<Void> erase() {
        return thenSupplyAsync(() -> null);
    }


    @Override
    public @NotNull Promise<T> logExceptions(@NotNull String message) {
        return onError(e -> getLogger().error(message, e));
    }

    @Override
    public @NotNull Promise<T> addListener(@NotNull PromiseListener<T> listener) {
        synchronized (completion) {
            if (isCompleted()) {
                getExecutor().runAsync(() -> {
                    try {
                        //noinspection ConstantConditions
                        listener.handle(getCompletion());
                    } catch (Exception e) {
                        getLogger().error("Exception caught in promise listener", e);
                    }
                });
            } else {
                getListeners().add(listener);
            }
        }

        return this;
    }

    @Override
    public @NotNull Promise<T> addListener(@Nullable Consumer<T> successListener, @Nullable Consumer<Throwable> errorListener) {
        return addListener((res) -> {
            if (res.isError()) {
                if (errorListener != null) errorListener.accept(res.getException());
            } else {
                if (successListener != null) successListener.accept(res.getResult());
            }
        });
    }

    @Override
    public @NotNull Promise<T> onSuccess(@NotNull Consumer<T> listener) {
        return addListener(listener, null);
    }

    @Override
    public @NotNull Promise<T> onError(@NotNull Consumer<Throwable> listener) {
        return addListener(null, listener);
    }

    @Override
    public <E extends Throwable> @NotNull Promise<T> onError(@NotNull Class<E> clazz, @NotNull Consumer<E> listener) {
        return onError((e) -> {
            if (clazz.isAssignableFrom(e.getClass())) {
                //noinspection unchecked
                listener.accept((E) e);
            }
        });
    }

    @Override
    public @NotNull Promise<T> onCancel(@NotNull Consumer<CancellationException> listener) {
        return onError(CancellationException.class, listener);
    }

    @Deprecated
    @Override
    public @NotNull Promise<T> timeout(long time, @NotNull TimeUnit unit) {
        return maxWaitTime(time, unit);
    }

    @Override
    public @NotNull Promise<T> maxWaitTime(long time, @NotNull TimeUnit unit) {
        F future = getExecutor().runAsync(() -> completeExceptionally(new TimeoutException("Promise stopped waiting after " + time + " " + unit)), time, unit);
        return onError(e -> getExecutor().cancel(future));
    }

    private void handleCompletion(@NotNull PromiseCompletion<T> ctx) {
        synchronized (completion) {
            if (!setCompletion(ctx)) return;

            completion.notifyAll();
            getExecutor().runAsync(() -> {
                for (PromiseListener<T> listener : getListeners()) {
                    if (!ctx.isActive()) return;

                    try {
                        listener.handle(ctx);
                    } catch (Exception e) {
                        getLogger().error("Exception caught in promise listener", e);
                    }
                }
            });
        }
    }

    private boolean setCompletion(PromiseCompletion<T> completion) {
        return this.completion.compareAndSet(null, completion);
    }

    @Override
    public void cancel() {
        completeExceptionally(new CancellationException());
    }

    @Override
    public void cancel(@NotNull String message) {
        completeExceptionally(new CancellationException(message));
    }

    @Override
    public void complete(@Nullable T result) {
        handleCompletion(new PromiseCompletion<>(result));
    }

    @Override
    public void completeExceptionally(@NotNull Throwable result) {
        handleCompletion(new PromiseCompletion<>(result));
    }

    @Override
    public boolean isCompleted() {
        return completion.get() != null;
    }

    @Override
    public @Nullable PromiseCompletion<T> getCompletion() {
        return completion.get();
    }

    private Collection<PromiseListener<T>> getListeners() {
        return listeners;
    }

}
