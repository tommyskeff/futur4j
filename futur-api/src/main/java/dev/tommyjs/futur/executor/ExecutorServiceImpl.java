package dev.tommyjs.futur.executor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

class ExecutorServiceImpl implements PromiseExecutor<Future<?>> {

    private final ExecutorService executor;

    public ExecutorServiceImpl(@NotNull ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public @NotNull Future<?> run(@NotNull Runnable task) {
        return executor.submit(task);
    }

    @Override
    public boolean cancel(@NotNull Future<?> task) {
        return task.cancel(true);
    }

    @Override
    public @NotNull PromiseScheduler<?> scheduler() {
        return PromiseScheduler.getDefault();
    }

}
