package dev.tommyjs.futur.standalone;

import dev.tommyjs.futur.scheduler.Scheduler;
import dev.tommyjs.futur.trace.ExecutorTrace;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExclusiveThreadPoolScheduler implements Scheduler {

    private final ScheduledExecutorService executor;

    protected ExclusiveThreadPoolScheduler(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void runSync(@NotNull Runnable task, @NotNull ExecutorTrace trace) {
        throw new UnsupportedOperationException("Sync task invoked on asynchronous environment");
    }

    @Override
    public void runDelayedSync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace) {
        throw new UnsupportedOperationException("Sync task invoked on asynchronous environment");
    }

    @Override
    public void runRepeatingSync(@NotNull Runnable task, long interval, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace) {
        throw new UnsupportedOperationException("Sync task invoked on asynchronous environment");
    }

    @Override
    public void runAsync(@NotNull Runnable task, @NotNull ExecutorTrace trace) {
        executor.submit(wrapExceptions(task, trace));
    }

    @Override
    public void runDelayedAsync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace) {
        executor.schedule(wrapExceptions(task, trace), delay, unit);
    }

    @Override
    public void runRepeatingAsync(@NotNull Runnable task, long interval, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace) {
        executor.scheduleAtFixedRate(wrapExceptions(task, trace), 0L, interval, unit);
    }

    public @NotNull ScheduledExecutorService getExecutor() {
        return executor;
    }

    public static ExclusiveThreadPoolScheduler create(ScheduledExecutorService executor) {
        return new ExclusiveThreadPoolScheduler(executor);
    }

    public static ExclusiveThreadPoolScheduler create(int nThreads) {
        return create(Executors.newScheduledThreadPool(nThreads));
    }

}
