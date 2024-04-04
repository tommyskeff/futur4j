package dev.tommyjs.futur;

import dev.tommyjs.futur.executor.PromiseExecutor;
import dev.tommyjs.futur.executor.SinglePoolExecutor;
import dev.tommyjs.futur.impl.SimplePromiseFactory;
import dev.tommyjs.futur.promise.PromiseFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PromiseTests {

    private final Logger logger = LoggerFactory.getLogger(PromiseTests.class);
    private final PromiseExecutor<ScheduledFuture<?>> executor = SinglePoolExecutor.create(1);
    private final PromiseFactory pfac = new SimplePromiseFactory<>(executor, logger);

    @Test
    void testMono() {
        Exception value = new Exception("Test Error");

        var error = pfac.wrap(Mono.error(value));
        assert Objects.requireNonNull(error.getCompletion()).isError();
        assert error.getCompletion().getException() == value;

        var resolved = pfac.wrap(Mono.just(value));
        assert !Objects.requireNonNull(resolved.getCompletion()).isError();
        assert resolved.getCompletion().getResult() == value;
    }

    @Test
    void testErrorCancellation() throws InterruptedException {
        var finish = new AtomicBoolean();
        pfac.start()
            .thenRunDelayedAsync(() -> finish.set(true), 50, TimeUnit.MILLISECONDS)
            .thenRunAsync(() -> {})
            .cancel();

        Thread.sleep(100L);
        assert !finish.get();
    }

}
