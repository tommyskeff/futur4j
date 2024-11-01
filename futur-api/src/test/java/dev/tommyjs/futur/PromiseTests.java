package dev.tommyjs.futur;

import dev.tommyjs.futur.executor.SinglePoolExecutor;
import dev.tommyjs.futur.impl.SimplePromiseFactory;
import dev.tommyjs.futur.promise.Promise;
import dev.tommyjs.futur.promise.PromiseFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PromiseTests {

    private final Logger logger = LoggerFactory.getLogger(PromiseTests.class);
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);
    private final PromiseFactory pfac = new SimplePromiseFactory<>(new SinglePoolExecutor(executor), logger);

    @Test
    public void testShutdown() {
        executor.shutdown();
        Promise<?> promise = pfac.resolve(null).thenSupplyAsync(() -> null);
        try {
            promise.await();
        } catch (RuntimeException e) {
            assert e.getCause() instanceof RejectedExecutionException;
        }
    }

    @Test
    public void testErrorCancellation() throws InterruptedException {
        var finished = new AtomicBoolean();
        pfac.start()
            .thenRunDelayedAsync(() -> finished.set(true), 50, TimeUnit.MILLISECONDS)
            .thenRunAsync(() -> {})
            .cancel();

        Thread.sleep(100L);
        assert !finished.get();
    }

    @Test
    public void testToFuture() throws InterruptedException {
        assert pfac.resolve(true).toFuture().getNow(false);
        assert pfac.error(new Exception("Test")).toFuture().isCompletedExceptionally();

        var finished = new AtomicBoolean();
        pfac.start()
            .thenRunDelayedAsync(() -> finished.set(true), 50, TimeUnit.MILLISECONDS)
            .toFuture()
            .cancel(true);

        Thread.sleep(100L);
        assert !finished.get();
    }

    @Test
    public void testCombineUtil() throws TimeoutException {
        pfac.all(
                pfac.start().thenRunDelayedAsync(() -> {}, 50, TimeUnit.MILLISECONDS),
                pfac.start().thenRunDelayedAsync(() -> {}, 50, TimeUnit.MILLISECONDS)
            )
            .join(100L);

        pfac.allSettled(
                pfac.start().thenRunDelayedAsync(() -> {}, 50, TimeUnit.MILLISECONDS),
                pfac.start().thenRunDelayedAsync(() -> {}, 50, TimeUnit.MILLISECONDS)
            )
            .join(100L);

        pfac.combine(
                pfac.start().thenRunDelayedAsync(() -> {}, 50, TimeUnit.MILLISECONDS),
                pfac.start().thenRunDelayedAsync(() -> {}, 50, TimeUnit.MILLISECONDS)
            )
            .join(100L);

        pfac.combine(
                List.of(
                    pfac.start().thenRunDelayedAsync(() -> {}, 49, TimeUnit.MILLISECONDS),
                    pfac.start().thenRunDelayedAsync(() -> {}, 50, TimeUnit.MILLISECONDS),
                    pfac.start().thenRunDelayedAsync(() -> {}, 51, TimeUnit.MILLISECONDS)
                )
            )
            .join(100L);

        pfac.combine(
                Map.of(
                    "a", pfac.start().thenRunDelayedAsync(() -> {}, 50, TimeUnit.MILLISECONDS),
                    "b", pfac.start().thenRunDelayedAsync(() -> {}, 50, TimeUnit.MILLISECONDS)
                )
            )
            .join(100L);
    }

    @Test
    public void testCombineUtilPropagation() throws InterruptedException {
        var finished1 = new AtomicBoolean();
        pfac.all(
                true,
                pfac.start().thenRunDelayedAsync(() -> finished1.set(true), 50, TimeUnit.MILLISECONDS),
                pfac.start().thenRunDelayedAsync(() -> finished1.set(true), 50, TimeUnit.MILLISECONDS)
            )
            .cancel();

        var finished2 = new AtomicBoolean();
        pfac.allSettled(
                true,
                pfac.start().thenRunDelayedAsync(() -> finished2.set(true), 50, TimeUnit.MILLISECONDS),
                pfac.start().thenRunDelayedAsync(() -> finished2.set(true), 50, TimeUnit.MILLISECONDS)
            )
            .cancel();

        var finished3 = new AtomicBoolean();
        pfac.combine(
                true,
                pfac.start().thenRunDelayedAsync(() -> finished3.set(true), 50, TimeUnit.MILLISECONDS),
                pfac.start().thenRunDelayedAsync(() -> finished3.set(true), 50, TimeUnit.MILLISECONDS)
            )
            .cancel();

        var finished4 = new AtomicBoolean();
        pfac.combine(
                true,
                List.of(
                    pfac.start().thenRunDelayedAsync(() -> finished4.set(true), 50, TimeUnit.MILLISECONDS),
                    pfac.start().thenRunDelayedAsync(() -> finished4.set(true), 50, TimeUnit.MILLISECONDS),
                    pfac.start().thenRunDelayedAsync(() -> finished4.set(true), 50, TimeUnit.MILLISECONDS)
                )
            )
            .cancel();

        var finished5 = new AtomicBoolean();
        pfac.combine(
                true,
                Map.of(
                    "a", pfac.start().thenRunDelayedAsync(() -> finished5.set(true), 50, TimeUnit.MILLISECONDS),
                    "b", pfac.start().thenRunDelayedAsync(() -> finished5.set(true), 50, TimeUnit.MILLISECONDS)
                )
            )
            .cancel();

        Thread.sleep(100L);
        assert !finished1.get();
        assert !finished2.get();
        assert !finished3.get();
        assert !finished4.get();
        assert !finished5.get();
    }

    @Test
    public void testRace() throws TimeoutException {
        assert pfac.race(
            List.of(
                pfac.start().thenSupplyDelayedAsync(() -> true, 150, TimeUnit.MILLISECONDS),
                pfac.start().thenSupplyDelayedAsync(() -> false, 200, TimeUnit.MILLISECONDS)
            )
        ).join(300L);
    }

}
