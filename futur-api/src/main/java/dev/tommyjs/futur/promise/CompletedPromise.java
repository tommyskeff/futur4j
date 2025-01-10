package dev.tommyjs.futur.promise;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public abstract class CompletedPromise<T, FS, FA> extends AbstractPromise<T, FS, FA> {

    private static final PromiseCompletion<?> EMPTY = new PromiseCompletion<>();

    private final @NotNull PromiseCompletion<T> completion;

    public CompletedPromise(@NotNull PromiseCompletion<T> completion) {
        this.completion = completion;
    }

    @SuppressWarnings("unchecked")
    public CompletedPromise() {
        this((PromiseCompletion<T>) EMPTY);
    }

    @Override
    protected @NotNull Promise<T> addAnyListener(@NotNull PromiseListener<T> listener) {
        callListener(listener, completion);
        return this;
    }

    @Override
    public @NotNull Promise<T> timeout(long time, @NotNull TimeUnit unit) {
        // Promise is already completed so can't time out
        return this;
    }

    @Override
    public @NotNull Promise<T> maxWaitTime(long time, @NotNull TimeUnit unit) {
        // Promise is already completed so can't time out
        return this;
    }

    @Override
    public void cancel(@NotNull CancellationException exception) {
        // Promise is already completed so can't be cancelled
    }

    @Override
    public T get() throws ExecutionException {
        return joinCompletionChecked();
    }

    @Override
    public T get(long timeout, @NotNull TimeUnit unit) throws ExecutionException {
        return joinCompletionChecked();
    }

    @Override
    public T await() {
        return joinCompletionUnchecked();
    }

    @Override
    public @NotNull PromiseCompletion<T> getCompletion() {
        return completion;
    }

    @Override
    public boolean isCompleted() {
        return true;
    }

}
