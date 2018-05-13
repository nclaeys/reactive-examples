package com.axxes.reactive.example4;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CombinePublishersTest {
    @Test
    public void testCompletedFutures() throws Exception {
        CombinePublishers combinePublishers = new CombinePublishers();
        CompletableFuture<Integer> notComplete = new CompletableFuture<>();

        List<CompletableFuture<Integer>> publishers = createCompletableFutures(notComplete);
        notComplete.complete(1);

        StepVerifier.create(combinePublishers.createFrom(publishers))
                    .expectNext(0)
                    .expectNext(1)
                    .expectNext(2)
                    .expectComplete()
                    .verify();
    }

    @Test(timeout = 2000)
    public void testNonCompletedFuture() throws Exception {
        CombinePublishers combinePublishers = new CombinePublishers();
        CompletableFuture<Integer> notComplete = new CompletableFuture<>();
        int delayedCompletingValue = 1;

        List<CompletableFuture<Integer>> publishers = createCompletableFutures(notComplete);

        StepVerifier.create(Flux.merge(combinePublishers.createFrom(publishers)))
                    .expectNext(0)
                    .expectNext(2)
                    .then(() -> notComplete.complete(delayedCompletingValue))
                    .expectNext(delayedCompletingValue)
                    .expectComplete()
                    .verify();
    }

    @Test
    public void testFutureToMono() throws Exception {
        CombinePublishers combinePublishers = new CombinePublishers();
        CompletableFuture<Integer> notComplete = new CompletableFuture<>();
        notComplete.complete(10);

        Mono<Integer> mono = combinePublishers.createFrom(notComplete);

        StepVerifier.create(mono)
                    .expectNext(10)
                    .expectComplete()
                    .verify();
    }

    private List<CompletableFuture<Integer>> createCompletableFutures(CompletableFuture<Integer> notComplete) {
        return IntStream.range(0, 3)
                        .mapToObj(i -> {
                            if (i == 1) {
                                return notComplete;
                            }
                            return CompletableFuture.completedFuture(i);
                        }).collect(Collectors.toList());
    }
}