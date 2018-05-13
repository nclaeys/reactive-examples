package com.axxes.reactive.example2;

import com.axxes.reactive.example2.model.KeyedResult;
import com.axxes.reactive.example2.original.CombineResultsByKeyOriginal;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class IllustrateProblemWithOriginalTest {
    @Test
    public void issueWhenThreadpoolIsSmallerThanCompletedResults() throws Exception {
        CombineResultsByKeyOriginal combineResultsByKeyOriginal = new CombineResultsByKeyOriginal(1);
        CompletableFuture<KeyedResult> uncompletedFuture = new CompletableFuture<>();

        Flux<KeyedResult> results = combineResultsByKeyOriginal.combine(
                Arrays.asList(
                        Arrays.asList(uncompletedFuture, generateFuture(2)),
                        Arrays.asList(uncompletedFuture, generateFuture(2))),
                2
                                                                       );
        StepVerifier.create(results)
                    .expectNext(new KeyedResult("key2", 2))
                    .thenCancel()
                    .verify();
    }

    private CompletableFuture<KeyedResult> generateFuture(Integer number) {
        return CompletableFuture.completedFuture(new KeyedResult("key" + number, number));
    }
}
