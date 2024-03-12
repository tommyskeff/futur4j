package dev.tommyjs.futur.impl;

import dev.tommyjs.futur.executor.PromiseExecutor;
import dev.tommyjs.futur.executor.SinglePoolExecutor;
import dev.tommyjs.futur.promise.AbstractPromise;
import dev.tommyjs.futur.promise.Promise;
import dev.tommyjs.futur.promise.PromiseFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

public class StaticPromiseFactory implements PromiseFactory {

    public static final @NotNull PromiseFactory INSTANCE;
    public static final @NotNull PromiseExecutor EXECUTOR;
    public static final @NotNull Logger LOGGER;

    static {
        INSTANCE = new StaticPromiseFactory();
        EXECUTOR = SinglePoolExecutor.create(1);
        LOGGER = LoggerFactory.getLogger(StaticPromiseFactory.class);
    }

    private StaticPromiseFactory() {
    }

    @Override
    public @NotNull <T> Promise<T> resolve(T value) {
        AbstractPromise<T> promise = new StaticPromise<>();
        promise.complete(value);
        return promise;
    }

    @Override
    public @NotNull <T> Promise<T> unresolved() {
        return new StaticPromise<>();
    }

    @Override
    public @NotNull <T> Promise<T> error(Throwable error) {
        AbstractPromise<T> promise = new StaticPromise<>();
        promise.completeExceptionally(error);
        return promise;
    }

}
