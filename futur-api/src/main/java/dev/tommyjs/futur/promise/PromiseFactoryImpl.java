package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.executor.PromiseExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class PromiseFactoryImpl<FS, FA> extends AbstractPromiseFactory<FS, FA> {

    private final @NotNull Logger logger;
    private final @NotNull PromiseExecutor<FS> syncExecutor;
    private final @NotNull PromiseExecutor<FA> asyncExecutor;

    public PromiseFactoryImpl(
        @NotNull Logger logger,
        @NotNull PromiseExecutor<FS> syncExecutor,
        @NotNull PromiseExecutor<FA> asyncExecutor
    ) {
        this.logger = logger;
        this.syncExecutor = syncExecutor;
        this.asyncExecutor = asyncExecutor;
    }

    @Override
    public @NotNull <T> CompletablePromise<T> unresolved() {
        return new PromiseImpl<>();
    }

    @Override
    public @NotNull <T> Promise<T> resolve(T value) {
        return new CompletedPromiseImpl<>(value);
    }

    @Override
    public @NotNull Promise<Void> start() {
        return new CompletedPromiseImpl<>();
    }

    @Override
    public @NotNull <T> Promise<T> error(@NotNull Throwable error) {
        return new CompletedPromiseImpl<>(error);
    }

    @Override
    public @NotNull Logger getLogger() {
        return logger;
    }

    @Override
    public @NotNull PromiseExecutor<FS> getSyncExecutor() {
        return syncExecutor;
    }

    @Override
    public @NotNull PromiseExecutor<FA> getAsyncExecutor() {
        return asyncExecutor;
    }

    private class PromiseImpl<T> extends BasePromise<T, FS, FA> {

        @Override
        public @NotNull AbstractPromiseFactory<FS, FA> getFactory() {
            return PromiseFactoryImpl.this;
        }

    }

    private class CompletedPromiseImpl<T> extends CompletedPromise<T, FS, FA> {

        public CompletedPromiseImpl(@Nullable T result) {
            super(new PromiseCompletion<>(result));
        }

        public CompletedPromiseImpl(@NotNull Throwable exception) {
            super(new PromiseCompletion<>(exception));
        }

        public CompletedPromiseImpl() {
            super();
        }

        @Override
        public @NotNull AbstractPromiseFactory<FS, FA> getFactory() {
            return PromiseFactoryImpl.this;
        }

    }

}
