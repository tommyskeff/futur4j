package dev.tommyjs.futur.joiner;

import dev.tommyjs.futur.promise.Promise;
import dev.tommyjs.futur.promise.PromiseCompletion;
import dev.tommyjs.futur.promise.PromiseFactory;
import dev.tommyjs.futur.util.ConcurrentResultArray;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class ResultJoiner<T> extends PromiseJoiner<Promise<T>, Void, T, List<T>> {

    private final ConcurrentResultArray<T> results;

    public ResultJoiner(
        @NotNull PromiseFactory factory,
        @NotNull Iterator<Promise<T>> promises,
        int expectedSize, boolean link
    ) {
        super(factory);
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
    protected void onChildComplete(int index, Void key, @NotNull PromiseCompletion<T> res) {
        results.set(index, res.getResult());
    }

    @Override
    protected List<T> getResult() {
        return results.toList();
    }

}
