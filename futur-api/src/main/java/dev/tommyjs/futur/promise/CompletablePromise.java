package dev.tommyjs.futur.promise;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link Promise} that can be completed.
 */
public interface CompletablePromise<T> extends Promise<T> {

    /**
     * Completes the promise successfully with the given result.
     * @param result the result
     */
    void complete(@Nullable T result);

    /**
     * Completes the promise exceptionally with the given exception.
     * @param result the exception
     */
    void completeExceptionally(@NotNull Throwable result);

}
