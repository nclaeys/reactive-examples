package com.axxes.reactive.example1.original;

import com.axxes.reactive.example1.CombineTimeSplittedResults;
import com.axxes.reactive.example1.model.PartialResult;
import com.axxes.reactive.example1.model.Result;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CombineTimeSplittedResultsOriginal implements CombineTimeSplittedResults {
    @Override
    public Flux<Result> transform(List<CompletableFuture<List<PartialResult>>> futureResults) {
        List<PartialResult> intermediateResults = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(futureResults.size());

        return Flux.<PartialResult>create(sink -> {
            futureResults.forEach(future -> future.whenComplete(addOrResolveFuture(intermediateResults, sink, countDownLatch)));
            CompletableFuture.supplyAsync(() -> waitForResultsAndFinish(countDownLatch, intermediateResults, sink));
        }).map(Result::fromPartialResult);
    }

    private Void waitForResultsAndFinish(CountDownLatch countDownLatch, List<PartialResult> intermediateResults, FluxSink<PartialResult> sink) {
        try {
            boolean inTime = countDownLatch.await(10, TimeUnit.SECONDS);
            if (!inTime) {
                throw new RuntimeException("Timeout while waiting for influenceResults in the different layers of our multilayer search.");
            }
            Consumer<List<PartialResult>> stitcher = Stitcher.stitcher(sink);
            stitcher.accept(intermediateResults);
            sink.complete();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread was interrupted while waiting for ignite futures to complete", e);
        }
        return null;
    }

    private BiConsumer<List<PartialResult>, Throwable> addOrResolveFuture(List<PartialResult> intermediateResults, FluxSink<PartialResult> sink, CountDownLatch countDownLatch) {
        return (results, exception) -> {
            if (results != null) {
                for (PartialResult res : results) {
                    if (res.isPartial()) {
                        intermediateResults.add(res);
                    } else {
                        sendResultIfBigEnough(sink, res);
                    }
                }
                countDownLatch.countDown();
            }
        };
    }

    static void sendResultIfBigEnough(FluxSink<PartialResult> sink, PartialResult res) {
        if(res.getIntervalSize() > 2) {
            sink.next(res);
        }
    }
}
