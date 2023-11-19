package dev.tommyjs.futur.function;

public interface ExceptionalFunction<K, V> {

    V apply(K value) throws Exception;

}
