package dev.tommyjs.futur.function;

public interface ExceptionalConsumer<T> {

    void accept(T value) throws Exception;

}
