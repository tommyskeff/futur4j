package dev.tommyjs.futur.lazy;

import dev.tommyjs.futur.executor.PromiseExecutor;
import dev.tommyjs.futur.executor.SinglePoolExecutor;
import dev.tommyjs.futur.impl.SimplePromise;
import dev.tommyjs.futur.promise.AbstractPromiseFactory;
import dev.tommyjs.futur.promise.Promise;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

public final class StaticPromiseFactory extends AbstractPromiseFactory<Future<?>> {

    public final static StaticPromiseFactory INSTANCE = new StaticPromiseFactory();
    private final static @NotNull SinglePoolExecutor EXECUTOR = SinglePoolExecutor.create(1);
    private final static @NotNull Logger LOGGER = LoggerFactory.getLogger(StaticPromiseFactory.class);

    private StaticPromiseFactory() {
    }

    @Override
    public @NotNull Logger getLogger() {
        return LOGGER;
    }

    @Override
    public @NotNull <T> Promise<T> unresolved() {
        return new SimplePromise<>(this);
    }

    @Override
    public @NotNull PromiseExecutor<Future<?>> getExecutor() {
        return EXECUTOR;
    }

}
