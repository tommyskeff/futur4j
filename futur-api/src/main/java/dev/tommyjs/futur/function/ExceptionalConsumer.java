package dev.tommyjs.futur.function;

@FunctionalInterface
public interface ExceptionalConsumer<T> {

    void accept(T value) throws Exception;

}
