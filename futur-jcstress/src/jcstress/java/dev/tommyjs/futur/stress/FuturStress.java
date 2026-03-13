package dev.tommyjs.futur.stress;

import dev.tommyjs.futur.promise.CompletablePromise;
import dev.tommyjs.futur.promise.PromiseFactory;
import dev.tommyjs.futur.util.ConcurrentResultArray;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;
import org.openjdk.jcstress.infra.results.L_Result;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class FuturStress {

    private static PromiseFactory factory() {
        return PromiseFactory.of(
            LoggerFactory.getLogger(FuturStress.class),
            Executors.newScheduledThreadPool(1)
        );
    }

    @JCStressTest
    @Outcome(id = "1", expect = Expect.ACCEPTABLE, desc = "Listener called exactly once")
    @Outcome(expect = Expect.FORBIDDEN, desc = "Unexpected call count")
    @State
    public static class ListenerVersusComplete {

        final CompletablePromise<Integer> promise;
        final AtomicInteger callCount = new AtomicInteger();

        public ListenerVersusComplete() {
            promise = factory().unresolved();
            promise.addDirectListener(v -> {});
        }

        @Actor
        public void adder() {
            promise.addDirectListener(v -> callCount.incrementAndGet());
        }

        @Actor
        public void completer() {
            promise.complete(42);
        }

        @Arbiter
        public void arbiter(I_Result r) {
            r.r1 = callCount.get();
        }
        
    }
    
    @JCStressTest
    @Outcome(id = "1", expect = Expect.ACCEPTABLE, desc = "Listener called exactly once")
    @Outcome(expect = Expect.FORBIDDEN, desc = "Unexpected call count")
    @State
    public static class ConcurrentComplete {

        final CompletablePromise<Integer> promise;
        final AtomicInteger callCount = new AtomicInteger();

        public ConcurrentComplete() {
            promise = factory().unresolved();
            promise.addDirectListener(v -> callCount.incrementAndGet());
        }

        @Actor
        public void completer1() {
            promise.complete(1);
        }

        @Actor
        public void completer2() {
            promise.complete(2);
        }

        @Arbiter
        public void arbiter(I_Result r) {
            r.r1 = callCount.get();
        }

    }

    @JCStressTest
    @Outcome(id = "2", expect = Expect.ACCEPTABLE, desc = "Both listeners called exactly once")
    @Outcome(expect = Expect.FORBIDDEN, desc = "Unexpected call count")
    @State
    public static class ConcurrentListenerAdders {

        final CompletablePromise<Integer> promise;
        final AtomicInteger callCount = new AtomicInteger();

        public ConcurrentListenerAdders() {
            promise = factory().unresolved();
        }

        @Actor
        public void adder1() {
            promise.addDirectListener(v -> callCount.incrementAndGet());
        }

        @Actor
        public void adder2() {
            promise.addDirectListener(v -> callCount.incrementAndGet());
        }

        @Arbiter
        public void arbiter(I_Result r) {
            promise.complete(42);
            r.r1 = callCount.get();
        }

    }

    @JCStressTest
    @Outcome(id = "42, 1", expect = Expect.ACCEPTABLE, desc = "Write visible")
    @Outcome(id = "null, 0", expect = Expect.ACCEPTABLE, desc = "Neither visible yet")
    @Outcome(id = "42, 0", expect = Expect.ACCEPTABLE, desc = "Element visible but size not yet")
    @Outcome(id = "null, 1", expect = Expect.FORBIDDEN, desc = "Size visible but element not")
    @State
    public static class ArrayWriteRead {

        final ConcurrentResultArray<Integer> array = new ConcurrentResultArray<>(4);

        @Actor
        public void writer() {
            array.set(0, 42);
        }

        @Actor
        public void reader(L_Result r) {
            int size = array.toList().size();
            Integer elem = size > 0 ? array.toList().get(0) : null;
            r.r1 = elem + ", " + size;
        }

    }

}
