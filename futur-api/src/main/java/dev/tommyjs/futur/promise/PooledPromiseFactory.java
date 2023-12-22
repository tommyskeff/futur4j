package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.scheduler.Scheduler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class PooledPromiseFactory implements PromiseFactory {

    private final Scheduler scheduler;
    private final Logger logger;

    public PooledPromiseFactory(Scheduler scheduler, Logger logger) {
        this.scheduler = scheduler;
        this.logger = logger;
    }

    @Override
    public @NotNull <T> Promise<T> resolve(T value) {
        AbstractPromise<T> promise = new PooledPromise<>(scheduler, logger, this);
        promise.setCompletion(new PromiseCompletion<>(value));
        return promise;
    }

    @Override
    public @NotNull <T> Promise<T> unresolved() {
        return new PooledPromise<>(scheduler, logger, this);
    }

    @Override
    public @NotNull <T> Promise<T> error(Throwable error) {
        AbstractPromise<T> promise = new PooledPromise<>(scheduler, logger, this);
        promise.completeExceptionally(error);
        return promise;
    }

    @Override
    public @NotNull Promise<Void> start() {
        return resolve(null);
    }

}
