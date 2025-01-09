package dev.tommyjs.futur.promise;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

@SuppressWarnings({"FieldMayBeFinal"})
public abstract class BasePromise<T, FS, FA> extends AbstractPromise<T, FS, FA> implements CompletablePromise<T> {

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

    protected T joinCompletion() throws ExecutionException {
        PromiseCompletion<T> completion = Objects.requireNonNull(getCompletion());
        if (completion.isSuccess()) return completion.getResult();
        throw new ExecutionException(completion.getException());
    }

    protected void handleCompletion(@NotNull PromiseCompletion<T> cmp) {
        if (!COMPLETION_HANDLE.compareAndSet(this, null, cmp)) return;
        sync.releaseShared(1);
        callListeners(cmp);
    }

    protected Promise<T> completeExceptionallyDelayed(Throwable e, long delay, TimeUnit unit) {
        runCompleter(this, () -> {
            FA future = getFactory().getAsyncExecutor().run(() -> completeExceptionally(e), delay, unit);
            addDirectListener(_ -> getFactory().getAsyncExecutor().cancel(future));
        });

        return this;
    }

    @SuppressWarnings("unchecked")
    protected void callListeners(@NotNull PromiseCompletion<T> cmp) {
        Iterator<PromiseListener<T>> iter = ((Iterable<PromiseListener<T>>) LISTENERS_HANDLE.getAndSet(this, null)).iterator();
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
                if (next != null) next.add(listener);
            }

            if (LISTENERS_HANDLE.weakCompareAndSet(this, prev, next))
                break;

            haveNext = (prev == (prev = listeners));
        }

        if (next == null) {
            callListener(listener, Objects.requireNonNull(getCompletion()));
        }

        return this;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        sync.acquireSharedInterruptibly(1);
        return joinCompletion();
    }

    @Override
    public T get(long time, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireSharedNanos(1, unit.toNanos(time));
        if (!success) {
            throw new TimeoutException("Promise stopped waiting after " + time + " " + unit);
        }

        return joinCompletion();
    }

    @Override
    public T await() {
        try {
            sync.acquireSharedInterruptibly(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        PromiseCompletion<T> completion = Objects.requireNonNull(getCompletion());
        if (completion.isSuccess()) return completion.getResult();
        throw new CompletionException(completion.getException());
    }

    @Override
    public @NotNull Promise<T> timeout(long time, @NotNull TimeUnit unit) {
        Exception e = new CancellationException("Promise timed out after " + time + " " + unit.toString().toLowerCase());
        return completeExceptionallyDelayed(e, time, unit);
    }

    @Override
    public @NotNull Promise<T> maxWaitTime(long time, @NotNull TimeUnit unit) {
        Exception e = new TimeoutException("Promise stopped waiting after " + time + " " + unit.toString().toLowerCase());
        return completeExceptionallyDelayed(e, time, unit);
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
