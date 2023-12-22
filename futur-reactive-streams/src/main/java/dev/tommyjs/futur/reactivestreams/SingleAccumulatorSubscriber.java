package dev.tommyjs.futur.reactivestreams;

import dev.tommyjs.futur.promise.AbstractPromise;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class SingleAccumulatorSubscriber<T> implements Subscriber<T> {

    private final AbstractPromise<T> promise;

    public SingleAccumulatorSubscriber(AbstractPromise<T> promise) {
        this.promise = promise;
    }

    @Override
    public void onSubscribe(Subscription s) {
        s.request(1);
    }

    @Override
    public void onNext(T t) {
        promise.complete(t);
    }

    @Override
    public void onError(Throwable t) {
        promise.completeExceptionally(t);
    }

    @Override
    public void onComplete() {
        // ignore
    }

    public AbstractPromise<T> getPromise() {
        return promise;
    }

    public static <T> SingleAccumulatorSubscriber<T> create(AbstractPromise<T> promise) {
        return new SingleAccumulatorSubscriber<>(promise);
    }

    public static <T> SingleAccumulatorSubscriber<T> create() {
        return create(new AbstractPromise<>());
    }

}
