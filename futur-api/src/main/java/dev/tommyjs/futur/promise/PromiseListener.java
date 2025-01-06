package dev.tommyjs.futur.promise;

import org.jetbrains.annotations.NotNull;

/**
 * A listener for a {@link Promise} that is called when the promise is resolved.
 */
public interface PromiseListener<T> {

    /**
     * Handles the completion of the promise.
     * @param completion the promise completion
     */
    void handle(@NotNull PromiseCompletion<T> completion);

}
