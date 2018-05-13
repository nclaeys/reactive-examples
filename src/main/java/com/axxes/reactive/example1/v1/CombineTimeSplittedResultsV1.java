package com.axxes.reactive.example1.v1;

import com.axxes.reactive.example1.model.PartialResult;
import com.axxes.reactive.example1.model.Result;
import org.slf4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import static org.slf4j.LoggerFactory.getLogger;

public class CombineTimeSplittedResultsV1 implements com.axxes.reactive.example1.CombineTimeSplittedResults {
    private static final Logger LOGGER = getLogger(CombineTimeSplittedResultsV1.class);

    @Override
    public Flux<Result> transform(List<CompletableFuture<List<PartialResult>>> futureResults) {
        Flux<PartialResult> initialResults = Flux.<List<PartialResult>>create(sink -> addFutures(sink, futureResults))
                .flatMap(Flux::fromIterable);

        Flux<PartialResult> stitchedResult = stitchResults(initialResults);

        return stitchedResult
                .map(Result::fromPartialResult)
                .filter(hasIntervalLargerThan2());
    }

    private Flux<PartialResult> stitchResults(Flux<PartialResult> initialResults) {
        return Flux.create(fluxSink -> {
            PartialResultStitcher partialResultStitcher = new PartialResultStitcher(fluxSink);
            initialResults.subscribe(partialResultStitcher);
        });
    }

    private Predicate<Result> hasIntervalLargerThan2() {
        return partialResult -> {
            LOGGER.info("filtering result of length {}", partialResult.getIntervalSize());
            return partialResult.getIntervalSize() > 2;
        };
    }

    private void addFutures(FluxSink<List<PartialResult>> sink,
                            List<CompletableFuture<List<PartialResult>>> batchedFutureResults) {
        for (CompletableFuture<List<PartialResult>> batchedFutureResult : batchedFutureResults) {
            try {
                List<PartialResult> result = batchedFutureResult.get();
                sink.next(result);
            } catch (InterruptedException e) {
                LOGGER.error("interrupted", e);
            } catch (ExecutionException e) {
                LOGGER.error("execution", e);
            }
        }
        sink.complete();
    }
}
