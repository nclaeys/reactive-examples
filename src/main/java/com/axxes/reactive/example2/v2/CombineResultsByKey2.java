package com.axxes.reactive.example2.v2;

import com.axxes.reactive.example2.model.KeyedResult;
import com.axxes.reactive.example2.CombineResultsByKey;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CombineResultsByKey2 implements CombineResultsByKey {

    @Override
    public Flux<KeyedResult> combine(List<CompletableFuture<List<KeyedResult>>> batchResults, int numberOfRuns) {
        List<Flux<KeyedResult>> resultsForRun = toFluxOfRuns(batchResults);

        return Flux.merge(resultsForRun)
                .groupBy(KeyedResult::getKey, KeyedResult::getResult)
                .flatMap(group -> new RunCombiner(group, numberOfRuns), 2048);
    }

    private List<Flux<KeyedResult>> toFluxOfRuns(List<CompletableFuture<List<KeyedResult>>> batchResults) {
        List<Flux<KeyedResult>> fluxOfRuns = new ArrayList<>();
        for(CompletableFuture<List<KeyedResult>> resultsForRun: batchResults) {
            fluxOfRuns.add(Mono.fromFuture(resultsForRun)
                                 .flux()
                                 .flatMap(Flux::fromIterable));
        }
        return fluxOfRuns;
    }
}
