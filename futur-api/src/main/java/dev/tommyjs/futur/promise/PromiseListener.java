package dev.tommyjs.futur.promise;

import org.jetbrains.annotations.NotNull;

public interface PromiseListener<T> {

    void handle(@NotNull PromiseCompletion<T> ctx);

}
