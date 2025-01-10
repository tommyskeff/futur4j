package dev.tommyjs.futur.function;

/**
 * Represents a function that accepts one argument and produces a result,
 * and may throw an exception. This is a functional interface whose functional method is {@link #apply(Object)}.
 *
 * @param <K> the type of the input to the function
 * @param <V> the type of the result of the function
 */
@FunctionalInterface
public interface ExceptionalFunction<K, V> {

    /**
     * Applies this function to the given argument, potentially throwing an exception.
     *
     * @param value the input argument
     * @return the function result
     * @throws Exception if unable to compute a result
     */
    V apply(K value) throws Exception;

}