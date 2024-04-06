package dev.tommyjs.futur.executor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DualPoolExecutor implements PromiseExecutor<Future<?>> {

    private final @NotNull ScheduledExecutorService syncSvc;
    private final @NotNull ScheduledExecutorService asyncSvc;

    public DualPoolExecutor(@NotNull ScheduledExecutorService syncSvc, @NotNull ScheduledExecutorService asyncSvc) {
        this.syncSvc = syncSvc;
        this.asyncSvc = asyncSvc;
    }

    public static @NotNull DualPoolExecutor create(int asyncPoolSize) {
        return new DualPoolExecutor(Executors.newSingleThreadScheduledExecutor(), Executors.newScheduledThreadPool(asyncPoolSize));
    }

    @Override
    public Future<?> runSync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        return syncSvc.schedule(task, delay, unit);
    }

    @Override
    public Future<?> runAsync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        return asyncSvc.schedule(task, delay, unit);
    }

    @Override
    public void cancel(Future<?> task) {
        task.cancel(true);
    }

}
