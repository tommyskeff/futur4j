package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.executor.PromiseExecutor;
import org.jetbrains.annotations.NotNull;
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

    public class PromiseImpl<T> extends AbstractPromise<T, FS, FA> {

        @Override
        public @NotNull AbstractPromiseFactory<FS, FA> getFactory() {
            return PromiseFactoryImpl.this;
        }

    }

}
