package dev.tommyjs.futur.impl;

import dev.tommyjs.futur.executor.PromiseExecutor;
import dev.tommyjs.futur.promise.AbstractPromise;
import dev.tommyjs.futur.promise.PromiseFactory;
import org.slf4j.Logger;

public class StaticPromise<T> extends AbstractPromise<T> {

    @Override
    protected PromiseExecutor getExecutor() {
        return StaticPromiseFactory.EXECUTOR;
    }

    @Override
    protected Logger getLogger() {
        return StaticPromiseFactory.LOGGER;
    }

    @Override
    public PromiseFactory getFactory() {
        return StaticPromiseFactory.INSTANCE;
    }

}
