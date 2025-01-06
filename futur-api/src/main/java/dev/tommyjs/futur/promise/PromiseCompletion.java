package dev.tommyjs.futur.promise;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CancellationException;

/**
 * Represents the result of a {@link Promise}, containing either an optional result or an exception.
 */
public class PromiseCompletion<T> {

    private @Nullable T result;
    private @Nullable Throwable exception;

    /**
     * Creates a new successful completion.
     * @param result the result
     */
    public PromiseCompletion(@Nullable T result) {
        this.result = result;
    }

    /**
     * Creates a new exceptional completion.
     * @param exception the exception
     */
    public PromiseCompletion(@NotNull Throwable exception) {
        this.exception = exception;
    }

    /**
     * Creates a new successful completion with a result of {@code null}.
     */
    public PromiseCompletion() {
        this((T) null);
    }

    /**
     * Checks if the completion was successful.
     * @return {@code true} if the completion was successful, {@code false} otherwise
     */
    public boolean isSuccess() {
        return exception == null;
    }

    /**
     * Checks if the completion was exceptional.
     * @return {@code true} if the completion was exceptional, {@code false} otherwise
     */
    public boolean isError() {
        return exception != null;
    }

    /**
     * Checks if the completion was cancelled.
     * @return {@code true} if the completion was cancelled, {@code false} otherwise
     */
    public boolean wasCancelled() {
        return exception instanceof CancellationException;
    }

    @Deprecated
    public boolean wasCanceled() {
        return wasCancelled();
    }

    /**
     * Gets the result of the completion.
     * @return the result, or {@code null} if the completion was exceptional
     */
    public @Nullable T getResult() {
        return result;
    }

    /**
     * Gets the exception of the completion.
     * @return the exception, or {@code null} if the completion was successful
     */
    public @Nullable Throwable getException() {
        return exception;
    }

}
