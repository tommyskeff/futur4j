package dev.tommyjs.futur.joiner;

import dev.tommyjs.futur.promise.Promise;
import dev.tommyjs.futur.promise.PromiseCompletion;
import dev.tommyjs.futur.promise.PromiseFactory;
import dev.tommyjs.futur.util.ConcurrentResultArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

public class ResultJoiner<T> extends PromiseJoiner<Promise<T>, Void, T, List<T>> {

    private final @Nullable BiConsumer<Integer, Throwable> exceptionHandler;
    private final ConcurrentResultArray<T> results;

    public ResultJoiner(
        @NotNull PromiseFactory factory,
        @NotNull Iterator<Promise<T>> promises,
        @Nullable BiConsumer<Integer, Throwable> exceptionHandler,
        int expectedSize, boolean link
    ) {
        super(factory);
        this.exceptionHandler = exceptionHandler;
        this.results = new ConcurrentResultArray<>(expectedSize);
        join(promises, link);
    }

    @Override
    protected Void getChildKey(Promise<T> value) {
        return null;
    }

    @Override
    protected @NotNull Promise<T> getChildPromise(Promise<T> value) {
        return value;
    }

    @Override
    protected @Nullable Throwable onChildComplete(int index, Void key, @NotNull PromiseCompletion<T> res) {
        if (res.isError()) {
            if (exceptionHandler == null) {
                return res.getException();
            }

            exceptionHandler.accept(index, res.getException());
        }

        results.set(index, res.getResult());
        return null;
    }

    @Override
    protected List<T> getResult() {
        return results.toList();
    }

}
