package dev.tommyjs.futur.executor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SinglePoolExecutor implements PromiseExecutor {

    private final @NotNull ScheduledExecutorService service;

    public SinglePoolExecutor(@NotNull ScheduledExecutorService service) {
        this.service = service;
    }

    @Override
    public void runSync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        service.schedule(task, delay, unit);
    }

    @Override
    public void runAsync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        service.schedule(task, delay, unit);
    }

    public static @NotNull SinglePoolExecutor create(int threadPoolSize) {
        return new SinglePoolExecutor(Executors.newScheduledThreadPool(threadPoolSize));
    }

}
