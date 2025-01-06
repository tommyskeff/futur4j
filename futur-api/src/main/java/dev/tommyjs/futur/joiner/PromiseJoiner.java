package dev.tommyjs.futur.joiner;

import dev.tommyjs.futur.promise.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

abstract class PromiseJoiner<V, K, T, R> {

    private final CompletablePromise<R> joined;

    protected PromiseJoiner(@NotNull PromiseFactory factory) {
        this.joined = factory.unresolved();
    }

    public @NotNull Promise<R> joined() {
        return joined;
    }

    protected abstract K getKey(V value);

    protected abstract @NotNull Promise<T> getPromise(V value);

    protected abstract @Nullable Throwable onFinish(int index, K key, @NotNull PromiseCompletion<T> completion);

    protected abstract R getResult();

    protected void join(@NotNull Iterator<V> promises, boolean link) {
        AtomicBoolean waiting = new AtomicBoolean();
        AtomicInteger count = new AtomicInteger();

        int i = 0;
        do {
            V value = promises.next();
            Promise<T> p = getPromise(value);
            if (link) {
                AbstractPromise.cancelOnFinish(p, joined);
            }

            if (!joined.isCompleted()) {
                count.incrementAndGet();
                K key = getKey(value);
                int index = i++;

                p.addListener((res) -> {
                    Throwable e = onFinish(index, key, res);
                    if (e != null) {
                        joined.completeExceptionally(e);
                    } else if (count.decrementAndGet() == 0 && waiting.get()) {
                        joined.complete(getResult());
                    }
                });
            }
        } while (promises.hasNext());

        count.updateAndGet((v) -> {
            if (v == 0) joined.complete(getResult());
            else waiting.set(true);
            return v;
        });
    }

}
