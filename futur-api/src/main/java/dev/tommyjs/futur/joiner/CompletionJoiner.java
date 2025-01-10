package dev.tommyjs.futur.joiner;

import dev.tommyjs.futur.promise.Promise;
import dev.tommyjs.futur.promise.PromiseCompletion;
import dev.tommyjs.futur.promise.PromiseFactory;
import dev.tommyjs.futur.util.ConcurrentResultArray;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class CompletionJoiner extends PromiseJoiner<Promise<?>, Void, Void, List<PromiseCompletion<?>>> {

    private final ConcurrentResultArray<PromiseCompletion<?>> results;

    public CompletionJoiner(
        @NotNull PromiseFactory factory,
        @NotNull Iterator<Promise<?>> promises,
        int expectedSize
    ) {
        super(factory);
        results = new ConcurrentResultArray<>(expectedSize);
        join(promises);
    }

    @Override
    protected Void getChildKey(Promise<?> value) {
        return null;
    }

    @Override
    protected @NotNull Promise<Void> getChildPromise(Promise<?> value) {
        //noinspection unchecked
        return (Promise<Void>) value;
    }

    @Override
    protected void onChildComplete(int index, Void key, @NotNull PromiseCompletion<Void> res) {
        results.set(index, res);
    }

    @Override
    protected List<PromiseCompletion<?>> getResult() {
        return results.toList();
    }

}
