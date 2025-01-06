package dev.tommyjs.futur.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentResultArray<T> {

    private static final float RESIZE_FACTOR = 1.2F;

    private final AtomicReference<T[]> ref;

    public ConcurrentResultArray(int expectedSize) {
        //noinspection unchecked
        this.ref = new AtomicReference<>((T[]) new Object[expectedSize]);
    }

    public void set(int index, T element) {
        ref.updateAndGet(array -> {
            if (array.length <= index) {
                array = Arrays.copyOf(array, (int) (array.length * RESIZE_FACTOR));
            }

            array[index] = element;
            return array;
        });
    }

    public @NotNull List<T> toList() {
        return Arrays.asList(ref.get());
    }

}
