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

    private static final class ListenerNode<T> {
        final PromiseListener<T> listener;
        ListenerNode<T> next;
        ListenerNode(PromiseListener<T> listener) { this.listener = listener; }
    }

    @SuppressWarnings("rawtypes")
    private static final ListenerNode COMPLETED_NODE = new ListenerNode<>(null);

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            COMPLETION_HANDLE = lookup.findVarHandle(BasePromise.class, "completion", PromiseCompletion.class);
            LISTENERS_HANDLE = lookup.findVarHandle(BasePromise.class, "listeners", ListenerNode.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final Sync sync;

    private volatile PromiseCompletion<T> completion;

    private volatile ListenerNode<T> listeners;

    public BasePromise() {
        this.sync = new Sync();
        this.completion = null;
        this.listeners = null;
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
            addDirectListener(v -> scheduler.cancel(future));
        });

        return this;
    }

    @SuppressWarnings("unchecked")
    protected void callListeners(@NotNull PromiseCompletion<T> cmp) {
        ListenerNode<T> node = (ListenerNode<T>) LISTENERS_HANDLE.getAndSet(this, COMPLETED_NODE);
        if (node == null || node == COMPLETED_NODE) {
            return;
        }

        ListenerNode<T> prev = null;
        while (node != null) {
            ListenerNode<T> next = node.next;
            node.next = prev;
            prev = node;
            node = next;
        }

        ListenerNode<T> curr = prev;
        try {
            while (curr != null) {
                callListener(curr.listener, cmp);
                curr = curr.next;
            }
        } finally {
            while (curr != null) {
                callListenerAsyncLastResort(curr.listener, cmp);
                curr = curr.next;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected @NotNull Promise<T> addAnyListener(@NotNull PromiseListener<T> listener) {
        ListenerNode<T> node = new ListenerNode<>(listener);
        ListenerNode<T> prev;
        do {
            prev = listeners;
            if (prev == COMPLETED_NODE) {
                callListener(listener, Objects.requireNonNull(getCompletion()));
                return this;
            }
            node.next = prev;
        } while (!LISTENERS_HANDLE.weakCompareAndSet(this, prev, node));

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
