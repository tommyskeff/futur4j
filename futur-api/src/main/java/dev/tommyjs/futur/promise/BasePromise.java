package dev.tommyjs.futur.promise;

import dev.tommyjs.futur.executor.PromiseScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

@SuppressWarnings({"FieldMayBeFinal"})
public abstract class BasePromise<T> extends AbstractPromise<T> implements CompletablePromise<T> {

    private static final VarHandle COMPLETION_HANDLE;
    private static final VarHandle LISTENERS_HANDLE;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            COMPLETION_HANDLE = lookup.findVarHandle(BasePromise.class, "completion", PromiseCompletion.class);
            LISTENERS_HANDLE = lookup.findVarHandle(BasePromise.class, "listeners", Collection.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final Sync sync;

    private volatile PromiseCompletion<T> completion;

    @SuppressWarnings("FieldMayBeFinal")
    private volatile Collection<PromiseListener<T>> listeners;

    @SuppressWarnings("unchecked")
    public BasePromise() {
        this.sync = new Sync();
        this.completion = null;
        this.listeners = Collections.EMPTY_LIST;
    }

    protected void handleCompletion(@NotNull PromiseCompletion<T> cmp) {
        if (!COMPLETION_HANDLE.compareAndSet(this, null, cmp)) {
            return;
        }

        sync.releaseShared(1);
        callListeners(cmp);
    }

    protected <F> Promise<T> completeExceptionallyDelayed(Throwable e, long delay, TimeUnit unit,
                                                          PromiseScheduler<F> scheduler) {
        runCompleter(this, () -> {
            F future = scheduler.schedule(() -> completeExceptionally(e), delay, unit);
            addDirectListener(_ -> scheduler.cancel(future));
        });

        return this;
    }

    @SuppressWarnings("unchecked")
    protected void callListeners(@NotNull PromiseCompletion<T> cmp) {
        var iter = ((Iterable<PromiseListener<T>>) LISTENERS_HANDLE.getAndSet(this, null)).iterator();
        try {
            while (iter.hasNext()) {
                callListener(iter.next(), cmp);
            }
        } finally {
            iter.forEachRemaining(v -> callListenerAsyncLastResort(v, cmp));
        }
    }

    @Override
    protected @NotNull Promise<T> addAnyListener(@NotNull PromiseListener<T> listener) {
        Collection<PromiseListener<T>> prev = listeners, next = null;
        for (boolean haveNext = false; ; ) {
            if (!haveNext) {
                next = prev == Collections.EMPTY_LIST ? new ConcurrentLinkedQueue<>() : prev;
                if (next != null) {
                    next.add(listener);
                }
            }

            if (LISTENERS_HANDLE.weakCompareAndSet(this, prev, next)) {
                break;
            }

            haveNext = (prev == (prev = listeners));
        }

        if (next == null) {
            callListener(listener, Objects.requireNonNull(getCompletion()));
        }

        return this;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        if (!isCompleted()) {
            sync.acquireSharedInterruptibly(1);
        }

        return joinCompletionChecked();
    }

    @Override
    public T get(long time, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!isCompleted()) {
            boolean success = sync.tryAcquireSharedNanos(1, unit.toNanos(time));
            if (!success) {
                throw new TimeoutException("Promise stopped waiting after " + time + " " + unit);
            }
        }

        return joinCompletionChecked();
    }

    @Override
    public T await() {
        if (!isCompleted()) {
            try {
                sync.acquireSharedInterruptibly(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return joinCompletionUnchecked();
    }

    @Override
    public T getNow() {
        return joinCompletionUnchecked();
    }

    @Override
    public @NotNull Promise<T> timeout(long time, @NotNull TimeUnit unit) {
        Exception e = new CancellationException(
            "Promise timed out after " + time + " " + unit.toString().toLowerCase());
        return completeExceptionallyDelayed(e, time, unit, PromiseScheduler.getDefault());
    }

    @Override
    public @NotNull Promise<T> maxWaitTime(long time, @NotNull TimeUnit unit) {
        Exception e = new TimeoutException(
            "Promise stopped waiting after " + time + " " + unit.toString().toLowerCase());
        return completeExceptionallyDelayed(e, time, unit, PromiseScheduler.getDefault());
    }

    @Override
    public void cancel(@NotNull CancellationException e) {
        completeExceptionally(e);
    }

    @Override
    public void complete(@Nullable T result) {
        handleCompletion(new PromiseCompletion<>(result));
    }

    @Override
    public void completeExceptionally(@NotNull Throwable result) {
        handleCompletion(new PromiseCompletion<>(result));
    }

    @Override
    public boolean isCompleted() {
        return completion != null;
    }

    @Override
    public @Nullable PromiseCompletion<T> getCompletion() {
        return completion;
    }

    @Override
    public @NotNull CompletableFuture<T> toFuture() {
        return useCompletion(() -> {
            CompletableFuture<T> future = new CompletableFuture<>();
            addDirectListener(future::complete, future::completeExceptionally);
            future.whenComplete((result, error) -> {
                if (error == null) {
                    complete(result);
                } else {
                    completeExceptionally(error);
                }
            });

            return future;
        }, CompletableFuture::completedFuture, CompletableFuture::failedFuture);
    }

    @Override
    public @NotNull CompletionStage<T> toCompletionStage() {
        return useCompletion(() -> {
            CompletableFuture<T> future = new CompletableFuture<>();
            addDirectListener(future::complete, future::completeExceptionally);
            return future;
        }, CompletableFuture::completedStage, CompletableFuture::failedStage);
    }

    private static final class Sync extends AbstractQueuedSynchronizer {

        private Sync() {
            setState(1);
        }

        @Override
        protected int tryAcquireShared(int acquires) {
            return getState() == 0 ? 1 : -1;
        }

        @Override
        protected boolean tryReleaseShared(int releases) {
            int c1, c2;
            do {
                c1 = getState();
                if (c1 == 0) {
                    return false;
                }

                c2 = c1 - 1;
            } while (!compareAndSetState(c1, c2));

            return c2 == 0;
        }

    }

}
