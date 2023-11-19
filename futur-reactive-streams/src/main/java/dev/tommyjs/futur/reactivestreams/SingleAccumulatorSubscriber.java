package dev.tommyjs.futur.reactivestreams;

import dev.tommyjs.futur.promise.Promise;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class SingleAccumulatorSubscriber<T> implements Subscriber<T> {

    private final Promise<T> promise;

    public SingleAccumulatorSubscriber(Promise<T> promise) {
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

    public Promise<T> getPromise() {
        return promise;
    }

    public static <T> SingleAccumulatorSubscriber<T> create(Promise<T> promise) {
        return new SingleAccumulatorSubscriber<>(promise);
    }

    public static <T> SingleAccumulatorSubscriber<T> create() {
        return create(new Promise<>());
    }

}
