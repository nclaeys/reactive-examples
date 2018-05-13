package com.axxes.reactive.example2;

import com.axxes.reactive.example2.model.KeyedResult;
import reactor.core.publisher.Flux;

import java.util.List;

public interface CombineResultsByKey {

    Flux<KeyedResult> combine(List<List<KeyedResult>> layerResults);
}
