package dev.tommyjs.futur.function;

@FunctionalInterface
public interface ExceptionalSupplier<T> {

    T get() throws Throwable;

}
