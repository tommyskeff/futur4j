package dev.tommyjs.futur.function;

public interface ExceptionalSupplier<T> {

    T get() throws Exception;

}
