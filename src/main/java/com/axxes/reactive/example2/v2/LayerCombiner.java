package com.axxes.reactive.example2.v2;

import com.axxes.reactive.example2.model.KeyedResult;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.GroupedFlux;

import java.util.List;

public class LayerCombiner implements Publisher<KeyedResult> {

    private final GroupedFlux<String, Float> allLayersCombined;
    private final int numberOfLayers;

    LayerCombiner(GroupedFlux<String, Float> allLayersCombined, int numberOfLayers) {
        this.allLayersCombined = allLayersCombined;
        this.numberOfLayers = numberOfLayers;
    }

    @Override
    public void subscribe(Subscriber<? super KeyedResult> subscriber) {
        allLayersCombined
                .take(numberOfLayers)
                .buffer()
                .subscribe(values -> {
                    subscriber.onNext(new KeyedResult(allLayersCombined.key(), sum(values) / numberOfLayers));
                    subscriber.onComplete();
                });
    }

    private float sum(List<Float> values) {
        float result = 0;
        for (Float value : values) {
            result += value;
        }
        return result;
    }
}
