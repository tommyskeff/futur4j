package dev.tommyjs.futur;

import dev.tommyjs.futur.promise.CompletablePromise;
import dev.tommyjs.futur.promise.CompletedPromise;
import dev.tommyjs.futur.promise.Promise;
import dev.tommyjs.futur.promise.PromiseFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public final class PromiseTests {

    private final Logger logger = LoggerFactory.getLogger(PromiseTests.class);
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(6);
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
    public void testFork() throws InterruptedException {
        var finished = new AtomicBoolean();
        promises.start()
            .thenRunDelayedAsync(() -> finished.set(true), 50, TimeUnit.MILLISECONDS)
            .fork()
            .cancel();

        Thread.sleep(100L);
        assert finished.get();
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

        promises.combineMapped(
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
        promises.combineMapped(
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

    @Test
    public void testOrDefault() {
        CompletablePromise<Integer> promise = promises.unresolved();
        AtomicReference<Integer> res = new AtomicReference<>();
        promise.orDefault(10).thenPopulateReference(res);
        promise.completeExceptionally(new IllegalStateException("Test"));
        assert res.get() == 10;
    }

    @Test
    public void testOrDefaultSupplier() {
        CompletablePromise<Integer> promise = promises.unresolved();
        AtomicReference<Integer> res = new AtomicReference<>();
        promise.orDefault(() -> 10).thenPopulateReference(res);
        promise.completeExceptionally(new IllegalStateException("Test"));
        assert res.get() == 10;
    }

    @Test
    public void testOrDefaultFunction() {
        CompletablePromise<Integer> promise = promises.unresolved();
        AtomicReference<Integer> res = new AtomicReference<>();
        promise.orDefault(e -> {
            assert e instanceof IllegalStateException;
            return 10;
        }).thenPopulateReference(res);
        promise.completeExceptionally(new IllegalStateException("Test"));
        assert res.get() == 10;
    }

    @Test
    public void testOrDefaultError() {
        CompletablePromise<Integer> promise = promises.unresolved();
        AtomicReference<Integer> res = new AtomicReference<>();
        Promise<Integer> promise2 = promise.orDefault(e -> {
            throw new IllegalStateException("Test");
        }).thenPopulateReference(res);
        promise.completeExceptionally(new IllegalStateException("Test"));

        assert res.get() == null;
        assert promise2.getCompletion() != null && promise2.getCompletion().getException() instanceof IllegalStateException;
    }

    @Test
    public void testStream() {
        var res = promises.combine(Stream.of(1, 2, 3).map(promises::resolve)).await();
        assert res.size() == 3;
    }

    @Test
    public void combineMappedTest() {
        var res = promises.combineMapped(List.of(1, 2, 3),
            n -> promises.start().thenSupplyDelayedAsync(() -> n * 2, 50, TimeUnit.MILLISECONDS)
        ).await();

        assert res.size() == 3;
        assert res.get(1) == 2;
        assert res.get(2) == 4;
        assert res.get(3) == 6;
    }

    @Test
    public void testImmediate1() {
        var promise = promises.start().thenSupply(() -> 10);
        assert promise.isCompleted() && promise instanceof CompletedPromise<?,?,?>;
    }

    @Test
    public void testImmediate2() {
        var resolved = promises.resolve(10);
        var promise = promises.start().thenCompose(_ -> resolved);
        assert promise.isCompleted() && promise instanceof CompletedPromise<?,?,?>;
    }

}
