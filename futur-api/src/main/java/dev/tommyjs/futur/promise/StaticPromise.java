package dev.tommyjs.futur.promise;

import org.slf4j.Logger;

import java.util.concurrent.ScheduledExecutorService;

public class StaticPromise<T> extends AbstractPromise<T> {

    @Override
    protected ScheduledExecutorService getExecutor() {
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
