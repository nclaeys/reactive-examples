package com.axxes.reactive.example1.v2;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

public final class FuturePublisher<T> implements Publisher<T> {

    private final List<CompletableFuture<T>> futures;

    public FuturePublisher(List<CompletableFuture<T>> futures) {
        this.futures = futures;
    }

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        Flux.merge(adaptFuturesToReactiveStreams()).subscribe(subscriber);
    }

    private List<Mono<T>> adaptFuturesToReactiveStreams() {
        return futures.stream()
                      .filter(future -> !future.isCompletedExceptionally())
                      .map(Mono::fromFuture).collect(toList());
    }
}
