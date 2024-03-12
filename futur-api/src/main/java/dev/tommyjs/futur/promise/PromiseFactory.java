package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.executor.PromiseExecutor;
import dev.tommyjs.futur.executor.SinglePoolExecutor;
import dev.tommyjs.futur.impl.SimplePromiseFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

public interface PromiseFactory {

    <T> @NotNull Promise<T> resolve(T value);

    <T> @NotNull Promise<T> unresolved();

    <T> @NotNull Promise<T> error(Throwable error);

    static PromiseFactory create(PromiseExecutor executor, Logger logger) {
        return new SimplePromiseFactory(executor, logger);
    }

    static PromiseFactory create(PromiseExecutor executor) {
        return create(executor, LoggerFactory.getLogger(SimplePromiseFactory.class));
    }

    static PromiseFactory create(int threadPoolSize) {
        return create(SinglePoolExecutor.create(threadPoolSize));
    }

    static PromiseFactory create() {
        return create(Runtime.getRuntime().availableProcessors());
    }

}
