package dev.tommyjs.futur.scheduler;

import dev.tommyjs.futur.trace.ExecutorTrace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("ConstantConditions")
public class Schedulers {

    private static final Logger LOGGER = LoggerFactory.getLogger(Schedulers.class);

    private static @Nullable Scheduler scheduler;

    public static void runSync(@NotNull Runnable task, @NotNull ExecutorTrace trace) {
        ensureLoaded();
        getScheduler().runSync(task, trace);
    }

    public static void runSync(@NotNull Runnable task) {
        ensureLoaded();
        getScheduler().runSync(task, Schedulers.getTrace(task));
    }

    public static void runDelayedSync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace) {
        ensureLoaded();
        getScheduler().runDelayedSync(task, delay, unit, trace);
    }

    public static void runDelayedSync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        ensureLoaded();
        getScheduler().runDelayedSync(task, delay, unit, Schedulers.getTrace(task));
    }

    public static void runRepeatingSync(@NotNull Runnable task, long interval, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace) {
        ensureLoaded();
        getScheduler().runRepeatingSync(task, interval, unit, trace);
    }

    public static void runRepeatingSync(@NotNull Runnable task, long interval, @NotNull TimeUnit unit) {
        ensureLoaded();
        getScheduler().runRepeatingSync(task, interval, unit, Schedulers.getTrace(task));
    }

    public static void runAsync(@NotNull Runnable task, @NotNull ExecutorTrace trace) {
        ensureLoaded();
        getScheduler().runAsync(task, trace);
    }

    public static void runAsync(@NotNull Runnable task) {
        ensureLoaded();
        getScheduler().runAsync(task, Schedulers.getTrace(task));
    }

    public static void runDelayedAsync(@NotNull Runnable task, long delay, TimeUnit unit, ExecutorTrace trace) {
        ensureLoaded();
        getScheduler().runDelayedAsync(task, delay, unit, trace);
    }

    public static void runDelayedAsync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        ensureLoaded();
        getScheduler().runDelayedAsync(task, delay, unit, Schedulers.getTrace(task));
    }

    public static void runRepeatingAsync(@NotNull Runnable task, long interval, @NotNull TimeUnit unit, @NotNull ExecutorTrace trace) {
        ensureLoaded();
        getScheduler().runRepeatingAsync(task, interval, unit, trace);
    }

    public static void runRepeatingAsync(@NotNull Runnable task, long interval, @NotNull TimeUnit unit) {
        ensureLoaded();
        getScheduler().runRepeatingAsync(task, interval, unit, Schedulers.getTrace(task));
    }

    public static ExecutorTrace getTrace(@NotNull Object function) {
        return new ExecutorTrace(function.getClass(), Thread.currentThread().getStackTrace());
    }

    public static void ensureLoaded() {
        if (getScheduler() == null) {
            LOGGER.warn("No scheduler loaded, falling back to default single threaded scheduler");
            setScheduler(SingleExecutorScheduler.create());
        }
    }

    public static void loadDefaultScheduler() {

    }

    public static boolean isLoaded() {
        return getScheduler() != null;
    }

    public static @Nullable Scheduler getScheduler() {
        return scheduler;
    }

    public static void setScheduler(@NotNull Scheduler scheduler) {
        Schedulers.scheduler = scheduler;
    }

}
