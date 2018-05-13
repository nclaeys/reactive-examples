package com.axxes.reactive.example1.v4;

import com.axxes.reactive.example1.CombineTimeSplittedResults;
import com.axxes.reactive.example1.model.PartialResult;
import com.axxes.reactive.example1.model.Result;
import com.axxes.reactive.example1.v2.FuturePublisher;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CombineTimeSplittedResultsWithAbstraction implements CombineTimeSplittedResults {
    @Override
    public Flux<Result> transform(List<CompletableFuture<List<PartialResult>>> futureResults) {
        return genericAbstraction(futureResults)
                   .map(Result::fromPartialResult)
                   .filter(r -> r.getIntervalSize() > 2);
    }

    private Flux<PartialResult> genericAbstraction(List<CompletableFuture<List<PartialResult>>> futureResults) {
        return Flux.from(new FuturePublisher<>(futureResults))
                   .flatMap(Flux::fromIterable)
                   .groupBy(PartialResult::isPartial)
                   .flatMap(groupedFlux -> groupedFlux.key() ? new MergePartialResultPublisher<>(groupedFlux, new DefaultPartialResultMerger()) : groupedFlux);
    }
}
