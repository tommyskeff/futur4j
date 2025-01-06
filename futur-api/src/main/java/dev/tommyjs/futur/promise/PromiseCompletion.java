package dev.tommyjs.futur.promise;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CancellationException;

public class PromiseCompletion<T> {

    private @Nullable T result;
    private @Nullable Throwable exception;

    public PromiseCompletion(@Nullable T result) {
        this.result = result;
    }

    public PromiseCompletion(@NotNull Throwable exception) {
        this.exception = exception;
    }

    public PromiseCompletion() {
        this.result = null;
    }

    public boolean isSuccess() {
        return exception == null;
    }

    public boolean isError() {
        return exception != null;
    }

    public boolean wasCanceled() {
        return exception instanceof CancellationException;
    }

    public @Nullable T getResult() {
        return result;
    }

    public @Nullable Throwable getException() {
        return exception;
    }

}
