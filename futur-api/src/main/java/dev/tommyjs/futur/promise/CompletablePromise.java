package dev.tommyjs.futur.promise;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CompletablePromise<T> extends Promise<T> {

    void complete(@Nullable T result);

    void completeExceptionally(@NotNull Throwable result);

}
