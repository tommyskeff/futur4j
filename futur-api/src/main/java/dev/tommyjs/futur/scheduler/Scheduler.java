package dev.tommyjs.futur.scheduler;

import dev.tommyjs.futur.trace.ExecutorTrace;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public interface Scheduler {

    Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

    void runSync(@NotNull Runnable task, @NotNull ExecutorTrace trace);

    void runDelayedSync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace);

    void runRepeatingSync(@NotNull Runnable task, long interval, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace);

    void runAsync(@NotNull Runnable task, @NotNull ExecutorTrace trace);

    void runDelayedAsync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace);

    void runRepeatingAsync(@NotNull Runnable task, long interval, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace);

    default @NotNull Runnable wrapExceptions(@NotNull Runnable task, @NotNull ExecutorTrace trace) {
        return () -> {
            try {
                task.run();
            } catch (Exception e) {
                LOGGER.error("Exception in scheduled task: {}", e.getClass().getName());
                LOGGER.error(trace.toString());
            }
        };
    }

}
