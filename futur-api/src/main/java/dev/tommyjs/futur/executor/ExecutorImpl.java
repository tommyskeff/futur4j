package dev.tommyjs.futur.executor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;

class ExecutorImpl implements PromiseExecutor<Void> {

    private final Executor executor;

    public ExecutorImpl(@NotNull Executor executor) {
        this.executor = executor;
    }

    @Override
    public @NotNull Void run(@NotNull Runnable task) {
        executor.execute(task);
        return null;
    }

    @Override
    public boolean cancel(@NotNull Void task) {
        return false;
    }

    @Override
    public @Nullable PromiseScheduler<?> scheduler() {
        return null;
    }

}
