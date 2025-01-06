package dev.tommyjs.futur.executor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class ExecutorServiceImpl implements PromiseExecutor<Future<?>> {

    private final ScheduledExecutorService service;

    public ExecutorServiceImpl(@NotNull ScheduledExecutorService service) {
        this.service = service;
    }

    @Override
    public Future<?> run(@NotNull Runnable task) {
        return service.submit(task);
    }

    @Override
    public Future<?> run(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        return service.schedule(task, delay, unit);
    }

    @Override
    public void cancel(Future<?> task) {
        task.cancel(true);
    }

}
