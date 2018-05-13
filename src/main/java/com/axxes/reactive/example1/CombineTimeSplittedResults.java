package com.axxes.reactive.example1;

import com.axxes.reactive.example1.model.PartialResult;
import com.axxes.reactive.example1.model.Result;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CombineTimeSplittedResults {

    /**
     * Make time splitted results reactive, by returning the stream of completed results. Each
     * future stands for a part of the computation. This reference example compiles partial results
     * across futures into completed results and filters out results that are too short.
     * @param futureResults For each partial computation a future; the result of a future is list of partial results
     * @return Stream of all full results, filtered on minimal length
     */
    Flux<Result> transform(List<CompletableFuture<List<PartialResult>>> futureResults);
}
