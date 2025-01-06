package dev.tommyjs.futur.promise;

import org.jetbrains.annotations.NotNull;

public class PromiseImpl<T, FS, FA> extends AbstractPromise<T, FS, FA> {

    private final @NotNull AbstractPromiseFactory<FS, FA> factory;

    public PromiseImpl(@NotNull AbstractPromiseFactory<FS, FA> factory) {
        this.factory = factory;
    }

    @Override
    public @NotNull AbstractPromiseFactory<FS, FA> getFactory() {
        return factory;
    }

}
