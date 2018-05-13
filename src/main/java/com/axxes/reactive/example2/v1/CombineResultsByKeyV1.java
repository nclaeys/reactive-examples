package com.axxes.reactive.example2.v1;

import com.axxes.reactive.example2.CombineResultsByKey;
import com.axxes.reactive.example2.model.KeyedResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CombineResultsByKeyV1 implements CombineResultsByKey {
    @Override
    public Flux<KeyedResult> combine(List<CompletableFuture<List<KeyedResult>>> layerResults, int numberOfRuns) {
        //merge in contrast to concat mixes results from the two fluxes together.
        List<Flux<KeyedResult>> fluxOfRuns = toFluxOfLayers(layerResults);
        Flux<KeyedResult> mergedFlux = Flux.merge(fluxOfRuns);

        return combineResults(mergedFlux, 2);
    }

    private List<Flux<KeyedResult>> toFluxOfLayers(List<CompletableFuture<List<KeyedResult>>> batchResults) {
        List<Flux<KeyedResult>> fluxOfRuns = new ArrayList<>();
        for(CompletableFuture<List<KeyedResult>> batchResultsForRun: batchResults) {
            fluxOfRuns.add(Mono.fromFuture(batchResultsForRun)
                            .flux()
                            .flatMap(Flux::fromIterable));
        }
        return fluxOfRuns;
    }

    private Flux<KeyedResult> combineResults(Flux<KeyedResult> mergedFlux, int runs) {
        //expose our new flux such that someone eventually can subscribe to it.
        return Flux.create(sink -> {
            CombineBatchesSubscriber combineBatchesSubscriber = new CombineBatchesSubscriber(sink, runs);
            //connect the subscriber to the upstream flux to create a chain.
            mergedFlux.subscribe(combineBatchesSubscriber);
        });
    }
}
