package dev.tommyjs.futur.joiner;

import dev.tommyjs.futur.promise.Promise;
import dev.tommyjs.futur.promise.PromiseCompletion;
import dev.tommyjs.futur.promise.PromiseFactory;
import dev.tommyjs.futur.util.ConcurrentResultArray;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MappedResultJoiner<K, V> extends PromiseJoiner<Map.Entry<K, Promise<V>>, K, V, Map<K, V>> {

    private final @NotNull ConcurrentResultArray<Map.Entry<K, V>> results;

    public MappedResultJoiner(
        @NotNull PromiseFactory factory,
        @NotNull Iterator<Map.Entry<K, Promise<V>>> promises,
        int expectedSize
    ) {
        super(factory);
        this.results = new ConcurrentResultArray<>(expectedSize);
        join(promises);
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
    protected void onChildComplete(int index, K key, @NotNull PromiseCompletion<V> res) {
        results.set(index, new AbstractMap.SimpleImmutableEntry<>(key, res.getResult()));
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