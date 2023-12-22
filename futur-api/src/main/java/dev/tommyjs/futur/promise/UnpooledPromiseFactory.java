package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.scheduler.Scheduler;
import dev.tommyjs.futur.scheduler.SingleExecutorScheduler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

public class UnpooledPromiseFactory implements PromiseFactory {

    public static final @NotNull PromiseFactory INSTANCE;
    public static final @NotNull Scheduler SCHEDULER;
    public static final @NotNull Logger LOGGER;

    static {
        INSTANCE = new UnpooledPromiseFactory();
        SCHEDULER = new SingleExecutorScheduler(Executors.newSingleThreadScheduledExecutor());
        LOGGER = LoggerFactory.getLogger(UnpooledPromiseFactory.class);
    }

    private UnpooledPromiseFactory() {
    }

    @Override
    public @NotNull <T> Promise<T> resolve(T value) {
        AbstractPromise<T> promise = new UnpooledPromise<>();
        promise.setCompletion(new PromiseCompletion<>(value));
        return promise;
    }

    @Override
    public @NotNull <T> Promise<T> unresolved() {
        return new UnpooledPromise<>();
    }

    @Override
    public @NotNull <T> Promise<T> error(Throwable error) {
        AbstractPromise<T> promise = new UnpooledPromise<>();
        promise.completeExceptionally(error);
        return promise;
    }

    @Override
    public @NotNull Promise<Void> start() {
        return resolve(null);
    }

}
