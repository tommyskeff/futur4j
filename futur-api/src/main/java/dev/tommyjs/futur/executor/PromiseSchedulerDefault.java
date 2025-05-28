package dev.tommyjs.futur.executor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

class PromiseSchedulerDefault implements PromiseScheduler<ScheduledFuture<?>> {

    static final PromiseSchedulerDefault INSTANCE = new PromiseSchedulerDefault();

    private final ScheduledExecutorService executor;

    PromiseSchedulerDefault() {
        ThreadFactory factory = Thread.ofPlatform().name("promise-scheduler").daemon(true).factory();
        this.executor = Executors.newSingleThreadScheduledExecutor(factory);
    }

    @Override
    public @NotNull ScheduledFuture<?> schedule(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        return executor.schedule(task, delay, unit);
    }

    @Override
    public boolean cancel(@NotNull ScheduledFuture<?> task) {
        return task.cancel(true);
    }

}
