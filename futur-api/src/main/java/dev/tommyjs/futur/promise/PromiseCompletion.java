package dev.tommyjs.futur.promise;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PromiseCompletion<T> {

    private @Nullable T result;
    private @Nullable Throwable exception;
    private boolean active;

    public PromiseCompletion(@Nullable T result) {
        this.result = result;
        this.active = true;
    }

    public PromiseCompletion(@NotNull Throwable exception) {
        this.exception = exception;
        this.active = true;
    }

    public PromiseCompletion() {
        this.result = null;
        this.active = true;
    }

    public void markHandled() {
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isError() {
        return getException() != null;
    }

    public @Nullable T getResult() {
        return result;
    }

    public @Nullable Throwable getException() {
        return exception;
    }

}
