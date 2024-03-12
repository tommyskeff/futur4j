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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractPromise<T> implements Promise<T> {

    private final Collection<PromiseListener<T>> listeners;
    private final AtomicReference<PromiseCompletion<T>> completion;

    public AbstractPromise() {
        this.listeners = new ConcurrentLinkedQueue<>();
        this.completion = new AtomicReference<>();
    }

    protected abstract PromiseExecutor getExecutor();

    protected abstract Logger getLogger();

    @Override
    public T join(long interval, long timeout) throws TimeoutException {
        long start = System.currentTimeMillis();
        while (!isCompleted()) {
            if (System.currentTimeMillis() > start + timeout)
                throw new TimeoutException("Promise timed out after " + timeout + "ms");

            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        PromiseCompletion<T> completion = getCompletion();
        if (completion == null) {
            throw new IllegalStateException();
        }

        if (completion.isError()) {
            throw new RuntimeException(completion.getException());
        }

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
        addListener(ctx -> {
            if (ctx.isError()) {
                //noinspection ConstantConditions
                promise.completeExceptionally(ctx.getException());
                return;
            }

            Runnable runnable = createRunnable(ctx, promise, task);
            getExecutor().runSync(runnable, 0L, TimeUnit.MILLISECONDS);
        });

        return promise;
    }

    @Override
    public <V> @NotNull Promise<V> thenApplyDelayedSync(@NotNull ExceptionalFunction<T, V> task, long delay, @NotNull TimeUnit unit) {
        Promise<V> promise = getFactory().unresolved();
        addListener(ctx -> {
            if (ctx.isError()) {
                //noinspection ConstantConditions
                promise.completeExceptionally(ctx.getException());
                return;
            }

            Runnable runnable = createRunnable(ctx, promise, task);
            getExecutor().runSync(runnable, delay, unit);
        });

        return promise;
    }

    @Override
    public <V> @NotNull Promise<V> thenComposeSync(@NotNull ExceptionalFunction<T, @NotNull Promise<V>> task) {
        Promise<V> promise = getFactory().unresolved();
        thenApplySync(task).thenConsumeAsync(nestedPromise -> {
            nestedPromise.addListener(ctx1 -> {
                if (ctx1.isError()) {
                    //noinspection ConstantConditions
                    promise.completeExceptionally(ctx1.getException());
                    return;
                }

                promise.complete(ctx1.getResult());
            });
        }).addListener(ctx2 -> {
            if (ctx2.isError()) {
                //noinspection ConstantConditions
                promise.completeExceptionally(ctx2.getException());
            }
        });

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
        addListener(ctx -> {
            if (ctx.isError()) {
                //noinspection ConstantConditions
                promise.completeExceptionally(ctx.getException());
                return;
            }

            Runnable runnable = createRunnable(ctx, promise, task);
            getExecutor().runAsync(runnable, 0L, TimeUnit.MILLISECONDS);
        });

        return promise;
    }

    @Override
    public <V> @NotNull Promise<V> thenApplyDelayedAsync(@NotNull ExceptionalFunction<T, V> task, long delay, @NotNull TimeUnit unit) {
        Promise<V> promise = getFactory().unresolved();
        addListener(ctx -> {
            Runnable runnable = createRunnable(ctx, promise, task);
            getExecutor().runAsync(runnable, delay, unit);
        });

        return promise;
    }

    @Override
    public <V> @NotNull Promise<V> thenComposeAsync(@NotNull ExceptionalFunction<T, Promise<V>> task) {
        Promise<V> promise = getFactory().unresolved();
        thenApplyAsync(task).thenConsumeAsync(nestedPromise -> {
            nestedPromise.addListener(ctx1 -> {
                if (ctx1.isError()) {
                    //noinspection ConstantConditions
                    promise.completeExceptionally(ctx1.getException());
                    return;
                }

                promise.complete(ctx1.getResult());
            });
        }).addListener(ctx2 -> {
            if (ctx2.isError()) {
                //noinspection ConstantConditions
                promise.completeExceptionally(ctx2.getException());
            }
        });

        return promise;
    }

    private <V> @NotNull Runnable createRunnable(@NotNull PromiseCompletion<T> ctx, @NotNull Promise<V> promise, @NotNull ExceptionalFunction<T, V> task) {
        return () -> {
            if (ctx.isError()) {
                //noinspection ConstantConditions
                promise.completeExceptionally(ctx.getException());
                return;
            }

            try {
                V result = task.apply(ctx.getResult());
                promise.complete(result);
            } catch (Throwable e) {
                promise.completeExceptionally(e);
            }
        };
    }

    @Override
    public @NotNull Promise<T> logExceptions() {
        return addListener(ctx -> {
            if (ctx.isError()) {
                getLogger().error("Exception caught in promise chain", ctx.getException());
            }
        });
    }

    @Override
    public @NotNull Promise<T> addListener(@NotNull PromiseListener<T> listener) {
        if (isCompleted()) {
            getExecutor().runAsync(() -> {
                try {
                    //noinspection ConstantConditions
                    listener.handle(getCompletion());
                } catch (Exception e) {
                    getLogger().error("Exception caught in promise listener", e);
                }
            }, 0L, TimeUnit.MILLISECONDS);
        } else {
            getListeners().add(listener);
        }

        return this;
    }

    @Override
    public @NotNull Promise<T> timeout(long time, @NotNull TimeUnit unit) {
        getExecutor().runAsync(() -> {
            if (!isCompleted()) {
                completeExceptionally(new TimeoutException("Promise timed out after " + time + " " + unit));
            }
        }, time, unit);

        return this;
    }

    @Override
    public @NotNull Promise<T> timeout(long ms) {
        return timeout(ms, TimeUnit.MILLISECONDS);
    }

    protected void handleCompletion(@NotNull PromiseCompletion<T> ctx) {
        AtomicBoolean success = new AtomicBoolean();
        completion.getAndUpdate(c -> {
            if (c == null) {
                success.set(true);
                return ctx;
            } else {
                success.set(false);
                return c;
            }
        });

        if (success.get()) {
            handleCompletion0(ctx);
        }
    }

    protected void handleCompletion0(@NotNull PromiseCompletion<T> ctx) {
        getExecutor().runAsync(() -> {
            for (PromiseListener<T> listener : getListeners()) {
                if (!ctx.isActive()) return;

                try {
                    listener.handle(ctx);
                } catch (Exception e) {
                    getLogger().error("Exception caught in promise listener", e);
                }
            }
        }, 0L, TimeUnit.MILLISECONDS);
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

    protected Collection<PromiseListener<T>> getListeners() {
        return listeners;
    }

}
