package dev.tommyjs.futur.function;

/**
 * Represents a runnable task that may throw an exception.
 * This is a functional interface whose functional method is {@link #run()}.
 */
@FunctionalInterface
public interface ExceptionalRunnable {

    /**
     * Performs this runnable task, potentially throwing an exception.
     *
     * @throws Exception if unable to complete the task
     */
    void run() throws Exception;

}