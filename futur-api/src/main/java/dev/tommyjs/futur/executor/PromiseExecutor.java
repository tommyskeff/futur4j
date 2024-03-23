package dev.tommyjs.futur.executor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public interface PromiseExecutor {

    void runSync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit);

    void runAsync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit);

    default void runSync(@NotNull Runnable task) {
        runSync(task, 0L, TimeUnit.MILLISECONDS);
    }

    default void runAsync(@NotNull Runnable task) {
        runAsync(task, 0L, TimeUnit.MILLISECONDS);
    }

}
