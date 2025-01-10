package dev.tommyjs.futur.function;

/**
 * Represents an operation that accepts a single input argument and returns no result,
 * and may throw an exception. This is a functional interface whose functional method is {@link #accept(Object)}.
 *
 * @param <T> the type of the input to the operation
 */
@FunctionalInterface
public interface ExceptionalConsumer<T> {

    /**
     * Performs this operation on the given argument, potentially throwing an exception.
     *
     * @param value the input argument
     * @throws Exception if unable to compute a result
     */
    void accept(T value) throws Exception;

}