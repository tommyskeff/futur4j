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

public abstract class PromiseJoiner<T, Key, Value, Result> {

    private final CompletablePromise<Result> joined;

    protected PromiseJoiner(@NotNull PromiseFactory factory) {
        this.joined = factory.unresolved();
    }

    protected abstract Key getChildKey(@NotNull T value);

    protected abstract @Nullable Promise<Value> getChildPromise(@NotNull T value);

    protected abstract void onChildComplete(int index, Key key, @NotNull PromiseCompletion<Value> completion);

    protected abstract Result getResult();

    protected void join(@NotNull Iterator<@Nullable T> promises) {
        assert !joined.isCompleted();

        AtomicInteger count = new AtomicInteger();

        int i = 0;
        do {
            T value = promises.next();
            if (value == null) {
                continue;
            }

            Promise<Value> promise = getChildPromise(value);
            if (promise == null) {
                continue;
            }

            if (!promise.isCompleted()) {
                PromiseUtil.cancelOnComplete(joined, promise);
            }

            count.incrementAndGet();
            Key key = getChildKey(value);
            int index = i++;

            promise.addAsyncListener(res -> {
                onChildComplete(index, key, res);
                if (res.isError()) {
                    assert res.getException() != null;
                    joined.completeExceptionally(res.getException());
                } else if (count.decrementAndGet() == -1) {
                    joined.complete(getResult());
                }
            });
        } while (promises.hasNext());

        if (count.decrementAndGet() == -1) {
            joined.complete(getResult());
        }
    }

    public @NotNull Promise<Result> joined() {
        return joined;
    }

}
