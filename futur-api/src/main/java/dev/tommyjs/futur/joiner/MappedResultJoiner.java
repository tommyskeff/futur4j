package dev.tommyjs.futur.joiner;

import dev.tommyjs.futur.promise.Promise;
import dev.tommyjs.futur.promise.PromiseCompletion;
import dev.tommyjs.futur.promise.PromiseFactory;
import dev.tommyjs.futur.util.ConcurrentResultArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

public class MappedResultJoiner<K, V> extends PromiseJoiner<Map.Entry<K, Promise<V>>, K, V, Map<K, V>> {

    private final @Nullable BiConsumer<K, Throwable> exceptionHandler;
    private final @NotNull ConcurrentResultArray<Map.Entry<K, V>> results;

    public MappedResultJoiner(
        @NotNull PromiseFactory factory,
        @NotNull Iterator<Map.Entry<K, Promise<V>>> promises,
        @Nullable BiConsumer<K, Throwable> exceptionHandler,
        int expectedSize, boolean link
    ) {
        super(factory);
        this.exceptionHandler = exceptionHandler;
        this.results = new ConcurrentResultArray<>(expectedSize);
        join(promises, link);
    }

    @Override
    protected K getChildKey(Map.Entry<K, Promise<V>> entry) {
        return entry.getKey();
    }

    @Override
    protected @NotNull Promise<V> getChildPromise(Map.Entry<K, Promise<V>> entry) {
        return entry.getValue();
    }

    @Override
    protected @Nullable Throwable onChildComplete(int index, K key, @NotNull PromiseCompletion<V> res) {
        if (res.isError()) {
            if (exceptionHandler == null) {
                return res.getException();
            }

            exceptionHandler.accept(key, res.getException());
        }

        results.set(index, new AbstractMap.SimpleImmutableEntry<>(key, res.getResult()));
        return null;
    }

    @Override
    protected Map<K, V> getResult() {
        List<Map.Entry<K, V>> list = results.toList();
        Map<K, V> map = new HashMap<>(list.size());
        for (Map.Entry<K, V> entry : list) {
            map.put(entry.getKey(), entry.getValue());
        }

        return map;
    }

}