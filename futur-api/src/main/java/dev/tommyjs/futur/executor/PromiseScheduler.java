package dev.tommyjs.futur.executor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * A scheduler for running tasks after a delay.
 */
public interface PromiseScheduler<T> {

    static @NotNull PromiseScheduler<?> getDefault() {
        return PromiseSchedulerDefault.INSTANCE;
    }

    /**
     * Runs the given task after the given delay.
     *
     * @param task  the task
     * @param delay the delay
     * @param unit  the time unit
     * @return the task
     * @throws Exception if scheduling the task failed
     */
    @NotNull T schedule(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) throws Exception;

    /**
     * Cancels the given task if possible. This may interrupt the task mid-execution.
     *
     * @param task the task
     * @return {@code true} if the task was cancelled. {@code false} if the task was already completed
     * or could not be cancelled.
     */
    boolean cancel(@NotNull T task);

}
