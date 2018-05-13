package com.axxes.reactive.example2.v1;

import com.axxes.reactive.example2.model.KeyedResult;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.FluxSink;

import java.util.Collection;
import java.util.Map;

public class CombineBatchesSubscriber extends BaseSubscriber<KeyedResult> {
    //interact with the downstream subscriber through the sink
    private final FluxSink<KeyedResult> sink;
    private final int runs;
    private Multimap<String, KeyedResult> keyToResults = ArrayListMultimap.create();

    public CombineBatchesSubscriber(FluxSink<KeyedResult> sink, int runs) {
        this.sink = sink;
        this.runs = runs;
    }

    @Override
    protected void hookOnNext(KeyedResult value) {
        handleResult(value);
    }

    @Override
    protected void hookOnComplete() {
        sendRemainingResults(runs);
    }

    private void sendRemainingResults(int layers) {
        Map<String, Collection<KeyedResult>> remainingResults = keyToResults.asMap();
        for(String key: remainingResults.keySet()) {
            Collection<KeyedResult> resultsForKey = remainingResults.get(key);
            float averageValue = calculateAverageValue(resultsForKey, layers);
            sink.next(new KeyedResult(key, averageValue));
        }
        sink.complete();
    }

    private void handleResult(KeyedResult keyedResult) {
        String currentKey = keyedResult.getKey();
        keyToResults.put(currentKey, keyedResult);
        Collection<KeyedResult> keyedResultsForKey = keyToResults.get(currentKey);
        if (keyedResultsForKey.size() == runs) {
            float averageSignificance = calculateAverageValue(keyedResultsForKey, runs);
            sink.next(new KeyedResult(currentKey, averageSignificance));
            keyToResults.removeAll(currentKey);
        }
    }

    private float calculateAverageValue(Collection<KeyedResult> keyedResultsForKey, int layers) {
        return keyedResultsForKey.stream()
                                 .map(KeyedResult::getResult)
                                 .reduce(Float::sum)
                                 .orElseThrow(IllegalArgumentException::new) / layers;
    }
}
