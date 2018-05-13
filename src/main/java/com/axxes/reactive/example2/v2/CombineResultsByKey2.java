package com.axxes.reactive.example2.v2;

import com.axxes.reactive.example2.model.KeyedResult;
import com.axxes.reactive.example2.CombineResultsByKey;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

public class CombineResultsByKey2 implements CombineResultsByKey {

    @Override
    public Flux<KeyedResult> combine(List<List<KeyedResult>> layerResults) {
        int numberOfLayers = layerResults.size();
        List<Flux<KeyedResult>> fluxOfLayers = toFluxOfLayers(layerResults);

        return Flux.merge(fluxOfLayers)
                .groupBy(KeyedResult::getKey, KeyedResult::getResult)
                .flatMap(group -> new LayerCombiner(group, numberOfLayers), 2048);
    }

    private List<Flux<KeyedResult>> toFluxOfLayers(List<List<KeyedResult>> layerResults) {
        List<Flux<KeyedResult>> fluxOfLayers = new ArrayList<>();
        for(List<KeyedResult> layers: layerResults) {
            fluxOfLayers.add(Flux.fromIterable(layers));
        }
        return fluxOfLayers;
    }
}
