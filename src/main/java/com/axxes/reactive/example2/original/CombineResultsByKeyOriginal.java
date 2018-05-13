package com.axxes.reactive.example2.original;

import com.axxes.reactive.example1.v2.FuturePublisher;
import com.axxes.reactive.example2.CombineResultsByKey;
import com.axxes.reactive.example2.model.KeyedResult;
import org.mockito.internal.util.collections.Iterables;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;

public class CombineResultsByKeyOriginal implements CombineResultsByKey, AutoCloseable {
    private ExecutorService executorService;

    public CombineResultsByKeyOriginal() {
        executorService = Executors.newFixedThreadPool(2);
    }

    @Override
    public Flux<KeyedResult> combine(List<List<CompletableFuture<KeyedResult>>> runResults, int numberOfRuns) {
        List<CompletableFuture<KeyedResult>> combinedResults = combineResults(runResults, numberOfRuns);

        return Flux.from(new FuturePublisher<>(combinedResults));
    }

    private List<CompletableFuture<KeyedResult>> combineResults(List<List<CompletableFuture<KeyedResult>>> runResults, int numberOfRuns) {
        return IntStream.range(0, nbOfFuturesPerRun(runResults))
                        .mapToObj(index -> toResultsByKey(runResults, index))
                        .map(resultsByKey -> {
                            CountDownLatch countDownLatch = new CountDownLatch(resultsByKey.size());
                            ArrayList<KeyedResult> completedResults = new ArrayList<>();
                            resultsByKey.forEach(future -> future.whenComplete(addResult(completedResults, countDownLatch)));
                            return supplyAsync(() -> waitForAllResults(completedResults, countDownLatch), executorService);
                        })
                        .map(future -> future.thenApply(partial(this::toAverageResultsForKeys, numberOfRuns)))
                        .collect(toList());
    }

    private BiConsumer<KeyedResult, Throwable> addResult(List<KeyedResult> completedResults, CountDownLatch countDownLatch) {
        return (keyedResult, e) -> {
            if (keyedResult != null) {
                completedResults.add(keyedResult);
            }
            countDownLatch.countDown();
        };
    }

    private List<KeyedResult> waitForAllResults(ArrayList<KeyedResult> resultsByKey, CountDownLatch countDownLatch) {
        try {
            boolean inTime = countDownLatch.await(10, TimeUnit.SECONDS);
            if (!inTime) {
                throw new RuntimeException("Timeout while waiting for influenceResults in the different layers of our multilayer search.");
            }
            return resultsByKey;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread was interrupted while waiting for ignite futures to complete", e);
        }
    }

    private int nbOfFuturesPerRun(List<List<CompletableFuture<KeyedResult>>> runResults) {
        return runResults.get(0).size();
    }

    private List<CompletableFuture<KeyedResult>> toResultsByKey(List<List<CompletableFuture<KeyedResult>>> runResults, int index) {
        return runResults.stream()
                         .map(result -> result.get(index))
                         .collect(Collectors.toList());
    }

    private KeyedResult toAverageResultsForKeys(Integer numberOfResults, List<KeyedResult> results) {
        float average = results.stream()
                               .map(KeyedResult::getResult)
                               .reduce(Float::sum)
                               .orElseThrow(IllegalArgumentException::new) / numberOfResults;
        return new KeyedResult(Iterables.firstOf(results).getKey(), average);
    }

    @Override
    public void close() throws Exception {
        executorService.shutdownNow();
    }

    private static Function<List<KeyedResult>, KeyedResult> partial(BiFunction<Integer, List<KeyedResult>, KeyedResult> fn, Integer nbOfRuns) {
        return results -> fn.apply(nbOfRuns, results);
    }
}
