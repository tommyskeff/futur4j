package dev.tommyjs.futur.reactivestreams;

import dev.tommyjs.futur.promise.Promise;
import dev.tommyjs.futur.promise.PromiseFactory;
import dev.tommyjs.futur.promise.UnpooledPromiseFactory;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

public class ReactiveTransformer {

    public static <T> @NotNull Promise<T> wrapPublisher(@NotNull Publisher<T> publisher, PromiseFactory factory) {
        SingleAccumulatorSubscriber<T> subscriber = SingleAccumulatorSubscriber.create(factory);
        publisher.subscribe(subscriber);
        return subscriber.getPromise();
    }

    public static <T> @NotNull Promise<T> wrapPublisher(@NotNull Publisher<T> publisher) {
        return wrapPublisher(publisher, UnpooledPromiseFactory.INSTANCE);
    }

}
