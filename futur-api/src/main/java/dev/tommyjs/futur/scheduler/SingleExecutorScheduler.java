package dev.tommyjs.futur.scheduler;

import dev.tommyjs.futur.trace.ExecutorTrace;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SingleExecutorScheduler implements Scheduler {

    private final ScheduledExecutorService service;

    public SingleExecutorScheduler(ScheduledExecutorService service) {
        this.service = service;
    }

    @Override
    public void runSync(@NotNull Runnable task, @NotNull ExecutorTrace trace) {
        service.submit(Scheduler.wrapExceptions(task, trace));
    }

    @Override
    public void runDelayedSync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace) {
        service.schedule(Scheduler.wrapExceptions(task, trace), delay, unit);
    }

    @Override
    public void runRepeatingSync(@NotNull Runnable task, long interval, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace) {
        service.scheduleAtFixedRate(Scheduler.wrapExceptions(task, trace), 0L, interval, unit);
    }

    @Override
    public void runAsync(@NotNull Runnable task, @NotNull ExecutorTrace trace) {
        runSync(task, trace);
    }

    @Override
    public void runDelayedAsync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace) {
        runDelayedSync(task, delay, unit, trace);
    }

    @Override
    public void runRepeatingAsync(@NotNull Runnable task, long interval, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace) {
        runRepeatingSync(task, interval, unit, trace);
    }

}
