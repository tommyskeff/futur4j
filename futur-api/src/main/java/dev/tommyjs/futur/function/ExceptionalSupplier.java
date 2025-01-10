package dev.tommyjs.futur.function;

/**
 * Represents a supplier of results that may throw an exception.
 * This is a functional interface whose functional method is {@link #get()}.
 *
 * @param <T> the type of results supplied by this supplier
 */
@FunctionalInterface
public interface ExceptionalSupplier<T> {

    /**
     * Gets a result, potentially throwing an exception.
     *
     * @return a result
     * @throws Exception if unable to supply a result
     */
    T get() throws Exception;

}