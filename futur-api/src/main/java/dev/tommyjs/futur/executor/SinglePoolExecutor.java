package dev.tommyjs.futur.executor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SinglePoolExecutor extends DualPoolExecutor {
    
    public SinglePoolExecutor(@NotNull ScheduledExecutorService service) {
        super(service, service);
    }

    public static @NotNull SinglePoolExecutor create(int threadPoolSize) {
        return new SinglePoolExecutor(Executors.newScheduledThreadPool(threadPoolSize));
    }

}
