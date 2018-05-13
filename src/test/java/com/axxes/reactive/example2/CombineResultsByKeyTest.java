package com.axxes.reactive.example2;

import com.axxes.reactive.example2.model.KeyedResult;
import com.axxes.reactive.example2.v1.CombineResultsByKeyV1;
import com.axxes.reactive.example2.v2.CombineResultsByKey2;
import org.hamcrest.core.IsInstanceOf;
import org.hamcrest.core.IsNot;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeThat;

@RunWith(Parameterized.class)
public class CombineResultsByKeyTest {

    private final CombineResultsByKey example;

    @Parameters(name = "{0}")
    public static Collection<Object[]> parameters() {
        return asList(
                new Object[]{"Impl1", new CombineResultsByKeyV1()},
                new Object[]{"Impl2", new CombineResultsByKey2()}
        );
    }

    public CombineResultsByKeyTest(String name, CombineResultsByKey example) {
        this.example = example;
    }

    @Test
    public void resultsWhenBothFluxesHaveResults_calculateAverages() {
        Flux<KeyedResult> resultFlux = example
                .combine(asList(
                        generateList(1, 2),
                        generateList(1, 2)
                ), 2);

        StepVerifier.create(resultFlux)
                .expectNext(new KeyedResult("key1", 1))
                .expectNext(new KeyedResult("key2", 2))
                .expectComplete()
                .verify();
    }

    @Test
    public void resultsWhenOneLayerDoesNotComplete_otherResultsAreReturned() {
        CompletableFuture<List<KeyedResult>> uncompleted = new CompletableFuture<>();
        Flux<KeyedResult> resultFlux = example
                .combine(asList(
                        uncompleted,
                        generateList(1, 2),
                        generateList(1, 1)), 1);

        StepVerifier.create(resultFlux)
                    .expectNext(new KeyedResult("key1", 1))
                    .thenCancel()
                    .verify();
    }

    @Test
    public void resultsWhenNotAllKeysAreOnBothFluxes_missingResultsAreSendAtTheEnd() {
        Flux<KeyedResult> resultFlux = example
                .combine(asList(
                        generateList(1, 3),
                        generateList(2, 3)
                ), asList(
                                generateList(1, 3),
                                generateList(2, 3)
                                 ).size());

        List<KeyedResult> expectedResults = asList(
                new KeyedResult("key2", 2),
                new KeyedResult("key3", 3),
                new KeyedResult("key1", 0.5f)
                                                  );

        StepVerifier.create(resultFlux)
                .thenConsumeWhile(expectedResults::contains)
                .expectComplete()
                .verify();
    }

    @Test
    public void returnsLargeAmountOfResults() {
        Flux<KeyedResult> resultFlux = example
                .combine(asList(
                        generateList(1, 100),
                        generateList(2, 100)
                ), asList(
                                generateList(1, 100),
                                generateList(2, 100)
                                 ).size());

        List<KeyedResult> expectedResults = IntStream
                .range(2, 101)
                .mapToObj(i -> new KeyedResult(format("key%s", i), i))
                .collect(toList());
        expectedResults.add(new KeyedResult("key1", 0.5f));

        StepVerifier.create(resultFlux)
                .thenConsumeWhile(expectedResults::contains)
                .expectComplete()
                .verify();
    }

    @Test
    public void aLotOfLayersAndALotOfKeysInRandomOrder() {
        assumeThat(example, new IsNot(new IsInstanceOf(CombineResultsByKeyV1.class)));
        int maximumMax = 1001;
        int lowerMax = 1000;
        int minimalStart = 1;
        int otherStart = 3;
        Flux<KeyedResult> result = example.combine(asList(
                generateFluxWithKeysInRandomOrderAndRandomDropout(minimalStart, lowerMax),
                generateFluxWithKeysInRandomOrderAndRandomDropout(otherStart, maximumMax),
                generateFluxWithKeysInRandomOrderAndRandomDropout(minimalStart, lowerMax),
                generateFluxWithKeysInRandomOrderAndRandomDropout(otherStart, maximumMax),
                generateFluxWithKeysInRandomOrderAndRandomDropout(minimalStart, lowerMax),
                generateFluxWithKeysInRandomOrderAndRandomDropout(otherStart, maximumMax),
                generateFluxWithKeysInRandomOrderAndRandomDropout(minimalStart, lowerMax)
                                                         ), asList(
                                                                 generateFluxWithKeysInRandomOrderAndRandomDropout(minimalStart, lowerMax),
                                                                 generateFluxWithKeysInRandomOrderAndRandomDropout(otherStart, maximumMax),
                                                                 generateFluxWithKeysInRandomOrderAndRandomDropout(minimalStart, lowerMax),
                                                                 generateFluxWithKeysInRandomOrderAndRandomDropout(otherStart, maximumMax),
                                                                 generateFluxWithKeysInRandomOrderAndRandomDropout(minimalStart, lowerMax),
                                                                 generateFluxWithKeysInRandomOrderAndRandomDropout(otherStart, maximumMax),
                                                                 generateFluxWithKeysInRandomOrderAndRandomDropout(minimalStart, lowerMax)
                                                                                                                   ).size());

        HashMap<String, Boolean> receivedKeys = new HashMap<>();
        for (int i = minimalStart; i <= maximumMax; i++) {
            receivedKeys.put("key" + i, false);
        }

        StepVerifier.create(result)
                .thenConsumeWhile(r -> {
                    if (receivedKeys.get(r.getKey())) {
                        return false;
                    }
                    receivedKeys.put(r.getKey(), true);
                    return true;
                })
                .expectComplete()
                .verify();

        assertThat(receivedKeys.values().stream().allMatch(b -> b)).isTrue();
    }

    private CompletableFuture<List<KeyedResult>> generateList(Integer seed, Integer max) {
        return CompletableFuture.completedFuture(IntStream.range(seed, max + 1)
                                                          .mapToObj(state -> new KeyedResult("key" + state, state))
                                                          .collect(toList()));
    }

    private CompletableFuture<List<KeyedResult>> generateFluxWithKeysInRandomOrderAndRandomDropout(int start, int max) {
        List<Integer> keys = IntStream.range(start, max + 1).boxed().collect(toList());
        Collections.shuffle(keys);
        Random r = new Random();

        int toRemove = r.nextInt(10);
        while (toRemove > 0) {
            keys.remove(0);
            toRemove--;
        }

        return CompletableFuture.completedFuture(keys.stream()
                                                     .map(key -> new KeyedResult("key" + key, key))
                                                     .collect(toList()));
    }
}