package dev.tommyjs.futur;

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
    private final PromiseFactory promises = PromiseFactory.of(logger, executor);

    @Test
    public void testErrors() {
        Promise<?> promise = promises.start().thenSupplyAsync(() -> {
            throw new StackOverflowError();
        });

        try {
            promise.await();
        } catch (CompletionException e) {
            assert e.getCause() instanceof StackOverflowError;
        }
    }

    @Test
    public void testShutdown() {
        executor.close();
        Promise<?> promise = promises.resolve(null).thenSupplyAsync(() -> null);
        try {
            promise.await();
        } catch (CompletionException e) {
            assert e.getCause() instanceof RejectedExecutionException;
        }
    }

    @Test
    public void testCancellation() throws InterruptedException {
        var finished = new AtomicBoolean();
        promises.start().thenRunDelayedAsync(() -> finished.set(true), 50, TimeUnit.MILLISECONDS)
            .thenRunAsync(() -> {})
            .cancel();

        Thread.sleep(100L);
        assert !finished.get();
    }

    @Test
    public void testToFuture() throws InterruptedException {
        assert promises.resolve(true).toFuture().getNow(false);
        assert promises.error(new Exception("Test")).toFuture().isCompletedExceptionally();

        var finished = new AtomicBoolean();
        promises.start()
            .thenRunDelayedAsync(() -> finished.set(true), 50, TimeUnit.MILLISECONDS)
            .toFuture()
            .cancel(true);

        Thread.sleep(100L);
        assert !finished.get();
    }

    @Test
    public void testCombineUtil() throws TimeoutException, ExecutionException, InterruptedException {
        promises.all(
                promises.start().thenRunDelayedAsync(() -> {}, 50, TimeUnit.MILLISECONDS),
                promises.start().thenRunDelayedAsync(() -> {}, 50, TimeUnit.MILLISECONDS)
            )
            .get(100L, TimeUnit.MILLISECONDS);

        promises.allSettled(
                promises.start().thenRunDelayedAsync(() -> {}, 50, TimeUnit.MILLISECONDS),
                promises.start().thenRunDelayedAsync(() -> {}, 50, TimeUnit.MILLISECONDS)
            )
            .get(100L, TimeUnit.MILLISECONDS);

        promises.combine(
                promises.start().thenRunDelayedAsync(() -> {}, 50, TimeUnit.MILLISECONDS),
                promises.start().thenRunDelayedAsync(() -> {}, 50, TimeUnit.MILLISECONDS)
            )
            .get(100L, TimeUnit.MILLISECONDS);

        promises.combine(
                List.of(
                    promises.start().thenRunDelayedAsync(() -> {}, 49, TimeUnit.MILLISECONDS),
                    promises.start().thenRunDelayedAsync(() -> {}, 50, TimeUnit.MILLISECONDS),
                    promises.start().thenRunDelayedAsync(() -> {}, 51, TimeUnit.MILLISECONDS)
                )
            )
            .get(100L, TimeUnit.MILLISECONDS);

        promises.combine(
                Map.of(
                    "a", promises.start().thenRunDelayedAsync(() -> {}, 50, TimeUnit.MILLISECONDS),
                    "b", promises.start().thenRunDelayedAsync(() -> {}, 50, TimeUnit.MILLISECONDS)
                )
            )
            .get(100L, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testCombineUtilPropagation() throws InterruptedException {
        var finished1 = new AtomicBoolean();
        promises.all(
                promises.start().thenRunDelayedAsync(() -> finished1.set(true), 50, TimeUnit.MILLISECONDS),
                promises.start().thenRunDelayedAsync(() -> finished1.set(true), 50, TimeUnit.MILLISECONDS)
            )
            .cancel();

        var finished2 = new AtomicBoolean();
        promises.allSettled(
                promises.start().thenRunDelayedAsync(() -> finished2.set(true), 50, TimeUnit.MILLISECONDS),
                promises.start().thenRunDelayedAsync(() -> finished2.set(true), 50, TimeUnit.MILLISECONDS)
            )
            .cancel();

        var finished3 = new AtomicBoolean();
        promises.combine(
                promises.start().thenRunDelayedAsync(() -> finished3.set(true), 50, TimeUnit.MILLISECONDS),
                promises.start().thenRunDelayedAsync(() -> finished3.set(true), 50, TimeUnit.MILLISECONDS)
            )
            .cancel();

        var finished4 = new AtomicBoolean();
        promises.combine(
                List.of(
                    promises.start().thenRunDelayedAsync(() -> finished4.set(true), 50, TimeUnit.MILLISECONDS),
                    promises.start().thenRunDelayedAsync(() -> finished4.set(true), 50, TimeUnit.MILLISECONDS),
                    promises.start().thenRunDelayedAsync(() -> finished4.set(true), 50, TimeUnit.MILLISECONDS)
                )
            )
            .cancel();

        var finished5 = new AtomicBoolean();
        promises.combine(
                Map.of(
                    "a", promises.start().thenRunDelayedAsync(() -> finished5.set(true), 50, TimeUnit.MILLISECONDS),
                    "b", promises.start().thenRunDelayedAsync(() -> finished5.set(true), 50, TimeUnit.MILLISECONDS)
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
    public void testRace() {
        assert promises.race(
            List.of(
                promises.start().thenSupplyDelayedAsync(() -> true, 150, TimeUnit.MILLISECONDS),
                promises.start().thenSupplyDelayedAsync(() -> false, 200, TimeUnit.MILLISECONDS)
            )
        ).await();
    }

}
