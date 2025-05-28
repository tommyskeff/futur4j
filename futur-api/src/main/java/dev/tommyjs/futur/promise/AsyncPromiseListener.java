package dev.tommyjs.futur.promise;

/**
 * A listener for a {@link Promise} that is called when the promise is resolved. This listener is
 * executed asynchronously by the {@link PromiseFactory} that created the completed promise.
 */
public interface AsyncPromiseListener<T> extends PromiseListener<T> {
}
