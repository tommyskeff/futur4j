package dev.tommyjs.futur.promise;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class StaticPromiseFactory implements PromiseFactory {

    public static final @NotNull PromiseFactory INSTANCE;
    public static final @NotNull ScheduledExecutorService EXECUTOR;
    public static final @NotNull Logger LOGGER;

    static {
        INSTANCE = new StaticPromiseFactory();
        EXECUTOR = Executors.newSingleThreadScheduledExecutor();
        LOGGER = LoggerFactory.getLogger(StaticPromiseFactory.class);
    }

    private StaticPromiseFactory() {
    }

    @Override
    public @NotNull <T> Promise<T> resolve(T value) {
        AbstractPromise<T> promise = new StaticPromise<>();
        promise.setCompletion(new PromiseCompletion<>(value));
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
