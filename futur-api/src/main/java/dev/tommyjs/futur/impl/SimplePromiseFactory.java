package dev.tommyjs.futur.impl;

import dev.tommyjs.futur.executor.PromiseExecutor;
import dev.tommyjs.futur.promise.AbstractPromiseFactory;
import dev.tommyjs.futur.promise.Promise;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class SimplePromiseFactory<F> extends AbstractPromiseFactory<F> {

    private final PromiseExecutor<F> executor;
    private final Logger logger;

    public SimplePromiseFactory(PromiseExecutor<F> executor, Logger logger) {
        this.executor = executor;
        this.logger = logger;
    }

    @Override
    public @NotNull <T> Promise<T> unresolved() {
        return new SimplePromise<>(this);
    }

    @Override
    public @NotNull Logger getLogger() {
        return logger;
    }

    @Override
    public @NotNull PromiseExecutor<F> getExecutor() {
        return executor;
    }

}
