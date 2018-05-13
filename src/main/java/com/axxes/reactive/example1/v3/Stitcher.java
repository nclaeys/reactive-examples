package com.axxes.reactive.example1.v3;

import com.axxes.reactive.example1.model.PartialResult;
import reactor.core.publisher.FluxSink;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class Stitcher {

    static Consumer<List<PartialResult>> stitcher(FluxSink<PartialResult> outputSink) {
        return prs -> {
            if (prs.isEmpty()) {
                return;
            }

            Collections.sort(prs);
            Iterator<PartialResult> it = prs.iterator();
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
