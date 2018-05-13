package com.axxes.reactive.example1.v2;

import com.axxes.reactive.example1.model.PartialResult;
import reactor.core.publisher.FluxSink;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Consumer;

public class Stitcher {

    static Consumer<List<PartialResult>> stitcher(FluxSink<PartialResult> outputSink) {
        return prs -> {
            if (prs.isEmpty()) {
                return;
            }

            TreeSet<PartialResult> sortedPrs = new TreeSet<>(prs);
            Iterator<PartialResult> it = sortedPrs.iterator();
            PartialResult current = it.next();
            while (it.hasNext()) {
                PartialResult previous = current;
                current = it.next();

                if (previous.getUpperBound() == current.getLowerBound()) {
                    current = PartialResult.mergeResult(previous, current);
                } else {
                    outputSink.next(previous);
                }
            }
            outputSink.next(current);
        };
    }
}
