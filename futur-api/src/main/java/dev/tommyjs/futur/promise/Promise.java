package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.function.ExceptionalConsumer;
import dev.tommyjs.futur.function.ExceptionalFunction;
import dev.tommyjs.futur.function.ExceptionalRunnable;
import dev.tommyjs.futur.function.ExceptionalSupplier;
import dev.tommyjs.futur.scheduler.Schedulers;
import dev.tommyjs.futur.trace.ExecutorTrace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class Promise<T> {

    private static final String PACKAGE;
    private static final Logger LOGGER = LoggerFactory.getLogger(Promise.class);

    static {
        String[] packageElements = Promise.class.getPackageName().split("\\.");
        int i = 0;

        StringBuilder packageBuilder = new StringBuilder();
        while (i < 3) {
            packageBuilder.append(packageElements[i]);
            i++;
        }

        PACKAGE = packageBuilder.toString();
    }

    private final Collection<PromiseListener<T>> listeners;
    private final StackTraceElement[] stackTrace;

    private @Nullable PromiseCompletion<T> completion;

    public Promise() {
        this.listeners = new ConcurrentLinkedQueue<>();
        this.completion = null;
        this.stackTrace = Arrays.stream(Thread.currentThread().getStackTrace())
            .filter(v -> !v.getClassName().startsWith(PACKAGE))
            .toArray(StackTraceElement[]::new);
    }

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

    public @NotNull Promise<Void> thenRunSync(@NotNull ExceptionalRunnable task) {
        return thenApplySync(result -> {
            task.run();
            return null;
        }, Schedulers.getTrace(task));
    }

    public @NotNull Promise<Void> thenRunDelayedSync(@NotNull ExceptionalRunnable task, long delay, @NotNull TimeUnit unit) {
        return thenApplyDelayedSync(result -> {
            task.run();
            return null;
        }, delay, unit, Schedulers.getTrace(task));
    }

    public @NotNull Promise<Void> thenConsumeSync(@NotNull ExceptionalConsumer<T> task) {
        return thenApplySync(result -> {
            task.accept(result);
            return null;
        }, Schedulers.getTrace(task));
    }

    public @NotNull Promise<Void> thenConsumeDelayedSync(@NotNull ExceptionalConsumer<T> task, long delay, @NotNull TimeUnit unit) {
        return thenApplyDelayedSync(result -> {
            task.accept(result);
            return null;
        }, delay, unit, Schedulers.getTrace(task));
    }

    public <V> @NotNull Promise<V> thenSupplySync(@NotNull ExceptionalSupplier<V> task) {
        return thenApplySync(result -> task.get(), Schedulers.getTrace(task));
    }

    public <V> @NotNull Promise<V> thenSupplyDelayedSync(@NotNull ExceptionalSupplier<V> task, long delay, @NotNull TimeUnit unit) {
        return thenApplyDelayedSync(result -> task.get(), delay, unit, Schedulers.getTrace(task));
    }

    public <V> @NotNull Promise<V> thenApplySync(@NotNull ExceptionalFunction<T, V> task, @NotNull ExecutorTrace trace) {
        Promise<V> promise = new Promise<>();
        addListener(ctx -> {
            if (ctx.isError()) {
                //noinspection ConstantConditions
                promise.completeExceptionally(ctx.getException());
                return;
            }

            Runnable runnable = createRunnable(ctx, promise, task);
            Schedulers.runSync(runnable, trace);
        });

        return promise;
    }

    public <V> @NotNull Promise<V> thenApplySync(@NotNull ExceptionalFunction<T, V> task) {
        return thenApplySync(task, Schedulers.getTrace(task));
    }

    public <V> @NotNull Promise<V> thenApplyDelayedSync(@NotNull ExceptionalFunction<T, V> task, long delay, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace) {
        Promise<V> promise = new Promise<>();
        addListener(ctx -> {
            if (ctx.isError()) {
                //noinspection ConstantConditions
                promise.completeExceptionally(ctx.getException());
                return;
            }

            Runnable runnable = createRunnable(ctx, promise, task);
            Schedulers.runDelayedSync(runnable, delay, unit, trace);
        });

        return promise;
    }

    public <V> @NotNull Promise<V> thenApplyDelayedSync(@NotNull ExceptionalFunction<T, V> task, long delay, @NotNull TimeUnit unit) {
        return thenApplyDelayedSync(task, delay, unit, Schedulers.getTrace(task));
    }

    public <V> @NotNull Promise<V> thenComposeSync(@NotNull ExceptionalFunction<T, @NotNull Promise<V>> task) {
        Promise<V> promise = new Promise<>();
        thenApplySync(task, Schedulers.getTrace(task)).thenConsumeAsync(nestedPromise -> {
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

    public @NotNull Promise<Void> thenRunAsync(@NotNull ExceptionalRunnable task) {
        return thenApplyAsync(result -> {
            task.run();
            return null;
        }, Schedulers.getTrace(task));
    }

    public @NotNull Promise<Void> thenRunDelayedAsync(@NotNull ExceptionalRunnable task, long delay, @NotNull TimeUnit unit) {
        return thenApplyDelayedAsync(result -> {
            task.run();
            return null;
        }, delay, unit, Schedulers.getTrace(task));
    }

    public @NotNull Promise<Void> thenConsumeAsync(@NotNull ExceptionalConsumer<T> task) {
        return thenApplyAsync(result -> {
            task.accept(result);
            return null;
        }, Schedulers.getTrace(task));
    }

    public @NotNull Promise<Void> thenConsumerDelayedAsync(@NotNull ExceptionalConsumer<T> task, long delay, @NotNull TimeUnit unit) {
        return thenApplyDelayedAsync(result -> {
            task.accept(result);
            return null;
        }, delay, unit, Schedulers.getTrace(task));
    }

    public <V> @NotNull Promise<V> thenSupplyAsync(@NotNull ExceptionalSupplier<V> task) {
        return thenApplyAsync(result -> task.get(), Schedulers.getTrace(task));
    }

    public <V> @NotNull Promise<V> thenSupplyDelayedAsync(@NotNull ExceptionalSupplier<V> task, long delay, @NotNull TimeUnit unit) {
        return thenApplyDelayedAsync(result -> task.get(), delay, unit, Schedulers.getTrace(task));
    }

    public @NotNull Promise<T> thenPopulateReference(@NotNull AtomicReference<T> reference) {
        return thenApplyAsync((result) -> {
            reference.set(result);
            return result;
        });
    }

    public <V> @NotNull Promise<V> thenApplyAsync(@NotNull ExceptionalFunction<T, V> task, @NotNull ExecutorTrace trace) {
        Promise<V> promise = new Promise<>();
        addListener(ctx -> {
            createRunnable(ctx, promise, task).run();
        });

        return promise;
    }

    public <V> @NotNull Promise<V> thenApplyAsync(@NotNull ExceptionalFunction<T, V> task) {
        return thenApplyAsync(task, Schedulers.getTrace(task));
    }

    public <V> @NotNull Promise<V> thenApplyDelayedAsync(@NotNull ExceptionalFunction<T, V> task, long delay, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace) {
        Promise<V> promise = new Promise<>();
        addListener(ctx -> {
            Runnable runnable = createRunnable(ctx, promise, task);
            Schedulers.runDelayedAsync(runnable, delay, unit, trace);
        });

        return promise;
    }

    public <V> @NotNull Promise<V> thenApplyDelayedAsync(@NotNull ExceptionalFunction<T, V> task, long delay, @NotNull TimeUnit unit) {
        return thenApplyDelayedAsync(task, delay, unit, Schedulers.getTrace(task));
    }

    public <V> @NotNull Promise<V> thenCompose(@NotNull ExceptionalFunction<T, Promise<V>> task) {
        return this.thenComposeAsync(task);
    }

    public <V> @NotNull Promise<V> thenComposeAsync(@NotNull ExceptionalFunction<T, Promise<V>> task) {
        Promise<V> promise = new Promise<>();
        thenApplyAsync(task, Schedulers.getTrace(task)).thenConsumeAsync(nestedPromise -> {
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
            } catch (Exception e) {
                promise.completeExceptionally(e, true);
            }
        };
    }

    public @NotNull Promise<T> logExceptions() {
        return addListener(ctx -> {
            if (ctx.isError()) {
                LOGGER.error("Exception caught in promise pipeline", ctx.getException());
            }
        });
    }

    public @NotNull Promise<T> addListener(@NotNull PromiseListener<T> listener) {
        if (isCompleted()) {
            Schedulers.runAsync(() -> {
                try {
                    listener.handle(getCompletion());
                } catch (Exception e) {
                    LOGGER.error("Exception caught in promise listener", e);
                }
            }, Schedulers.getTrace(listener));
        } else {
            getListeners().add(listener);
        }

        return this;
    }

    public @NotNull Promise<T> timeout(long time, @NotNull TimeUnit unit) {
        Schedulers.runDelayedAsync(() -> {
            if (!isCompleted()) {
                completeExceptionally(new TimeoutException("Promise timed out after " + time + " " + unit), true);
            }
        }, time, unit);

        return this;
    }

    public @NotNull Promise<T> timeout(long ms) {
        return timeout(ms, TimeUnit.MILLISECONDS);
    }

    protected void handleCompletion(@NotNull PromiseCompletion<T> ctx) {
        if (this.isCompleted()) return;
        setCompletion(ctx);

        Schedulers.runAsync(() -> {
            for (PromiseListener<T> listener : getListeners()) {
                if (!ctx.isActive()) return;

                try {
                    listener.handle(ctx);
                } catch (Exception e) {
                    LOGGER.error("Exception caught in promise listener", e);
                }
            }
        });
    }

    public void complete(@Nullable T result) {
        handleCompletion(new PromiseCompletion<>(result));
    }

    public void completeExceptionally(@NotNull Throwable result, boolean appendStacktrace) {
        if (appendStacktrace && this.stackTrace != null) {
            result.setStackTrace(Stream.of(result.getStackTrace(), this.stackTrace)
                .flatMap(Stream::of)
                .filter(v -> !v.getClassName().startsWith(PACKAGE))
                .filter(v -> !v.getClassName().startsWith("java.lang.Thread"))
                .filter(v -> !v.getClassName().startsWith("java.util.concurrent"))
                .toArray(StackTraceElement[]::new));
        }

        handleCompletion(new PromiseCompletion<>(result));
    }

    public void completeExceptionally(@NotNull Throwable result) {
        completeExceptionally(result, false);
    }

    public boolean isCompleted() {
        return getCompletion() != null;
    }

    protected Collection<PromiseListener<T>> getListeners() {
        return listeners;
    }

    public @Nullable PromiseCompletion<T> getCompletion() {
        return completion;
    }

    protected void setCompletion(@NotNull PromiseCompletion<T> completion) {
        this.completion = completion;
    }

    public static <T> @NotNull Promise<T> resolve(T value) {
        Promise<T> promise = new Promise<>();
        promise.setCompletion(new PromiseCompletion<>(value));
        return promise;
    }

    public static <T> @NotNull Promise<T> error(Throwable error) {
        Promise<T> promise = new Promise<>();
        promise.completeExceptionally(error);
        return promise;
    }

    public static @NotNull Promise<Void> start() {
        return Promise.resolve(null);
    }

    @Deprecated // use resolve()
    public static <T> @NotNull Promise<T> start(T start) {
        Promise<T> promise = new Promise<>();
        promise.complete(start);
        return promise;
    }

}
