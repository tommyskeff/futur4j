package dev.tommyjs.futur.reactivestreams;

import dev.tommyjs.futur.promise.AbstractPromise;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

public class ReactiveTransformer {

    public static <T> @NotNull AbstractPromise<T> wrapPublisher(@NotNull Publisher<T> publisher) {
        SingleAccumulatorSubscriber<T> subscriber = SingleAccumulatorSubscriber.create();
        publisher.subscribe(subscriber);
        return subscriber.getPromise();
    }

}
