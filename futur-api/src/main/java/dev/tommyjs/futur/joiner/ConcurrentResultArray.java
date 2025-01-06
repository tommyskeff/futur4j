package dev.tommyjs.futur.joiner;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

class ConcurrentResultArray<T> {

    private final AtomicReference<T[]> ref;

    public ConcurrentResultArray(int expectedSize) {
        //noinspection unchecked
        this.ref = new AtomicReference<>((T[]) new Object[expectedSize]);
    }

    public void set(int index, T element) {
        ref.updateAndGet(array -> {
            if (array.length <= index)
                return Arrays.copyOf(array, index + 6);

            array[index] = element;
            return array;
        });
    }

    public @NotNull List<T> toList() {
        return Arrays.asList(ref.get());
    }

}
