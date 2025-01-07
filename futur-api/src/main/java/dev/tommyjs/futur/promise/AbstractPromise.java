package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.function.ExceptionalConsumer;
import dev.tommyjs.futur.function.ExceptionalFunction;
import dev.tommyjs.futur.function.ExceptionalRunnable;
import dev.tommyjs.futur.function.ExceptionalSupplier;
import dev.tommyjs.futur.util.PromiseUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.function.Consumer;

@SuppressWarnings({"FieldMayBeFinal", "unchecked"})
public abstract class AbstractPromise<T, FS, FA> implements CompletablePromise<T> {

    private static final VarHandle COMPLETION_HANDLE;
    private static final VarHandle LISTENERS_HANDLE;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            COMPLETION_HANDLE = lookup.findVarHandle(AbstractPromise.class, "completion", PromiseCompletion.class);
            LISTENERS_HANDLE = lookup.findVarHandle(AbstractPromise.class, "listeners", Collection.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final Sync sync;

    private volatile Collection<PromiseListener<T>> listeners;
    private volatile PromiseCompletion<T> completion;

    public AbstractPromise() {
        this.sync = new Sync();
        this.listeners = Collections.EMPTY_LIST;
        this.completion = null;
    }

    public abstract @NotNull AbstractPromiseFactory<FS, FA> getFactory();

    private void runCompleter(@NotNull CompletablePromise<?> promise, @NotNull ExceptionalRunnable completer) {
        try {
            completer.run();
        } catch (Error e) {
            promise.completeExceptionally(e);
            throw e;
        } catch (Throwable e) {
            promise.completeExceptionally(e);
        }
    }

    private <V> @NotNull Runnable createCompleter(
        T result,
        @NotNull CompletablePromise<V> promise,
        @NotNull ExceptionalFunction<T, V> completer
    ) {
        return () -> {
            if (!promise.isCompleted()) {
                runCompleter(promise, () -> promise.complete(completer.apply(result)));
            }
        };
    }

    protected @NotNull Logger getLogger() {
        return getFactory().getLogger();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        sync.acquireSharedInterruptibly(1);
        return joinCompletion();
    }

    @Override
    public T get(long time, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireSharedNanos(1, unit.toNanos(time));
        if (!success) {
            throw new TimeoutException("Promise stopped waiting after " + time + " " + unit);
        }

        return joinCompletion();
    }

    @Override
    public T await() {
        try {
            sync.acquireSharedInterruptibly(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        PromiseCompletion<T> completion = Objects.requireNonNull(getCompletion());
        if (completion.isSuccess()) return completion.getResult();
        throw new CompletionException(completion.getException());
    }

    private T joinCompletion() throws ExecutionException {
        PromiseCompletion<T> completion = Objects.requireNonNull(getCompletion());
        if (completion.isSuccess()) return completion.getResult();
        throw new ExecutionException(completion.getException());
    }

    @Override
    public @NotNull Promise<T> fork() {
        CompletablePromise<T> fork = getFactory().unresolved();
        PromiseUtil.propagateCompletion(this, fork);
        return fork;
    }

    @Override
    public @NotNull Promise<Void> thenRun(@NotNull ExceptionalRunnable task) {
        return thenApply(_ -> {
            task.run();
            return null;
        });
    }

    @Override
    public @NotNull Promise<Void> thenConsume(@NotNull ExceptionalConsumer<T> task) {
        return thenApply(result -> {
            task.accept(result);
            return null;
        });
    }

    @Override
    public <V> @NotNull Promise<V> thenSupply(@NotNull ExceptionalSupplier<V> task) {
        return thenApply(_ -> task.get());
    }

    @Override
    public <V> @NotNull Promise<V> thenApply(@NotNull ExceptionalFunction<T, V> task) {
        CompletablePromise<V> promise = getFactory().unresolved();
        addDirectListener(
            res -> createCompleter(res, promise, task).run(),
            promise::completeExceptionally
        );

        PromiseUtil.propagateCancel(promise, this);
        return promise;
    }

    @Override
    public <V> @NotNull Promise<V> thenCompose(@NotNull ExceptionalFunction<T, Promise<V>> task) {
        CompletablePromise<V> promise = getFactory().unresolved();
        thenApply(task).addDirectListener(
            nestedPromise -> {
                if (nestedPromise == null) {
                    promise.complete(null);
                } else {
                    PromiseUtil.propagateCompletion(nestedPromise, promise);
                    PromiseUtil.propagateCancel(promise, nestedPromise);
                }
            },
            promise::completeExceptionally
        );

        PromiseUtil.propagateCancel(promise, this);
        return promise;
    }

    @Override
    public @NotNull Promise<Void> thenRunSync(@NotNull ExceptionalRunnable task) {
        return thenApplySync(_ -> {
            task.run();
            return null;
        });
    }

    @Override
    public @NotNull Promise<Void> thenRunDelayedSync(@NotNull ExceptionalRunnable task, long delay, @NotNull TimeUnit unit) {
        return thenApplyDelayedSync(_ -> {
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
        return thenApplySync(_ -> task.get());
    }

    @Override
    public <V> @NotNull Promise<V> thenSupplyDelayedSync(@NotNull ExceptionalSupplier<V> task, long delay, @NotNull TimeUnit unit) {
        return thenApplyDelayedSync(_ -> task.get(), delay, unit);
    }

    @Override
    public <V> @NotNull Promise<V> thenApplySync(@NotNull ExceptionalFunction<T, V> task) {
        CompletablePromise<V> promise = getFactory().unresolved();
        addDirectListener(
            res -> runCompleter(promise, () -> {
                Runnable runnable = createCompleter(res, promise, task);
                FS future = getFactory().getSyncExecutor().run(runnable);
                promise.addDirectListener(_ -> getFactory().getSyncExecutor().cancel(future));
            }),
            promise::completeExceptionally
        );

        PromiseUtil.propagateCancel(promise, this);
        return promise;
    }

    @Override
    public <V> @NotNull Promise<V> thenApplyDelayedSync(@NotNull ExceptionalFunction<T, V> task, long delay, @NotNull TimeUnit unit) {
        CompletablePromise<V> promise = getFactory().unresolved();
        addDirectListener(
            res -> runCompleter(promise, () -> {
                Runnable runnable = createCompleter(res, promise, task);
                FS future = getFactory().getSyncExecutor().run(runnable, delay, unit);
                promise.addDirectListener(_ -> getFactory().getSyncExecutor().cancel(future));
            }),
            promise::completeExceptionally
        );

        PromiseUtil.propagateCancel(promise, this);
        return promise;
    }

    @Override
    public <V> @NotNull Promise<V> thenComposeSync(@NotNull ExceptionalFunction<T, Promise<V>> task) {
        CompletablePromise<V> promise = getFactory().unresolved();
        thenApplySync(task).addDirectListener(
            nestedPromise -> {
                if (nestedPromise == null) {
                    promise.complete(null);
                } else {
                    PromiseUtil.propagateCompletion(nestedPromise, promise);
                    PromiseUtil.propagateCancel(promise, nestedPromise);
                }
            },
            promise::completeExceptionally
        );

        PromiseUtil.propagateCancel(promise, this);
        return promise;
    }

    @Override
    public @NotNull Promise<Void> thenRunAsync(@NotNull ExceptionalRunnable task) {
        return thenApplyAsync(_ -> {
            task.run();
            return null;
        });
    }

    @Override
    public @NotNull Promise<Void> thenRunDelayedAsync(@NotNull ExceptionalRunnable task, long delay, @NotNull TimeUnit unit) {
        return thenApplyDelayedAsync(_ -> {
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
        return thenApplyAsync(_ -> task.get());
    }

    @Override
    public <V> @NotNull Promise<V> thenSupplyDelayedAsync(@NotNull ExceptionalSupplier<V> task, long delay, @NotNull TimeUnit unit) {
        return thenApplyDelayedAsync(_ -> task.get(), delay, unit);
    }

    @Override
    public <V> @NotNull Promise<V> thenApplyAsync(@NotNull ExceptionalFunction<T, V> task) {
        CompletablePromise<V> promise = getFactory().unresolved();
        addDirectListener(
            (res) -> runCompleter(promise, () -> {
                Runnable runnable = createCompleter(res, promise, task);
                FA future = getFactory().getAsyncExecutor().run(runnable);
                promise.addDirectListener(_ -> getFactory().getAsyncExecutor().cancel(future));
            }),
            promise::completeExceptionally
        );

        PromiseUtil.propagateCancel(promise, this);
        return promise;
    }

    @Override
    public <V> @NotNull Promise<V> thenApplyDelayedAsync(@NotNull ExceptionalFunction<T, V> task, long delay, @NotNull TimeUnit unit) {
        CompletablePromise<V> promise = getFactory().unresolved();
        addDirectListener(
            res -> runCompleter(promise, () -> {
                Runnable runnable = createCompleter(res, promise, task);
                FA future = getFactory().getAsyncExecutor().run(runnable, delay, unit);
                promise.addDirectListener(_ -> getFactory().getAsyncExecutor().cancel(future));
            }),
            promise::completeExceptionally
        );

        PromiseUtil.propagateCancel(promise, this);
        return promise;
    }

    @Override
    public <V> @NotNull Promise<V> thenComposeAsync(@NotNull ExceptionalFunction<T, Promise<V>> task) {
        CompletablePromise<V> promise = getFactory().unresolved();
        thenApplyAsync(task).addDirectListener(
            nestedPromise -> {
                if (nestedPromise == null) {
                    promise.complete(null);
                } else {
                    PromiseUtil.propagateCompletion(nestedPromise, promise);
                    PromiseUtil.propagateCancel(promise, nestedPromise);
                }
            },
            promise::completeExceptionally
        );

        PromiseUtil.propagateCancel(promise, this);
        return promise;
    }

    @Override
    public @NotNull Promise<T> thenPopulateReference(@NotNull AtomicReference<T> reference) {
        return thenApplyAsync(result -> {
            reference.set(result);
            return result;
        });
    }

    @Override
    public @NotNull Promise<Void> erase() {
        return thenSupply(() -> null);
    }

    @Override
    public @NotNull Promise<T> addAsyncListener(@NotNull AsyncPromiseListener<T> listener) {
        return addAnyListener(listener);
    }

    @Override
    public @NotNull Promise<T> addAsyncListener(@Nullable Consumer<T> successListener, @Nullable Consumer<Throwable> errorListener) {
        return addAsyncListener(res -> {
            if (res.isSuccess()) {
                if (successListener != null) successListener.accept(res.getResult());
            } else {
                if (errorListener != null) errorListener.accept(res.getException());
            }
        });
    }

    @Override
    public @NotNull Promise<T> addDirectListener(@NotNull PromiseListener<T> listener) {
        return addAnyListener(listener);
    }

    @Override
    public @NotNull Promise<T> addDirectListener(@Nullable Consumer<T> successListener, @Nullable Consumer<Throwable> errorListener) {
        return addDirectListener(res -> {
            if (res.isSuccess()) {
                if (successListener != null) successListener.accept(res.getResult());
            } else {
                if (errorListener != null) errorListener.accept(res.getException());
            }
        });
    }

    private @NotNull Promise<T> addAnyListener(PromiseListener<T> listener) {
        Collection<PromiseListener<T>> prev = listeners, next = null;
        for (boolean haveNext = false;;) {
            if (!haveNext) {
                next = prev == Collections.EMPTY_LIST ? new ConcurrentLinkedQueue<>() : prev;
                if (next != null) next.add(listener);
            }

            if (LISTENERS_HANDLE.weakCompareAndSet(this, prev, next))
                break;

            haveNext = (prev == (prev = listeners));
        }

        if (next == null) {
            if (listener instanceof AsyncPromiseListener) {
                callListenerAsync(listener, Objects.requireNonNull(getCompletion()));
            } else {
                callListenerNow(listener, Objects.requireNonNull(getCompletion()));
            }
        }

        return this;
    }

    private void callListenerAsync(PromiseListener<T> listener, PromiseCompletion<T> res) {
        try {
            getFactory().getAsyncExecutor().run(() -> callListenerNow(listener, res));
        } catch (RejectedExecutionException ignored) {
        } catch (Exception e) {
            getLogger().warn("Exception caught while running promise listener", e);
        }
    }

    private void callListenerNow(PromiseListener<T> listener, PromiseCompletion<T> res) {
        try {
            listener.handle(res);
        } catch (Error e) {
            getLogger().error("Error caught in promise listener", e);
            throw e;
        } catch (Throwable e) {
            getLogger().error("Exception caught in promise listener", e);
        }
    }

    @Override
    public @NotNull Promise<T> onSuccess(@NotNull Consumer<T> listener) {
        return addAsyncListener(listener, null);
    }

    @Override
    public @NotNull Promise<T> onError(@NotNull Consumer<Throwable> listener) {
        return addAsyncListener(null, listener);
    }

    @Override
    public @NotNull Promise<T> logExceptions(@NotNull String message) {
        Exception wrapper = new DeferredExecutionException();
        return onError(e -> getLogger().error(message, wrapper.initCause(e)));
    }

    @Override
    public <E extends Throwable> @NotNull Promise<T> onError(@NotNull Class<E> type, @NotNull Consumer<E> listener) {
        return onError(e -> {
            if (type.isAssignableFrom(e.getClass())) {
                //noinspection unchecked
                listener.accept((E) e);
            }
        });
    }

    @Override
    public @NotNull Promise<T> onCancel(@NotNull Consumer<CancellationException> listener) {
        return onError(CancellationException.class, listener);
    }

    @Override
    public @NotNull Promise<T> timeout(long time, @NotNull TimeUnit unit) {
        Exception e = new CancellationException("Promise timed out after " + time + " " + unit.toString().toLowerCase());
        return completeExceptionallyDelayed(e, time, unit);
    }

    @Override
    public @NotNull Promise<T> maxWaitTime(long time, @NotNull TimeUnit unit) {
        Exception e = new TimeoutException("Promise stopped waiting after " + time + " " + unit.toString().toLowerCase());
        return completeExceptionallyDelayed(e, time, unit);
    }

    private Promise<T> completeExceptionallyDelayed(Throwable e, long delay, TimeUnit unit) {
        runCompleter(this, () -> {
            FA future = getFactory().getAsyncExecutor().run(() -> completeExceptionally(e), delay, unit);
            addDirectListener(_ -> getFactory().getAsyncExecutor().cancel(future));
        });
        return this;
    }

    private void handleCompletion(@NotNull PromiseCompletion<T> cmp) {
        if (!COMPLETION_HANDLE.compareAndSet(this, null, cmp)) return;
        sync.releaseShared(1);


        Iterator<PromiseListener<T>> iter = ((Iterable<PromiseListener<T>>) LISTENERS_HANDLE.getAndSet(this, null)).iterator();
        try {
            while (iter.hasNext()) {
                PromiseListener<T> listener = iter.next();
                if (listener instanceof AsyncPromiseListener) {
                    callListenerAsync(listener, cmp);
                } else {
                    callListenerNow(listener, cmp);
                }
            }
        } finally {
            iter.forEachRemaining(v -> callListenerAsyncLastResort(v, cmp));
        }
    }

    private void callListenerAsyncLastResort(PromiseListener<T> listener, PromiseCompletion<T> completion) {
        try {
            getFactory().getAsyncExecutor().run(() -> callListenerNow(listener, completion));
        } catch (Throwable ignored) {}
    }

    @Override
    public void cancel(@NotNull CancellationException e) {
        completeExceptionally(e);
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
        return completion != null;
    }

    @Override
    public @Nullable PromiseCompletion<T> getCompletion() {
        return completion;
    }

    @Override
    public @NotNull CompletableFuture<T> toFuture() {
        CompletableFuture<T> future = new CompletableFuture<>();
        addDirectListener(future::complete, future::completeExceptionally);
        future.whenComplete((_, e) -> {
            if (e instanceof CancellationException) {
                this.cancel();
            }
        });

        return future;
    }

    private static class DeferredExecutionException extends ExecutionException {
    }

    private static final class Sync extends AbstractQueuedSynchronizer {

        private Sync() {
            setState(1);
        }

        @Override
        protected int tryAcquireShared(int acquires) {
            return getState() == 0 ? 1 : -1;
        }

        @Override
        protected boolean tryReleaseShared(int releases) {
            int c1, c2;
            do {
                c1 = getState();
                if (c1 == 0) {
                    return false;
                }

                c2 = c1 - 1;
            } while(!compareAndSetState(c1, c2));

            return c2 == 0;
        }
    }

}
