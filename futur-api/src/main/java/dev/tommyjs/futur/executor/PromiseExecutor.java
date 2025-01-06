package dev.tommyjs.futur.executor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public interface PromiseExecutor<T> {

    /**
     * Creates a new {@link PromiseExecutor} that runs tasks on virtual threads.
     *
     * @return the new executor
     */
    static PromiseExecutor<?> virtualThreaded() {
        return new VirtualThreadImpl();
    }

    /**
     * Creates a new {@link PromiseExecutor} that runs tasks on a single thread.
     *
     * @return the new executor
     */
    static PromiseExecutor<?> singleThreaded() {
        return of(Executors.newSingleThreadScheduledExecutor());
    }

    /**
     * Creates a new {@link PromiseExecutor} that runs tasks on multiple threads.
     *
     * @param threads the number of threads
     * @return the new executor
     */
    static PromiseExecutor<?> multiThreaded(int threads) {
        return of(Executors.newScheduledThreadPool(threads));
    }

    /**
     * Creates a new {@link PromiseExecutor} that runs tasks on the given executor service.
     *
     * @param service the executor service
     * @return the new executor
     */
    static PromiseExecutor<?> of(@NotNull ScheduledExecutorService service) {
        return new ExecutorServiceImpl(service);
    }

    /**
     * Runs the given task.
     *
     * @param task the task
     * @return the task
     * @throws Exception if scheduling the task failed
     */
    T run(@NotNull Runnable task) throws Exception;

    /**
     * Runs the given task after the given delay.
     *
     * @param task  the task
     * @param delay the delay
     * @param unit  the time unit
     * @return the task
     * @throws Exception if scheduling the task failed
     */
    T run(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) throws Exception;

    /**
     * Cancels the given task if possible. This may interrupt the task mid-execution.
     *
     * @param task the task
     * @return {@code true} if the task was cancelled. {@code false} if the task was already completed
     * or could not be cancelled.
     */
    boolean cancel(T task);

}
