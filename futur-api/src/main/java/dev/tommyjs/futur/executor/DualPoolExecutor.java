package dev.tommyjs.futur.executor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DualPoolExecutor implements PromiseExecutor<ScheduledFuture<?>> {

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
    public ScheduledFuture<?> runSync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        return syncSvc.schedule(task, delay, unit);
    }

    @Override
    public ScheduledFuture<?> runAsync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        return asyncSvc.schedule(task, delay, unit);
    }

    @Override
    public void cancel(ScheduledFuture<?> task) {
        task.cancel(true);
    }

}
