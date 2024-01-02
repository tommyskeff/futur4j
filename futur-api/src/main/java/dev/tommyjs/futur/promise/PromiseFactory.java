package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.impl.SimplePromiseFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public interface PromiseFactory {

    <T> @NotNull Promise<T> resolve(T value);

    <T> @NotNull Promise<T> unresolved();

    <T> @NotNull Promise<T> error(Throwable error);

    static PromiseFactory create(ScheduledExecutorService executor, Logger logger) {
        return new SimplePromiseFactory(executor, logger);
    }

    static PromiseFactory create(ScheduledExecutorService executor) {
        return create(executor, LoggerFactory.getLogger(SimplePromiseFactory.class));
    }

    static PromiseFactory create(int threadPoolSize) {
        return create(Executors.newScheduledThreadPool(threadPoolSize));
    }

    static PromiseFactory create() {
        return create(Runtime.getRuntime().availableProcessors());
    }

}
