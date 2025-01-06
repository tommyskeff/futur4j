package dev.tommyjs.futur.util;

import dev.tommyjs.futur.promise.CompletablePromise;
import dev.tommyjs.futur.promise.Promise;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class PromiseUtil {

    /**
     * Propagates the completion, once completed, of the given promise to the given promise.
     *
     * @param from the promise to propagate the completion from
     * @param to   the completable promise to propagate the completion to
     */
    public static <V> void propagateCompletion(@NotNull Promise<V> from, @NotNull CompletablePromise<V> to) {
        from.addDirectListener(to::complete, to::completeExceptionally);
    }

    /**
     * Propagates the cancellation, once cancelled, of the given promise to the given promise.
     *
     * @param from the promise to propagate the cancellation from
     * @param to   the promise to propagate the cancellation to
     */
    public static void propagateCancel(@NotNull Promise<?> from, @NotNull Promise<?> to) {
        from.onCancel(to::cancel);
    }

    /**
     * Cancels the given promise once the given promise is completed.
     *
     * @param from the promise to propagate the completion from
     * @param to   the promise to cancel upon completion
     */
    public static void cancelOnComplete(@NotNull Promise<?> from, @NotNull Promise<?> to) {
        from.addDirectListener(_ -> to.cancel());
    }

    /**
     * Estimates the size of the given stream.
     *
     * @param stream the stream
     * @return the estimated size
     */
    public static int estimateSize(@NotNull Stream<?> stream) {
        long estimate = stream.spliterator().estimateSize();
        return estimate == Long.MAX_VALUE ? 10 : (int) estimate;
    }

}
