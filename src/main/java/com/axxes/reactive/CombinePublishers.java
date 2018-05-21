package com.axxes.reactive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CombinePublishers {


    public Mono<Integer> createFrom(CompletableFuture<Integer> future) {
        return Mono.fromFuture(future);
    }

    public Flux<Integer> createFrom(List<CompletableFuture<Integer>> futures) {
        List<Mono<Integer>> publishers = futures.stream()
                                             .map(Mono::fromFuture)
                                             .collect(Collectors.toList());
        return Flux.merge(publishers);
    }
}
