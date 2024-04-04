package dev.tommyjs.futur.impl;

import dev.tommyjs.futur.executor.PromiseExecutor;
import dev.tommyjs.futur.promise.AbstractPromise;
import dev.tommyjs.futur.promise.AbstractPromiseFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class SimplePromise<T, F> extends AbstractPromise<T, F> {

    private final @NotNull AbstractPromiseFactory<F> factory;

    public SimplePromise(@NotNull AbstractPromiseFactory<F> factory) {
        this.factory = factory;
    }

    @Deprecated
    public SimplePromise(@NotNull PromiseExecutor<F> executor, @NotNull Logger logger, @NotNull AbstractPromiseFactory<F> factory) {
        this(factory);
    }

    @Override
    public @NotNull AbstractPromiseFactory<F> getFactory() {
        return factory;
    }

}
