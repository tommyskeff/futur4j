package dev.tommyjs.futur.impl;

import dev.tommyjs.futur.executor.PromiseExecutor;
import dev.tommyjs.futur.promise.AbstractPromise;
import dev.tommyjs.futur.promise.PromiseFactory;
import org.slf4j.Logger;

public class SimplePromise<T> extends AbstractPromise<T> {

    private final PromiseExecutor executor;
    private final Logger logger;
    private final PromiseFactory factory;

    public SimplePromise(PromiseExecutor executor, Logger logger, PromiseFactory factory) {
        this.executor = executor;
        this.logger = logger;
        this.factory = factory;
    }

    @Override
    protected PromiseExecutor getExecutor() {
        return executor;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    public PromiseFactory getFactory() {
        return factory;
    }

}
