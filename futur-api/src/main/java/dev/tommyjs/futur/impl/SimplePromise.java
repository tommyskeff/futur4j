package dev.tommyjs.futur.impl;

import dev.tommyjs.futur.promise.AbstractPromise;
import dev.tommyjs.futur.promise.PromiseFactory;
import org.slf4j.Logger;

import java.util.concurrent.ScheduledExecutorService;

public class SimplePromise<T> extends AbstractPromise<T> {

    private final ScheduledExecutorService executor;
    private final Logger logger;
    private final PromiseFactory factory;

    public SimplePromise(ScheduledExecutorService executor, Logger logger, PromiseFactory factory) {
        this.executor = executor;
        this.logger = logger;
        this.factory = factory;
    }

    @Override
    protected ScheduledExecutorService getExecutor() {
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
