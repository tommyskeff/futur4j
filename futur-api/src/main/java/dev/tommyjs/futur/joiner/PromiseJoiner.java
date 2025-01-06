package dev.tommyjs.futur.joiner;

import dev.tommyjs.futur.promise.CompletablePromise;
import dev.tommyjs.futur.promise.Promise;
import dev.tommyjs.futur.promise.PromiseCompletion;
import dev.tommyjs.futur.promise.PromiseFactory;
import dev.tommyjs.futur.util.PromiseUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PromiseJoiner<V, K, T, R> {

    private final CompletablePromise<R> joined;

    protected PromiseJoiner(@NotNull PromiseFactory factory) {
        this.joined = factory.unresolved();
    }

    protected abstract K getChildKey(V value);

    protected abstract @NotNull Promise<T> getChildPromise(V value);

    protected abstract @Nullable Throwable onChildComplete(int index, K key, @NotNull PromiseCompletion<T> completion);

    protected abstract R getResult();

    protected void join(@NotNull Iterator<V> promises, boolean link) {
        AtomicInteger count = new AtomicInteger();

        int i = 0;
        do {
            V value = promises.next();
            Promise<T> p = getChildPromise(value);

            if (link) {
                PromiseUtil.cancelOnComplete(joined, p);
            }

            if (!joined.isCompleted()) {
                count.incrementAndGet();
                K key = getChildKey(value);
                int index = i++;

                p.addAsyncListener(res -> {
                    Throwable e = onChildComplete(index, key, res);
                    if (e != null) {
                        joined.completeExceptionally(e);
                    } else if (count.decrementAndGet() == -1) {
                        joined.complete(getResult());
                    }
                });
            }
        } while (promises.hasNext());

        if (count.decrementAndGet() == -1) {
            joined.complete(getResult());
        }
    }

    public @NotNull Promise<R> joined() {
        return joined;
    }

}
