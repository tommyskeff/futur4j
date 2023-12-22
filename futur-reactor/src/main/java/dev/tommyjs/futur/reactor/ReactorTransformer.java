package dev.tommyjs.futur.reactor;

import dev.tommyjs.futur.promise.AbstractPromise;
import dev.tommyjs.futur.promise.PromiseFactory;
import dev.tommyjs.futur.promise.Promise;
import dev.tommyjs.futur.promise.UnpooledPromiseFactory;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ReactorTransformer {

    public static <T> @NotNull Promise<T> wrapMono(@NotNull Mono<T> mono, PromiseFactory factory) {
        Promise<T> promise = factory.unresolved();
        mono.doOnSuccess(promise::complete).doOnError(promise::completeExceptionally).subscribe();
        return promise;
    }

    public static <T> @NotNull Promise<T> wrapMono(@NotNull Mono<T> mono) {
        return wrapMono(mono, UnpooledPromiseFactory.INSTANCE);
    }

    public static <T> @NotNull Promise<@NotNull List<T>> wrapFlux(@NotNull Flux<T> flux, PromiseFactory factory) {
        Promise<List<T>> promise = factory.unresolved();
        AtomicReference<List<T>> out = new AtomicReference<>(new ArrayList<>());

        flux.doOnNext(out.get()::add).subscribe();
        flux.doOnComplete(() -> promise.complete(out.get())).subscribe();
        flux.doOnError(promise::completeExceptionally).subscribe();

        return promise;
    }

    public static <T> @NotNull Promise<@NotNull List<T>> wrapFlux(@NotNull Flux<T> flux) {
        return wrapFlux(flux, UnpooledPromiseFactory.INSTANCE);
    }

}
