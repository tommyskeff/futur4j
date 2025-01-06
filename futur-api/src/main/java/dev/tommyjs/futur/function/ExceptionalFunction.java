package dev.tommyjs.futur.function;

@FunctionalInterface
public interface ExceptionalFunction<K, V> {

    V apply(K value) throws Exception;

}
