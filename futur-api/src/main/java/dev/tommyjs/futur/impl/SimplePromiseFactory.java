package dev.tommyjs.futur.impl;

import dev.tommyjs.futur.promise.AbstractPromise;
import dev.tommyjs.futur.promise.Promise;
import dev.tommyjs.futur.promise.PromiseFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.concurrent.ScheduledExecutorService;

public class SimplePromiseFactory implements PromiseFactory {

    private final ScheduledExecutorService executor;
    private final Logger logger;

    public SimplePromiseFactory(ScheduledExecutorService executor, Logger logger) {
        this.executor = executor;
        this.logger = logger;
    }

    @Override
    public @NotNull <T> Promise<T> resolve(T value) {
        AbstractPromise<T> promise = new SimplePromise<>(executor, logger, this);
        promise.complete(value);
        return promise;
    }

    @Override
    public @NotNull <T> Promise<T> unresolved() {
        return new SimplePromise<>(executor, logger, this);
    }

    @Override
    public @NotNull <T> Promise<T> error(Throwable error) {
        AbstractPromise<T> promise = new SimplePromise<>(executor, logger, this);
        promise.completeExceptionally(error);
        return promise;
    }

}
