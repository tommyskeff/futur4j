package dev.tommyjs.futur.reactor;

import dev.tommyjs.futur.promise.Promise;
import dev.tommyjs.futur.promise.PromiseFactory;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public class ReactorTransformer {

    public static <T> @NotNull Promise<T> wrapMono(@NotNull Mono<T> mono, PromiseFactory factory) {
        Promise<T> promise = factory.unresolved();
        mono.subscribe(promise::complete, promise::completeExceptionally);
        return promise;
    }

}
