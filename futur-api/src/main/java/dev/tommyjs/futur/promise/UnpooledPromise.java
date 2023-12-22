package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.scheduler.Scheduler;
import org.slf4j.Logger;

public class UnpooledPromise<T> extends AbstractPromise<T> {

    @Override
    protected Scheduler getScheduler() {
        return UnpooledPromiseFactory.SCHEDULER;
    }

    @Override
    protected Logger getLogger() {
        return UnpooledPromiseFactory.LOGGER;
    }

    @Override
    public PromiseFactory getFactory() {
        return UnpooledPromiseFactory.INSTANCE;
    }

}
