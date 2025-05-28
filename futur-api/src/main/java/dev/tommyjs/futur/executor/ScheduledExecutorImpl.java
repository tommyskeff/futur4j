package dev.tommyjs.futur.executor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class ScheduledExecutorImpl implements PromiseExecutor<Future<?>>, PromiseScheduler<Future<?>> {

    private final ScheduledExecutorService executor;

    public ScheduledExecutorImpl(@NotNull ScheduledExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public @NotNull Future<?> run(@NotNull Runnable task) {
        return executor.submit(task);
    }

    @Override
    public @NotNull Future<?> schedule(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        return executor.schedule(task, delay, unit);
    }

    @Override
    public boolean cancel(@NotNull Future<?> task) {
        return task.cancel(true);
    }

    @Override
    public @NotNull PromiseScheduler<Future<?>> scheduler() {
        return this;
    }

}
