package dev.tommyjs.futur.joiner;

import dev.tommyjs.futur.promise.Promise;
import dev.tommyjs.futur.promise.PromiseCompletion;
import dev.tommyjs.futur.promise.PromiseFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class VoidJoiner extends PromiseJoiner<Promise<?>, Void, Void, Void> {

    public VoidJoiner(@NotNull PromiseFactory factory, @NotNull Iterator<Promise<?>> promises, boolean link) {
        super(factory);
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
    protected @Nullable Throwable onFinish(int index, Void key, @NotNull PromiseCompletion<Void> completion) {
        return completion.getException();
    }

    @Override
    protected Void getResult() {
        return null;
    }

}
