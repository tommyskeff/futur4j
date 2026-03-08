package dev.tommyjs.futur.function;

import org.jetbrains.annotations.NotNull;

public final class FunctionAdapter {

    public static <T, V> @NotNull ExceptionalFunction<T, V> adapt(@NotNull ExceptionalConsumer<T> consumer) {
        return value -> {
            consumer.accept(value);
            return null;
        };
    }

    public static <K, V> @NotNull ExceptionalFunction<K, V> adapt(@NotNull ExceptionalRunnable runnable) {
        return v -> {
            runnable.run();
            return null;
        };
    }

    public static <K, T> @NotNull ExceptionalFunction<K, T> adapt(@NotNull ExceptionalSupplier<T> supplier) {
        return v -> supplier.get();
    }

}
