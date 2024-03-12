package dev.tommyjs.futur.executor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DualPoolExecutor implements PromiseExecutor {

    private final @NotNull ScheduledExecutorService syncSvc;
    private final @NotNull ScheduledExecutorService asyncSvc;

    public DualPoolExecutor(@NotNull ScheduledExecutorService syncSvc, @NotNull ScheduledExecutorService asyncSvc) {
        this.syncSvc = syncSvc;
        this.asyncSvc = asyncSvc;
    }

    @Override
    public void runSync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        syncSvc.schedule(task, delay, unit);
    }

    @Override
    public void runAsync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        asyncSvc.schedule(task, delay, unit);
    }

    public static @NotNull DualPoolExecutor create(int asyncPoolSize) {
        return new DualPoolExecutor(Executors.newSingleThreadScheduledExecutor(), Executors.newScheduledThreadPool(asyncPoolSize));
    }

}
