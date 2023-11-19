package dev.tommyjs.futur.reactivestreams;

import dev.tommyjs.futur.promise.Promise;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

public class ReactiveTransformer {

    public static <T> @NotNull Promise<T> wrapPublisher(@NotNull Publisher<T> publisher) {
        SingleAccumulatorSubscriber<T> subscriber = SingleAccumulatorSubscriber.create();
        publisher.subscribe(subscriber);
        return subscriber.getPromise();
    }

}
