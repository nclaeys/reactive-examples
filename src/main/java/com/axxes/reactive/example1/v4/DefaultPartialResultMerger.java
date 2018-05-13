package com.axxes.reactive.example1.v4;

import com.axxes.reactive.example1.model.PartialResult;

public class DefaultPartialResultMerger implements PartialResultMerger<PartialResult>{

    @Override
    public PartialResult merge(PartialResult previous, PartialResult next) {
        return PartialResult.mergeResult(previous, next);
    }
}
