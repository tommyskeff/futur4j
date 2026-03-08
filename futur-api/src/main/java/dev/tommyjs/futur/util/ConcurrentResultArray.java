package dev.tommyjs.futur.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentResultArray<T> {

    private final T[] expected;
    private final AtomicInteger size;
    private T @Nullable [] unexpected;

    public ConcurrentResultArray(int expectedSize) {
        //noinspection unchecked
        this.expected = (T[]) new Object[expectedSize];
        this.size = new AtomicInteger(0);
    }

    public void set(int index, T element) {
        size.updateAndGet(v -> Math.max(v, index + 1));
        if (index < expected.length) {
            expected[index] = element;
            return;
        }

        int altIndex = index - expected.length;
        synchronized (this) {
            if (unexpected == null) {
                //noinspection unchecked
                unexpected = (T[]) new Object[Math.max(10, altIndex + 1)];
            } else if (altIndex >= unexpected.length) {
                int minGrowth = altIndex - unexpected.length + 1;
                int prefGrowth = Math.max(1, unexpected.length >> 1);
                int newLength = unexpected.length + Math.max(minGrowth, prefGrowth);
                unexpected = Arrays.copyOf(unexpected, newLength);
            }

            unexpected[altIndex] = element;
        }
    }

    public @NotNull List<T> toList() {
        int size = this.size.get();
        T[] result = Arrays.copyOf(expected, size);
        if (size <= expected.length) {
            return Arrays.asList(result);
        }

        System.arraycopy(Objects.requireNonNull(unexpected), 0,
            result, expected.length, size - expected.length);
        return Arrays.asList(result);
    }

}
