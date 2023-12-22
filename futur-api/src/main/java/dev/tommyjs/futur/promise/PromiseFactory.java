package dev.tommyjs.futur.promise;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public interface PromiseFactory {

    <T> @NotNull Promise<T> resolve(T value);

    <T> @NotNull Promise<T> unresolved();

    <T> @NotNull Promise<T> error(Throwable error);

    @NotNull Promise<Void> start();

}
