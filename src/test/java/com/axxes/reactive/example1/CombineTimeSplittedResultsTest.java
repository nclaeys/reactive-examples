package com.axxes.reactive.example1;

import com.axxes.reactive.example1.v3.CombineTimeSplittedResultsV3;
import com.axxes.reactive.example1.v2.CombineTimeSplittedResultsV2;
import com.axxes.reactive.example1.model.PartialResult;
import com.axxes.reactive.example1.model.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toList;

@RunWith(Parameterized.class)
public class CombineTimeSplittedResultsTest {

    private final CombineTimeSplittedResults example;

    @Parameters(name = "{0}")
    public static Collection<Object[]> impls() {
        return asList(
                new Object[]{"Impl 1", new com.axxes.reactive.example1.v1.CombineTimeSplittedResults()},
                new Object[]{"Impl 2", new CombineTimeSplittedResultsV2()},
                new Object[]{"Impl 3", new CombineTimeSplittedResultsV3()}
        );
    }

    public CombineTimeSplittedResultsTest(String name, CombineTimeSplittedResults example) {
        this.example = example;
    }

    @Test
    public void transform_stitchesPartialResultFromFirstAndSecondBatch() {
        int firstBatchLower = 0;
        int sharedBound = 5;
        int secondBatchUpper = 12;
        List<PartialResult> firstBatch = createPartialResults(
                firstBatchLower, sharedBound, singletonList(Tuples.of(2, 5, 1)));
        List<PartialResult> secondBatch = createPartialResults(
                sharedBound, secondBatchUpper, asList(Tuples.of(5, 7, 1), Tuples.of(8, 11, 1)));
        List<CompletableFuture<List<PartialResult>>> completableFutures =
                asList(completedFuture(firstBatch), completedFuture(secondBatch));

        Flux<Result> resultFlux = example.transform(completableFutures);

        StepVerifier.create(resultFlux)
                    .expectNext(new Result(8, 11, 1))
                    .expectNext(new Result(2, 7, 2))
                    .expectComplete()
                .verify();
    }

    @Test
    public void transform_whenNoStitchingIsPossible_returnsAllPartialsAsResults() {
        List<PartialResult> firstBatch =
                asList(
                        createPartialResult(1, 4, 1, 0, 20),
                        createPartialResult(5, 8, 2, 0, 20),
                        createPartialResult(12, 15, 3, 0, 20)
                );

        Flux<Result> resultFlux = example.transform(singletonList(completedFuture(firstBatch)));

        StepVerifier.create(resultFlux)
                .expectNext(new Result(1, 4, 1))
                .expectNext(new Result(5, 8, 2))
                .expectNext(new Result(12, 15, 3))
                .expectComplete()
                .verify();
    }

    @Test
    public void transform_allPartialsAreStitchable_returnsASingleResult() {
        List<PartialResult> batch1 = createPartialResults(0, 10, singletonList(Tuples.of(3, 10, 1)));
        List<PartialResult> batch2 = createPartialResults(10, 20, singletonList(Tuples.of(10, 20, 1)));
        List<PartialResult> batch3 = createPartialResults(20, 30, singletonList(Tuples.of(20, 27, 1)));

        List<CompletableFuture<List<PartialResult>>> futures = asList(
                completedFuture(batch1),
                completedFuture(batch2),
                completedFuture(batch3)
        );
        Flux<Result> resultFlux = example.transform(futures);

        StepVerifier.create(resultFlux)
                .expectNext(new Result(3, 27, 3))
                .expectComplete()
                .verify();
    }

    @Test
    public void transform_filtersOutResultsShorterThan3() {
        List<PartialResult> batch1 = createPartialResults(0, 10, asList(
                Tuples.of(1, 2, 1),
                Tuples.of(3, 4, 1),
                Tuples.of(9, 10, 1)));
        List<PartialResult> batch2 = createPartialResults(10, 20, asList(
                Tuples.of(10, 12, 1),
                Tuples.of(13, 14, 1)
        ));

        Flux<Result> result = example.transform(asList(
                completedFuture(batch1),
                completedFuture(batch2)));

        StepVerifier.create(result)
                .expectNext(new Result(9, 12, 2))
                .expectComplete()
                .verify();
    }

    private List<PartialResult> createPartialResults(int contextLowerBound, int contextUpperBound,
                                                     List<Tuple3<Integer, Integer, Integer>> values) {
        return values
                .stream()
                .map(t -> createPartialResult(t.getT1(), t.getT2(), t.getT3(), contextLowerBound, contextUpperBound))
                .collect(toList());
    }

    private PartialResult createPartialResult(int lowerBound, int upperBound, int value,
                                              int contextLowerBound, int contextUpperBound) {
        if(contextLowerBound == lowerBound || upperBound == contextUpperBound) {
            return new PartialResult(lowerBound, upperBound, value, true);
        }
        return new PartialResult(lowerBound, upperBound, value);
    }
}