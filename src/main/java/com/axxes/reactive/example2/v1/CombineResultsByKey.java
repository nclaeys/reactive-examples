package com.axxes.reactive.example2.v1;

import com.axxes.reactive.example2.model.KeyedResult;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

public class CombineResultsByKey implements com.axxes.reactive.example2.CombineResultsByKey {
    @Override
    public Flux<KeyedResult> combine(List<List<KeyedResult>> layerResults) {
        //merge in contrast to concat mixes results from the two fluxes together.
        List<Flux<KeyedResult>> fluxOfLayers = toFluxOfLayers(layerResults);
        Flux<KeyedResult> mergedFlux = Flux.merge(fluxOfLayers);

        return combineResults(mergedFlux, 2);
    }

    private List<Flux<KeyedResult>> toFluxOfLayers(List<List<KeyedResult>> layerResults) {
        List<Flux<KeyedResult>> fluxOfLayers = new ArrayList<>();
        for(List<KeyedResult> layers: layerResults) {
            fluxOfLayers.add(Flux.fromIterable(layers));
        }
        return fluxOfLayers;
    }

    private Flux<KeyedResult> combineResults(Flux<KeyedResult> mergedFlux, int layers) {
        //expose our new flux such that someone eventually can subscribe to it.
        return Flux.create(sink -> {
            CombineLayersSubscriber combineLayersSubscriber = new CombineLayersSubscriber(sink, layers);
            //connect the subscriber to the upstream flux to create a chain.
            mergedFlux.subscribe(combineLayersSubscriber);
        });
    }
}
