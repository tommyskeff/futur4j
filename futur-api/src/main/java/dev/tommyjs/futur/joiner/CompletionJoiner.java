package dev.tommyjs.futur.joiner;

import dev.tommyjs.futur.promise.Promise;
import dev.tommyjs.futur.promise.PromiseCompletion;
import dev.tommyjs.futur.promise.PromiseFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class CompletionJoiner extends PromiseJoiner<Promise<?>, Void, Void, List<PromiseCompletion<?>>> {

    private final ConcurrentResultArray<PromiseCompletion<?>> results;

    public CompletionJoiner(
        @NotNull PromiseFactory factory,
        @NotNull Iterator<Promise<?>> promises,
        int expectedSize, boolean link
    ) {
        super(factory);
        results = new ConcurrentResultArray<>(expectedSize);
        join(promises, link);
    }

    @Override
    protected Void getKey(Promise<?> value) {
        return null;
    }

    @Override
    protected @NotNull Promise<Void> getPromise(Promise<?> value) {
        //noinspection unchecked
        return (Promise<Void>) value;
    }

    @Override
    protected @Nullable Throwable onFinish(int index, Void key, @NotNull PromiseCompletion<Void> res) {
        results.set(index, res);
        return null;
    }

    @Override
    protected List<PromiseCompletion<?>> getResult() {
        return results.toList();
    }

}
