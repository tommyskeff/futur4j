package dev.tommyjs.futur.executor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public interface PromiseExecutor<T> {

    T runSync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit);

    T runAsync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit);

    default T runSync(@NotNull Runnable task) {
        return runSync(task, 0L, TimeUnit.MILLISECONDS);
    }

    default T runAsync(@NotNull Runnable task) {
        return runAsync(task, 0L, TimeUnit.MILLISECONDS);
    }

    void cancel(T task);

}
