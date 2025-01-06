package dev.tommyjs.futur.executor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public interface PromiseExecutor<T> {

    static PromiseExecutor<?> virtualThreaded() {
        return new VirtualThreadImpl();
    }

    static PromiseExecutor<?> singleThreaded() {
        return of(Executors.newSingleThreadScheduledExecutor());
    }

    static PromiseExecutor<?> multiThreaded(int threads) {
        return of(Executors.newScheduledThreadPool(threads));
    }

    static PromiseExecutor<?> of(@NotNull ScheduledExecutorService service) {
        return new ExecutorServiceImpl(service);
    }

    T run(@NotNull Runnable task) throws Exception;

    T run(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) throws Exception;

    void cancel(T task);

}
