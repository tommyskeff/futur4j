package dev.tommyjs.futur.joiner;

import dev.tommyjs.futur.promise.Promise;
import dev.tommyjs.futur.promise.PromiseCompletion;
import dev.tommyjs.futur.promise.PromiseFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class VoidJoiner extends PromiseJoiner<Promise<?>, Void, Void, Void> {

    public VoidJoiner(@NotNull PromiseFactory factory, @NotNull Iterator<Promise<?>> promises) {
        super(factory);
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
    protected void onChildComplete(int index, Void key, @NotNull PromiseCompletion<Void> completion) {

    }

    @Override
    protected Void getResult() {
        return null;
    }

}
