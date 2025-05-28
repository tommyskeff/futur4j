package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.executor.PromiseExecutor;
import dev.tommyjs.futur.executor.PromiseScheduler;
import dev.tommyjs.futur.function.*;
import dev.tommyjs.futur.util.PromiseUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractPromise<T> implements Promise<T> {

    public abstract @NotNull AbstractPromiseFactory getFactory();

    protected abstract @NotNull Promise<T> addAnyListener(@NotNull PromiseListener<T> listener);

    protected @NotNull Logger getLogger() {
        return getFactory().getLogger();
    }

    protected void callListener(@NotNull PromiseListener<T> listener, @NotNull PromiseCompletion<T> cmp) {
        if (listener instanceof AsyncPromiseListener) {
            callListenerAsync(listener, cmp);
        } else {
            callListenerNow(listener, cmp);
        }
    }

    protected <V> V supplySafe(@NotNull ExceptionalSupplier<V> supplier, @NotNull Function<Throwable, V> handler) {
        try {
            return supplier.get();
        } catch (Error error) {
            // rethrow unrecoverable errors
            throw error;
        } catch (Throwable e) {
            return handler.apply(e);
        }
    }

    protected void runSafe(@NotNull ExceptionalRunnable runnable, @NotNull Consumer<Throwable> handler) {
        try {
            runnable.run();
        } catch (Error error) {
            handler.accept(error);
            // rethrow unrecoverable errors
            throw error;
        } catch (Throwable e) {
            handler.accept(e);
        }
    }

    protected void runCompleter(@NotNull CompletablePromise<?> promise, @NotNull ExceptionalRunnable completer) {
        runSafe(completer, promise::completeExceptionally);
    }

    protected <V> V useCompletion(Supplier<V> unresolved, Function<T, V> completed, Function<Throwable, V> failed) {
        PromiseCompletion<T> completion = getCompletion();
        if (completion == null) {
            return unresolved.get();
        } else if (completion.isSuccess()) {
            return completed.apply(completion.getResult());
        } else {
            return failed.apply(completion.getException());
        }
    }

    protected <V> @NotNull Runnable createCompleter(T result, @NotNull CompletablePromise<V> promise,
                                                    @NotNull ExceptionalFunction<T, V> completer) {
        return () -> {
            if (!promise.isCompleted()) {
                runCompleter(promise, () -> promise.complete(completer.apply(result)));
            }
        };
    }

    protected <V> @NotNull CompletablePromise<V> createLinked() {
        CompletablePromise<V> promise = getFactory().unresolved();
        PromiseUtil.propagateCancel(promise, this);
        return promise;
    }

    protected void callListenerAsync(PromiseListener<T> listener, PromiseCompletion<T> res) {
        try {
            getFactory().getAsyncExecutor().run(() -> callListenerNow(listener, res));
        } catch (RejectedExecutionException ignored) {
        } catch (Exception e) {
            getLogger().warn("Exception caught while running promise listener", e);
        }
    }

    protected void callListenerNow(PromiseListener<T> listener, PromiseCompletion<T> res) {
        runSafe(() -> listener.handle(res), e -> getLogger().error("Exception caught in promise listener", e));
    }

    protected void callListenerAsyncLastResort(PromiseListener<T> listener, PromiseCompletion<T> completion) {
        try {
            getFactory().getAsyncExecutor().run(() -> callListenerNow(listener, completion));
        } catch (Throwable ignored) {
        }
    }

    protected T joinCompletionChecked() throws ExecutionException {
        PromiseCompletion<T> completion = getCompletion();
        if (completion == null) {
            throw new IllegalStateException("Promise is not completed yet.");
        }

        return completion.getChecked();
    }

    protected T joinCompletionUnchecked() {
        PromiseCompletion<T> completion = getCompletion();
        if (completion == null) {
            throw new IllegalStateException("Promise is not completed yet.");
        }

        return completion.get();
    }

    @Override
    public @NotNull Promise<T> fork() {
        if (isCompleted()) {
            return this;
        }

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
        return useCompletion(() -> {
            CompletablePromise<V> promise = createLinked();
            addDirectListener(res -> createCompleter(res, promise, task).run(), promise::completeExceptionally);

            return promise;
        }, result -> supplySafe(() -> getFactory().resolve(task.apply(result)), getFactory()::error), getFactory()::error);
    }

    @Override
    public <V> @NotNull Promise<V> thenCompose(@NotNull ExceptionalFunction<T, Promise<V>> task) {
        return useCompletion(() -> {
            CompletablePromise<V> promise = createLinked();
            thenApply(task).addDirectListener(result -> {
                if (result == null) {
                    promise.complete(null);
                } else {
                    PromiseUtil.propagateCompletion(result, promise);
                    PromiseUtil.propagateCancel(promise, result);
                }
            }, promise::completeExceptionally);

            return promise;
        }, result -> supplySafe(() -> {
            Promise<V> nested = task.apply(result);
            if (nested == null) {
                return getFactory().resolve(null);
            } else if (nested.isCompleted()) {
                return nested;
            } else {
                CompletablePromise<V> promise = createLinked();
                PromiseUtil.propagateCompletion(nested, promise);
                PromiseUtil.propagateCancel(promise, nested);
                return promise;
            }
        }, getFactory()::error), getFactory()::error);
    }

    private <F, V> @NotNull Promise<V> thenApply(@NotNull ExceptionalFunction<T, V> task,
                                                 @NotNull PromiseExecutor<F> executor) {
        CompletablePromise<V> promise = createLinked();
        addDirectListener(res -> runCompleter(promise, () -> {
            Runnable completer = createCompleter(res, promise, task);
            execute(promise, completer, executor);
        }), promise::completeExceptionally);

        return promise;
    }

    private <F, V> @NotNull Promise<V> thenApplyDelayed(@NotNull ExceptionalFunction<T, V> task, long delay,
                                                        @NotNull TimeUnit unit, @NotNull PromiseExecutor<F> executor) {
        CompletablePromise<V> promise = createLinked();
        addDirectListener(res -> runCompleter(promise, () -> {
            Runnable completer = createCompleter(res, promise, task);
            PromiseScheduler<?> scheduler = executor.scheduler();
            if (scheduler == null) {
                schedule(promise, () -> runCompleter(promise, () -> execute(promise, completer, executor)), delay, unit,
                    PromiseScheduler.getDefault());
            } else {
                schedule(promise, completer, delay, unit, scheduler);
            }
        }), promise::completeExceptionally);

        return promise;
    }

    private <F> void execute(@NotNull Promise<?> promise, @NotNull Runnable task, @NotNull PromiseExecutor<F> executor)
        throws Exception {
        F future = executor.run(task);
        promise.addDirectListener(_ -> executor.cancel(future));
    }

    private <F> void schedule(@NotNull Promise<?> promise, @NotNull Runnable task, long delay, @NotNull TimeUnit unit,
                              @NotNull PromiseScheduler<F> scheduler) throws Exception {
        F future = scheduler.schedule(task, delay, unit);
        promise.addDirectListener(_ -> scheduler.cancel(future));
    }

    private <V> @NotNull Promise<V> thenCompose(@NotNull ExceptionalFunction<T, Promise<V>> task,
                                                @NotNull PromiseExecutor<?> executor) {
        CompletablePromise<V> promise = createLinked();
        thenApply(task, executor).addDirectListener(nestedPromise -> {
            if (nestedPromise == null) {
                promise.complete(null);
            } else {
                PromiseUtil.propagateCompletion(nestedPromise, promise);
                PromiseUtil.propagateCancel(promise, nestedPromise);
            }
        }, promise::completeExceptionally);

        return promise;
    }

    @Override
    public @NotNull Promise<Void> thenRunSync(@NotNull ExceptionalRunnable task) {
        return thenApply(FunctionAdapter.adapt(task), getFactory().getSyncExecutor());
    }

    @Override
    public @NotNull Promise<Void> thenRunDelayedSync(@NotNull ExceptionalRunnable task, long delay,
                                                     @NotNull TimeUnit unit) {
        return thenApplyDelayed(FunctionAdapter.adapt(task), delay, unit, getFactory().getSyncExecutor());
    }

    @Override
    public @NotNull Promise<Void> thenConsumeSync(@NotNull ExceptionalConsumer<T> task) {
        return thenApply(FunctionAdapter.adapt(task), getFactory().getSyncExecutor());
    }

    @Override
    public @NotNull Promise<Void> thenConsumeDelayedSync(@NotNull ExceptionalConsumer<T> task, long delay,
                                                         @NotNull TimeUnit unit) {
        return thenApplyDelayed(FunctionAdapter.adapt(task), delay, unit, getFactory().getSyncExecutor());
    }

    @Override
    public <V> @NotNull Promise<V> thenSupplySync(@NotNull ExceptionalSupplier<V> task) {
        return thenApply(FunctionAdapter.adapt(task), getFactory().getSyncExecutor());
    }

    @Override
    public <V> @NotNull Promise<V> thenSupplyDelayedSync(@NotNull ExceptionalSupplier<V> task, long delay,
                                                         @NotNull TimeUnit unit) {
        return thenApplyDelayed(FunctionAdapter.adapt(task), delay, unit, getFactory().getSyncExecutor());
    }

    @Override
    public <V> @NotNull Promise<V> thenApplySync(@NotNull ExceptionalFunction<T, V> task) {
        return thenApply(task, getFactory().getSyncExecutor());
    }

    @Override
    public <V> @NotNull Promise<V> thenApplyDelayedSync(@NotNull ExceptionalFunction<T, V> task, long delay,
                                                        @NotNull TimeUnit unit) {
        return thenApplyDelayed(task, delay, unit, getFactory().getSyncExecutor());
    }

    @Override
    public <V> @NotNull Promise<V> thenComposeSync(@NotNull ExceptionalFunction<T, Promise<V>> task) {
        return thenCompose(task, getFactory().getSyncExecutor());
    }

    @Override
    public @NotNull Promise<Void> thenRunAsync(@NotNull ExceptionalRunnable task) {
        return thenApply(FunctionAdapter.adapt(task), getFactory().getAsyncExecutor());
    }

    @Override
    public @NotNull Promise<Void> thenRunDelayedAsync(@NotNull ExceptionalRunnable task, long delay,
                                                      @NotNull TimeUnit unit) {
        return thenApplyDelayed(FunctionAdapter.adapt(task), delay, unit, getFactory().getAsyncExecutor());
    }

    @Override
    public @NotNull Promise<Void> thenConsumeAsync(@NotNull ExceptionalConsumer<T> task) {
        return thenApply(FunctionAdapter.adapt(task), getFactory().getAsyncExecutor());
    }

    @Override
    public @NotNull Promise<Void> thenConsumeDelayedAsync(@NotNull ExceptionalConsumer<T> task, long delay,
                                                          @NotNull TimeUnit unit) {
        return thenApplyDelayed(FunctionAdapter.adapt(task), delay, unit, getFactory().getAsyncExecutor());
    }

    @Override
    public <V> @NotNull Promise<V> thenSupplyAsync(@NotNull ExceptionalSupplier<V> task) {
        return thenApply(FunctionAdapter.adapt(task), getFactory().getAsyncExecutor());
    }

    @Override
    public <V> @NotNull Promise<V> thenSupplyDelayedAsync(@NotNull ExceptionalSupplier<V> task, long delay,
                                                          @NotNull TimeUnit unit) {
        return thenApplyDelayed(FunctionAdapter.adapt(task), delay, unit, getFactory().getAsyncExecutor());
    }

    @Override
    public <V> @NotNull Promise<V> thenApplyAsync(@NotNull ExceptionalFunction<T, V> task) {
        return thenApply(task, getFactory().getAsyncExecutor());
    }

    @Override
    public <V> @NotNull Promise<V> thenApplyDelayedAsync(@NotNull ExceptionalFunction<T, V> task, long delay,
                                                         @NotNull TimeUnit unit) {
        return thenApplyDelayed(task, delay, unit, getFactory().getAsyncExecutor());
    }

    @Override
    public <V> @NotNull Promise<V> thenComposeAsync(@NotNull ExceptionalFunction<T, Promise<V>> task) {
        return thenCompose(task, getFactory().getAsyncExecutor());
    }

    @Override
    public @NotNull Promise<Void> thenRunVirtual(@NotNull ExceptionalRunnable task) {
        return thenApply(FunctionAdapter.adapt(task), getFactory().getVirtualExecutor());
    }

    @Override
    public @NotNull Promise<Void> thenRunDelayedVirtual(@NotNull ExceptionalRunnable task, long delay,
                                                        @NotNull TimeUnit unit) {
        return thenApplyDelayed(FunctionAdapter.adapt(task), delay, unit, getFactory().getVirtualExecutor());
    }

    @Override
    public @NotNull Promise<Void> thenConsumeVirtual(@NotNull ExceptionalConsumer<T> task) {
        return thenApply(FunctionAdapter.adapt(task), getFactory().getVirtualExecutor());
    }

    @Override
    public @NotNull Promise<Void> thenConsumeDelayedVirtual(@NotNull ExceptionalConsumer<T> task, long delay,
                                                            @NotNull TimeUnit unit) {
        return thenApplyDelayed(FunctionAdapter.adapt(task), delay, unit, getFactory().getVirtualExecutor());
    }

    @Override
    public <V> @NotNull Promise<V> thenSupplyVirtual(@NotNull ExceptionalSupplier<V> task) {
        return thenApply(FunctionAdapter.adapt(task), getFactory().getVirtualExecutor());
    }

    @Override
    public <V> @NotNull Promise<V> thenSupplyDelayedVirtual(@NotNull ExceptionalSupplier<V> task, long delay,
                                                            @NotNull TimeUnit unit) {
        return thenApplyDelayed(FunctionAdapter.adapt(task), delay, unit, getFactory().getVirtualExecutor());
    }

    @Override
    public <V> @NotNull Promise<V> thenApplyVirtual(@NotNull ExceptionalFunction<T, V> task) {
        return thenApply(task, getFactory().getVirtualExecutor());
    }

    @Override
    public <V> @NotNull Promise<V> thenApplyDelayedVirtual(@NotNull ExceptionalFunction<T, V> task, long delay,
                                                           @NotNull TimeUnit unit) {
        return thenApplyDelayed(task, delay, unit, getFactory().getVirtualExecutor());
    }

    @Override
    public <V> @NotNull Promise<V> thenComposeVirtual(@NotNull ExceptionalFunction<T, Promise<V>> task) {
        return thenCompose(task, getFactory().getVirtualExecutor());
    }

    @Override
    public @NotNull Promise<T> thenPopulateReference(@NotNull AtomicReference<T> reference) {
        return thenApply(result -> {
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
    public @NotNull Promise<T> addAsyncListener(@Nullable Consumer<T> successListener,
                                                @Nullable Consumer<Throwable> errorListener) {
        return addAsyncListener(res -> {
            if (res.isSuccess()) {
                if (successListener != null) {
                    successListener.accept(res.getResult());
                }
            } else {
                if (errorListener != null) {
                    errorListener.accept(res.getException());
                }
            }
        });
    }

    @Override
    public @NotNull Promise<T> addDirectListener(@NotNull PromiseListener<T> listener) {
        return addAnyListener(listener);
    }

    @Override
    public @NotNull Promise<T> addDirectListener(@Nullable Consumer<T> successListener,
                                                 @Nullable Consumer<Throwable> errorListener) {
        return addDirectListener(res -> {
            if (res.isSuccess()) {
                if (successListener != null) {
                    successListener.accept(res.getResult());
                }
            } else {
                if (errorListener != null) {
                    errorListener.accept(res.getException());
                }
            }
        });
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
        return onError(e -> {
            if (e instanceof CancellationException && e.getMessage() == null && e.getCause() == null) {
                // ignore cancellation exceptions without a message or cause
                return;
            }

            getLogger().error(message, wrapper.initCause(e));
        });
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
    public @NotNull Promise<T> orDefault(@Nullable T defaultValue) {
        return orDefault(_ -> defaultValue);
    }

    @Override
    public @NotNull Promise<T> orDefault(@NotNull ExceptionalSupplier<T> supplier) {
        return orDefault(_ -> supplier.get());
    }

    @Override
    public @NotNull Promise<T> orDefault(@NotNull ExceptionalFunction<Throwable, T> function) {
        return useCompletion(() -> {
            CompletablePromise<T> promise = createLinked();
            addDirectListener(promise::complete, e -> runCompleter(promise, () -> promise.complete(function.apply(e))));
            return promise;
        }, getFactory()::resolve, getFactory()::error);
    }

    private static class DeferredExecutionException extends ExecutionException {

    }

}
