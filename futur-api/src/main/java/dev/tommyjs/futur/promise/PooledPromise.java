package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.scheduler.Scheduler;
import org.slf4j.Logger;

public class PooledPromise<T> extends AbstractPromise<T> {

    private final Scheduler scheduler;
    private final Logger logger;
    private final PromiseFactory factory;

    public PooledPromise(Scheduler scheduler, Logger logger, PromiseFactory factory) {
        this.scheduler = scheduler;
        this.logger = logger;
        this.factory = factory;
    }

    @Override
    protected Scheduler getScheduler() {
        return scheduler;
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
