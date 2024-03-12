package dev.tommyjs.futur.executor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public interface PromiseExecutor {

    void runSync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit);

    void runAsync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit);

}
