package dev.tommyjs.futur.standalone;

import dev.tommyjs.futur.scheduler.Scheduler;
import dev.tommyjs.futur.trace.ExecutorTrace;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadPoolScheduler implements Scheduler {

    private final ScheduledExecutorService syncExecutor;
    private final ScheduledExecutorService asyncExecutor;

    protected ThreadPoolScheduler(ScheduledExecutorService syncExecutor, ScheduledExecutorService asyncExecutor) {
        this.syncExecutor = syncExecutor;
        this.asyncExecutor = asyncExecutor;
    }

    @Override
    public void runSync(@NotNull Runnable task, @NotNull ExecutorTrace trace) {
        syncExecutor.submit(Scheduler.wrapExceptions(task, trace));
    }

    @Override
    public void runDelayedSync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace) {
        syncExecutor.schedule(Scheduler.wrapExceptions(task, trace), delay, unit);
    }

    @Override
    public void runRepeatingSync(@NotNull Runnable task, long interval, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace) {
        syncExecutor.scheduleAtFixedRate(Scheduler.wrapExceptions(task, trace), 0L, interval, unit);
    }

    @Override
    public void runAsync(@NotNull Runnable task, @NotNull ExecutorTrace trace) {
        asyncExecutor.submit(Scheduler.wrapExceptions(task, trace));
    }

    @Override
    public void runDelayedAsync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace) {
        asyncExecutor.schedule(Scheduler.wrapExceptions(task, trace), delay, unit);
    }

    @Override
    public void runRepeatingAsync(@NotNull Runnable task, long interval, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace) {
        asyncExecutor.scheduleAtFixedRate(Scheduler.wrapExceptions(task, trace), 0L, interval, unit);
    }

    public @NotNull ScheduledExecutorService getSyncExecutor() {
        return syncExecutor;
    }

    public @NotNull ScheduledExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }

    public static ThreadPoolScheduler create(int nThreads) {
        return new ThreadPoolScheduler(Executors.newSingleThreadScheduledExecutor(), Executors.newScheduledThreadPool(nThreads));
    }

}
