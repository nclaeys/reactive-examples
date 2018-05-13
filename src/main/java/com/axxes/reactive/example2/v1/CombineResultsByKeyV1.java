package com.axxes.reactive.example2.v1;

import com.axxes.reactive.example1.v2.FuturePublisher;
import com.axxes.reactive.example2.CombineResultsByKey;
import com.axxes.reactive.example2.model.KeyedResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CombineResultsByKeyV1 implements CombineResultsByKey {
    @Override
    public Flux<KeyedResult> combine(List<List<CompletableFuture<KeyedResult>>> runResults, int numberOfRuns) {
        //merge in contrast to concat mixes results from the two fluxes together.
        List<Flux<KeyedResult>> fluxOfRuns = toFluxOfRuns(runResults);
        Flux<KeyedResult> mergedFlux = Flux.merge(fluxOfRuns);

        return combineResults(mergedFlux, 2);
    }

    private List<Flux<KeyedResult>> toFluxOfRuns(List<List<CompletableFuture<KeyedResult>>> batchResults) {
        List<Flux<KeyedResult>> fluxOfRuns = new ArrayList<>();
        for(List<CompletableFuture<KeyedResult>> batchResultsForRun: batchResults) {
            fluxOfRuns.add(Flux.from(new FuturePublisher<>(batchResultsForRun)));
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
